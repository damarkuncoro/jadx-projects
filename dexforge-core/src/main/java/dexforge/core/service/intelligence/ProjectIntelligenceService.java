package dexforge.core.service.intelligence;

import dexforge.core.parser.apk.ApkLoader;
import dexforge.core.parser.resolver.ResourceResolver;
import dexforge.core.parser.dex.service.DexFastIndexer;
import dexforge.core.parser.analysis.callgraph.model.CallGraphNode;
import dexforge.core.parser.analysis.deobf.HeuristicDeobfuscator;
import dexforge.core.parser.analysis.deobf.strategy.impl.EmulatedDeobfuscationStrategy;
import dexforge.core.parser.analysis.patterns.DeobfuscatorHeuristicScanner;
import dexforge.core.parser.analysis.dataflow.InterProceduralAnalyzer;
import dexforge.core.parser.analysis.deobf.strategy.impl.GenericXorStrategy;
import dexforge.core.parser.analysis.deobf.strategy.impl.ResourceLookupStrategy;
import dexforge.core.parser.analysis.deobf.strategy.impl.ReflectionResolverStrategy;
import dexforge.core.parser.axml.model.AxmlNode;
import dexforge.core.parser.axml.service.LayoutAnalyzer;
import dexforge.core.parser.axml.service.VisualLayoutRenderer;
import dexforge.core.service.security.VulnerabilityScanner;
import dexforge.core.service.security.model.VulnerabilityIssue;
import dexforge.core.service.security.taint.GlobalTaintAnalyzer;
import dexforge.core.service.plugin.PluginManager;
import dexforge.core.service.intelligence.registry.DeobfuscationRegistry;
import dexforge.api.intelligence.IProjectIntelligence;
import dexforge.core.parser.dex.decompiler.JavaDecompiler;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Aggregates findings from all parsers to provide high-level insights about the APK.
 * Integrated with Deobfuscator, Unified Resolver, and Inter-procedural DFA.
 */
public final class ProjectIntelligenceService implements IProjectIntelligence {
    private final ApkLoader loader;
    private final MalwareDetector malwareDetector = new MalwareDetector();
    private final Map<String, String> deobfuscationMap = new HashMap<>();
    private final List<String> autoDetectedDeobf = new ArrayList<>();
    private final DeobfuscationRegistry deobfRegistry = new DeobfuscationRegistry();
    private final PluginManager pluginManager = new PluginManager();
    private InterProceduralAnalyzer ipDfa;
    private GlobalTaintAnalyzer taintAnalyzer;

    public ProjectIntelligenceService(ApkLoader loader) {
        this.loader = loader;
        initStrategies();
        runInitialAnalysis();
    }

    private void initStrategies() {
        deobfRegistry.registerStrategy(new ResourceLookupStrategy(loader.getResourceResolver()));
        deobfRegistry.registerStrategy(new ReflectionResolverStrategy());
        deobfRegistry.registerStrategy(new GenericXorStrategy("deobf", 0x42));
    }

    public PluginManager getPluginManager() {
        return pluginManager;
    }

    public JavaDecompiler createAnnotatedDecompiler(DexFastIndexer indexer) {
        JavaDecompiler decompiler = new JavaDecompiler(indexer);
        decompiler.setDeobfuscationMap(deobfuscationMap);
        decompiler.setDeobfuscationRegistry(deobfRegistry);
        decompiler.setJniBridges(loader.getJniBridges());
        return decompiler;
    }

    private void runInitialAnalysis() {
        // 1. Run deobfuscator first
        for (DexFastIndexer indexer : loader.getIndexers()) {
            HeuristicDeobfuscator deobf = new HeuristicDeobfuscator(indexer);
            deobfuscationMap.putAll(deobf.deobfuscate());

            // 1.5 Auto-detect and register Obfuscation Strategies
            DeobfuscatorHeuristicScanner scanner = new DeobfuscatorHeuristicScanner(indexer);
            List<String> detectedMethods = scanner.findDeobfuscatorMethods();
            for (String sig : detectedMethods) {
                autoDetectedDeobf.add(sig);

                // 1.6 Auto-register Emulated Strategy for each detected method
                var instructions = indexer.getMethodInstructions(sig);
                if (!instructions.isEmpty()) {
                    deobfRegistry.registerStrategy(new EmulatedDeobfuscationStrategy(sig, instructions));
                    System.out.println("[INTEL] Auto-registered emulator for: " + sig);
                }
            }
        }

        // 2. Initialize and run Inter-procedural DFA
        if (!loader.getIndexers().isEmpty()) {
            this.ipDfa = new InterProceduralAnalyzer(loader.getIndexers().get(0), loader.getGlobalCallGraph());
            this.taintAnalyzer = new GlobalTaintAnalyzer(ipDfa);
            this.taintAnalyzer.analyze();
        }
    }

    @Override
    public Map<String, Object> getProjectInsights() {
        Map<String, Object> insights = new HashMap<>();

        // 1. Core Metadata
        insights.put("framework", detectFramework());
        insights.put("packer", malwareDetector.detectPacker(loader.getJniBridges().values()));
        insights.put("deobfuscationStats", deobfuscationMap.size() + " classes recovered");
        insights.put("autoDetectedDeobf", autoDetectedDeobf);

        // 2. Security & Vulnerabilities
        VulnerabilityScanner scanner = new VulnerabilityScanner(loader);
        scanner.setInterProceduralAnalyzer(ipDfa);
        scanner.setManifestAnalyzer(loader.getManifestAnalyzer());
        List<VulnerabilityIssue> issues = scanner.scan();
        insights.put("securityScore", calculateSecurityScore(issues.size()));
        insights.put("vulnerabilities", issues);

        // 3. Technical Debt / Hot Paths
        insights.put("hotMethods", findHotMethods());
        insights.put("technologies", detectTechnologies());

        // 4. UI & Resources
        insights.put("uiStats", aggregateUiStats());
        insights.put("topResources", findTopResources());

        // 5. DFA & Taint Insights
        if (ipDfa != null) {
            insights.put("analyzedMethods", ipDfa.getMethodAnalyzers().size());
            if (taintAnalyzer != null) {
                insights.put("piiLeaks", taintAnalyzer.getFindings());
            }
        }

        return insights;
    }

    public String getDeobfuscatedName(String originalName) {
        return deobfuscationMap.getOrDefault(originalName, originalName);
    }

    private List<String> findTopResources() {
        Map<Integer, Integer> globalStats = new HashMap<>();
        for (DexFastIndexer indexer : loader.getIndexers()) {
            indexer.getResourceUsageStats().forEach((id, count) ->
                globalStats.merge(id, count, Integer::sum));
        }

        ResourceResolver resolver = loader.getResourceResolver();
        return globalStats.entrySet().stream()
            .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
            .limit(10)
            .map(e -> resolver.resolveOrDefault(e.getKey()) + " (" + e.getValue() + " usages)")
            .collect(Collectors.toList());
    }

    private String detectFramework() {
        Map<String, String> bridges = loader.getJniBridges();
        for (String key : bridges.keySet()) {
            if (key.contains("io.flutter")) return "Flutter";
            if (key.contains("com.facebook.react")) return "React Native";
            if (key.contains("com.unity3d")) return "Unity";
        }
        return "Native (Java/Kotlin)";
    }

    private List<String> findHotMethods() {
        List<String> hot = new ArrayList<>();
        Map<String, CallGraphNode> nodes = loader.getGlobalCallGraph();

        nodes.values().stream()
            .filter(n -> n.getFanIn() > 5)
            .sorted((a, b) -> Integer.compare(b.getFanIn(), a.getFanIn()))
            .limit(10)
            .forEach(n -> {
                String sig = n.getSignature();
                for (Map.Entry<String, String> entry : deobfuscationMap.entrySet()) {
                    sig = sig.replace(entry.getKey(), entry.getValue());
                }
                hot.add(sig + " (" + n.getFanIn() + " calls)");
            });

        return hot;
    }

    private List<String> detectTechnologies() {
        List<String> techs = new ArrayList<>();
        if (!loader.getJniBridges().isEmpty()) techs.add("Native C/C++");
        techs.add(detectFramework());

        for (String activity : loader.getActivities()) {
            if (activity.contains("com.google.firebase")) techs.add("Firebase");
            if (activity.contains("dagger.hilt")) techs.add("Hilt/Dagger");
        }
        return techs.stream().distinct().collect(Collectors.toList());
    }

    private Map<String, Integer> aggregateUiStats() {
        Map<String, Integer> globalStats = new HashMap<>();
        for (AxmlNode layout : loader.getLayouts()) {
            LayoutAnalyzer analyzer = new LayoutAnalyzer(layout);
            Map<String, Integer> stats = analyzer.getViewStats();
            stats.forEach((k, v) -> globalStats.merge(k, v, Integer::sum));
        }
        return globalStats;
    }

    private String calculateSecurityScore(int issueCount) {
        if (issueCount == 0) return "A+";
        if (issueCount < 3) return "A";
        if (issueCount < 7) return "B";
        if (issueCount < 15) return "C";
        return "D (High Risk)";
    }
}
