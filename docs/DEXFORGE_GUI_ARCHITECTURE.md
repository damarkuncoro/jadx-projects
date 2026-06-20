# DexForge GUI Architecture: Incremental Migration Guide

## Ringkasan Eksekutif

Dokumen ini menjelaskan strategi mentransformasi `jadx-gui` menjadi `dexforge-gui` dengan menerapkan:
- **Clean Architecture** (4 layers: Presentation, Application, Domain, Infrastructure)
- **Domain-Driven Design (DDD)** (bounded contexts, aggregates, value objects)
- **SOLID Principles** (SRP, OCP, LSP, ISP, DIP)
- **DRY & Reusability** (shared components, cross-cutting concerns)
- **Scalability** (modular plugin system, event-driven architecture)

**Status**: Phase 5 - IDE Extensions in progress  
**Timeline**: 6-12 months (incremental phases)  
**Effort**: Medium-High (10-15 engineer-weeks)

---

## Bagian 1: Current State Analysis

### Current Architecture (jadx-gui)

```
jadx-gui/src/main/java/jadx/gui/
├── ui/                          # Presentation layer (mixed concerns)
│   ├── MainWindow.java
│   ├── TabbedPane.java
│   ├── panel/                   # Various panels (monolithic)
│   └── popupmenu/               # Context menus
├── treemodel/                   # Mixed domain + presentation
│   ├── JNode.java               # Tree node abstraction
│   ├── JClass.java              # Wraps jadx.api.JavaClass
│   ├── JMethod.java
│   └── JPackage.java
├── cache/                       # Infrastructure (caching)
├── jobs/                        # Infrastructure (background tasks)
├── device/                      # Feature: Device Explorer
├── frida/                        # Feature: Frida Integration
├── plugins/                      # Plugin system (basic)
├── settings/                     # Configuration management
├── utils/                        # Utility functions
├── search/                       # Search feature
├── events/                       # Event bus
└── JadxGUI.java                 # Entry point (main orchestrator)
```

**Problems:**
- ❌ UI logic mixed with domain logic
- ❌ Bidirectional dependencies between layers
- ❌ Tightly coupled to JADX core API
- ❌ Hard to test (many UI dependencies)
- ❌ Code reuse limited (Swing-specific)
- ❌ Feature coupling (Device Explorer, Frida tightly integrated)
- ❌ No clear bounded contexts
- ❌ Cache/jobs/plugins lack clear ownership

---

## Bagian 2: Target Architecture (dexforge-gui)

### Clean Architecture Layers

```
dexforge-gui/src/main/java/dexforge/gui/
│
├── presentation/                # UI Layer (Swing-specific)
│   ├── swing/                   # Swing implementation
│   │   ├── components/          # Reusable Swing components
│   │   ├── panels/              # Feature panels
│   │   ├── dialogs/             # Dialog boxes
│   │   └── MainWindow.java      # Entry point for UI
│   └── model/                   # UI state models (not domain)
│       ├── TreeNodeViewModel.java
│       ├── SearchResultsViewModel.java
│       └── TabState.java
│
├── application/                 # Application Layer (Use Cases)
│   ├── usecases/                # Orchestrators
│   │   ├── OpenProjectUseCase.java
│   │   ├── SearchCodeUseCase.java
│   │   ├── DecompileClassUseCase.java
│   │   ├── ExportReportUseCase.java
│   │   └── GenerateFridaHookUseCase.java
│   ├── dto/                     # Data transfer objects (inbound/outbound)
│   │   ├── OpenProjectRequest.java
│   │   ├── SearchRequest.java
│   │   └── SearchResult.java
│   ├── port/                    # Interfaces (outbound contracts)
│   │   ├── ProjectRepository.java
│   │   ├── SearchEngine.java
│   │   ├── DecompileEngine.java
│   │   └── NotificationPort.java
│   └── exception/               # Application exceptions
│       ├── ProjectNotOpenException.java
│       └── DecompilationFailedException.java
│
├── domain/                      # Domain Layer (Business Logic)
│   ├── model/                   # Aggregates & entities
│   │   ├── project/
│   │   │   ├── Project.java     # Root aggregate
│   │   │   ├── ProjectId.java   # Value object
│   │   │   ├── ProjectConfig.java
│   │   │   └── ProjectModule.java
│   │   ├── source/
│   │   │   ├── SourceFile.java  # Root aggregate
│   │   │   ├── SourceFileId.java
│   │   │   ├── ClassDefinition.java
│   │   │   └── MethodSignature.java (value object)
│   │   ├── analysis/
│   │   │   ├── CodeAnalysis.java
│   │   │   └── SecurityFinding.java
│   │   └── search/
│   │       ├── SearchQuery.java
│   │       └── SearchResult.java
│   ├── service/                 # Domain services
│   │   ├── ProjectService.java
│   │   ├── SearchService.java
│   │   └── CodeAnalysisService.java
│   ├── event/                   # Domain events
│   │   ├── ProjectOpenedEvent.java
│   │   ├── ClassDecompiledEvent.java
│   │   └── SearchCompletedEvent.java
│   └── exception/               # Domain exceptions
│       ├── InvalidProjectException.java
│       └── SourceNotFoundException.java
│
└── infrastructure/              # Infrastructure Layer
    ├── adapter/                 # Adapters (inbound & outbound)
    │   ├── jadx/                # JADX decompiler adapter
    │   │   ├── JadxDecompileAdapter.java
    │   │   ├── JadxProjectRepository.java
    │   │   └── JadxClassLoader.java
    │   ├── cache/               # Cache adapter
    │   │   ├── CacheAdapter.java
    │   │   └── GuiCacheManager.java
    │   ├── notification/        # Notification adapter
    │   │   └── SwingNotificationAdapter.java
    │   └── search/              # Search adapter
    │       └── LuceneSearchAdapter.java
    ├── event/                   # Event infrastructure
    │   ├── EventBus.java
    │   └── EventListener.java
    ├── config/                  # Configuration
    │   └── GuiConfiguration.java
    └── persistence/             # Persistence
        ├── ProjectPersistence.java
        └── UserPreferences.java
```

### DDD Bounded Contexts

```
DexForge GUI Domain Model
│
├── Project Management Context
│   - Responsibilities: Project lifecycle, module structure
│   - Aggregates: Project, ProjectModule
│   - Services: ProjectService
│   - Events: ProjectOpenedEvent, ProjectClosedEvent
│
├── Source Code Context
│   - Responsibilities: Class/method/field definitions
│   - Aggregates: SourceFile, ClassDefinition
│   - Value Objects: MethodSignature, FieldDefinition
│   - Services: CodeAnalysisService
│   - Events: ClassDecompiledEvent, CodeAnalyzedEvent
│
├── Search Context
│   - Responsibilities: Find code, references, definitions
│   - Aggregates: SearchQuery, SearchResult
│   - Services: SearchService
│   - Events: SearchCompletedEvent
│
├── Device Integration Context
│   - Responsibilities: Device connection, APK extraction
│   - Aggregates: Device, ExtractedAPK
│   - Services: DeviceService
│   - Events: DeviceConnectedEvent, APKExtractedEvent
│
└── Report/Export Context
    - Responsibilities: Generate reports, export analysis
    - Aggregates: Report, ExportConfiguration
    - Services: ReportGenerationService
    - Events: ReportGeneratedEvent
```

---

## Bagian 3: Migration Strategy (Incremental Phases)

### Phase 0: Foundation (Weeks 1-2)

**Goal**: Setup infrastructure untuk layered architecture

**Tasks:**
1. Create new module structure in `dexforge-core` (already exists)
2. Define domain value objects (immutable, language primitives)
3. Define application ports/interfaces
4. Setup event bus infrastructure

**Deliverables:**
- `dexforge-core/src/main/java/dexforge/domain/` with base classes
- `dexforge-core/src/main/java/dexforge/application/` with ports
- Event infrastructure (EventBus, DomainEvent)

**Example Code (Phase 0):**

```java
// Domain: Value Object (immutable)
package dexforge.domain.model.project;

public final class ProjectId {
    private final String value;
    
    private ProjectId(String value) {
        this.value = value;
    }
    
    public static ProjectId of(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("ProjectId cannot be empty");
        }
        return new ProjectId(id);
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ProjectId)) return false;
        return value.equals(((ProjectId) o).value);
    }
    
    @Override
    public int hashCode() {
        return value.hashCode();
    }
    
    @Override
    public String toString() {
        return value;
    }
}

// Domain: Aggregate Root
public class Project {
    private final ProjectId id;
    private final String name;
    private final Path rootPath;
    private final List<ProjectModule> modules;
    private ProjectStatus status;
    
    private Project(ProjectId id, String name, Path rootPath) {
        this.id = id;
        this.name = name;
        this.rootPath = rootPath;
        this.modules = new ArrayList<>();
        this.status = ProjectStatus.CREATED;
    }
    
    public static Project create(ProjectId id, String name, Path rootPath) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(name);
        Objects.requireNonNull(rootPath);
        return new Project(id, name, rootPath);
    }
    
    // Domain logic
    public void addModule(ProjectModule module) {
        if (status != ProjectStatus.CREATED) {
            throw new IllegalStateException("Cannot add module in " + status + " state");
        }
        modules.add(module);
    }
    
    public void open() {
        status = ProjectStatus.OPENED;
        // Emit domain event
        raise(new ProjectOpenedEvent(id, name));
    }
    
    // Getters (immutable)
    public ProjectId getId() { return id; }
    public String getName() { return name; }
    public Path getRootPath() { return rootPath; }
    public List<ProjectModule> getModules() { return Collections.unmodifiableList(modules); }
    
    // DDD event sourcing support
    private List<DomainEvent> changes = new ArrayList<>();
    
    protected void raise(DomainEvent event) {
        changes.add(event);
    }
    
    public List<DomainEvent> getUncommittedEvents() {
        return Collections.unmodifiableList(changes);
    }
    
    public void markEventsAsCommitted() {
        changes.clear();
    }
}

// Application: Use Case
public class OpenProjectUseCase {
    private final ProjectRepository projectRepository;
    private final NotificationPort notificationPort;
    private final EventBus eventBus;
    
    public OpenProjectUseCase(ProjectRepository projectRepository, 
                              NotificationPort notificationPort,
                              EventBus eventBus) {
        this.projectRepository = projectRepository;
        this.notificationPort = notificationPort;
        this.eventBus = eventBus;
    }
    
    public void execute(OpenProjectRequest request) throws ProjectNotOpenException {
        try {
            ProjectId projectId = ProjectId.of(request.getProjectPath());
            Project project = Project.create(projectId, 
                                            request.getProjectName(),
                                            Paths.get(request.getProjectPath()));
            
            project.open();
            projectRepository.save(project);
            
            // Emit domain events
            project.getUncommittedEvents().forEach(eventBus::publish);
            project.markEventsAsCommitted();
            
            notificationPort.notifySuccess("Project opened: " + project.getName());
        } catch (Exception e) {
            throw new ProjectNotOpenException("Failed to open project", e);
        }
    }
}

// Application: Port (interface)
public interface ProjectRepository {
    void save(Project project);
    Optional<Project> findById(ProjectId id);
    List<Project> findAll();
}

// Domain: Event
public class ProjectOpenedEvent implements DomainEvent {
    private final ProjectId projectId;
    private final String projectName;
    private final LocalDateTime occurredAt;
    
    public ProjectOpenedEvent(ProjectId projectId, String projectName) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.occurredAt = LocalDateTime.now();
    }
    
    public ProjectId getProjectId() { return projectId; }
    public String getProjectName() { return projectName; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
}
```

---

### Phase 1: Domain Model (Weeks 3-4)

**Goal**: Extract domain logic dari jadx-gui, define aggregates & entities

**Tasks:**
1. Create SourceFile & ClassDefinition aggregates
2. Create Method, Field value objects
3. Create Search aggregate
4. Implement domain services (logic that spans aggregates)
5. Write domain-only tests (no UI dependencies)

**Deliverables:**
- Domain model classes (immutable, no Swing dependencies)
- Domain services
- 80%+ test coverage for domain

**SOLID Principles Applied:**
- **SRP**: Each class has ONE responsibility (Project manages project, Method manages method data)
- **OCP**: New domain concepts extend, not modify existing code
- **DIP**: Services depend on abstractions, not concrete implementations

---

### Phase 2: Application Layer (Weeks 5-6)

**Goal**: Create use case orchestrators with clear IO contracts

**Tasks:**
1. Create 10-15 core use cases (OpenProject, SearchCode, DecompileClass, etc.)
2. Define DTOs for request/response
3. Implement application service
4. Create port interfaces (ProjectRepository, SearchEngine, etc.)
5. Wire domain services into use cases

**Deliverables:**
- Use case classes with @UseCase annotation
- Request/Response DTOs
- Port interfaces
- Application exception hierarchy
- 70%+ test coverage (tests with mocked ports)

**Example Use Case (Phase 2):**

```java
@UseCase
public class SearchCodeUseCase {
    private final SearchService searchService;      // Domain service
    private final SearchRepository searchRepository; // Port
    private final NotificationPort notificationPort; // Port
    private final EventBus eventBus;
    
    public SearchCodeUseCase(SearchService searchService,
                             SearchRepository searchRepository,
                             NotificationPort notificationPort,
                             EventBus eventBus) {
        this.searchService = searchService;
        this.searchRepository = searchRepository;
        this.notificationPort = notificationPort;
        this.eventBus = eventBus;
    }
    
    public SearchResultsResponse execute(SearchCodeRequest request) throws SearchFailedException {
        try {
            // Create domain object
            SearchQuery query = SearchQuery.of(
                request.getQueryText(),
                request.getSearchType(),
                request.getProject()
            );
            
            // Use domain service
            SearchResults results = searchService.search(query);
            
            // Persist if needed
            searchRepository.save(results);
            
            // Emit event
            eventBus.publish(new SearchCompletedEvent(query, results));
            
            // Return DTO
            return SearchResultsResponse.from(results);
        } catch (Exception e) {
            throw new SearchFailedException("Search failed", e);
        }
    }
}

// DTO (Application layer - only data transfer)
public class SearchCodeRequest {
    private final String queryText;
    private final SearchType searchType;
    private final ProjectId projectId;
    
    public SearchCodeRequest(String queryText, SearchType searchType, ProjectId projectId) {
        this.queryText = queryText;
        this.searchType = searchType;
        this.projectId = projectId;
    }
    
    // Getters only
    public String getQueryText() { return queryText; }
    public SearchType getSearchType() { return searchType; }
    public ProjectId getProjectId() { return projectId; }
}
```

---

### Phase 3: Presentation Model (Weeks 7-8)

**Goal**: Create presentation models that translate domain to UI

**Tasks:**
1. Create ViewModels for each major feature
2. Implement value-to-presentation mapping
3. Create reactive state management (using RxJava3 already in project)
4. Define presentation ports (what UI needs from infrastructure)
5. Create presentation adapters

**Deliverables:**
- ViewModel classes
- Mappers (Domain → ViewModel)
- Presentation state management
- 60%+ test coverage (tests without Swing)

**Example ViewModel (Phase 3):**

```java
// Presentation: ViewModel
@ViewModel
public class ProjectExplorerViewModel {
    private final Observable<TreeNodeViewModel> treeNodes;
    private final Observable<ProjectLoadingState> loadingState;
    private final Observable<ProjectError> errors;
    
    private final Subject<String> nodeSelections = new PublishSubject<>();
    private final Subject<Void> refreshRequest = new PublishSubject<>();
    
    private final GetProjectTreeUseCase getProjectTreeUseCase;
    private final Disposable subscription;
    
    public ProjectExplorerViewModel(GetProjectTreeUseCase getProjectTreeUseCase) {
        this.getProjectTreeUseCase = getProjectTreeUseCase;
        
        // Observable that reacts to selections and refresh requests
        this.treeNodes = Observable.merge(
            nodeSelections.flatMap(id -> 
                Observable.fromCallable(() -> 
                    getProjectTreeUseCase.execute(new GetProjectTreeRequest(id))
                ).map(response -> TreeNodeViewModel.from(response.getNodes()))
            ),
            refreshRequest.flatMap(__ ->
                Observable.fromCallable(() -> getProjectTreeUseCase.execute(new GetProjectTreeRequest(null)))
                    .map(response -> TreeNodeViewModel.from(response.getNodes()))
            )
        ).replay(1).refCount();
        
        this.loadingState = treeNodes.map(__ -> ProjectLoadingState.LOADED)
            .startWith(ProjectLoadingState.LOADING);
        
        this.errors = treeNodes.map(__ -> (ProjectError) null)
            .onErrorReturn(throwable -> ProjectError.from(throwable));
        
        this.subscription = Observable.combineLatest(
            treeNodes, loadingState, errors,
            (nodes, state, error) -> nodes
        ).subscribe();
    }
    
    // UI can observe these
    public Observable<TreeNodeViewModel> getTreeNodes() {
        return treeNodes;
    }
    
    public Observable<ProjectLoadingState> getLoadingState() {
        return loadingState;
    }
    
    public Observable<ProjectError> getErrors() {
        return errors;
    }
    
    // UI can trigger these
    public void selectNode(String nodeId) {
        nodeSelections.onNext(nodeId);
    }
    
    public void refresh() {
        refreshRequest.onNext(null);
    }
    
    public void cleanup() {
        subscription.dispose();
    }
}

// Presentation: Value Object
public final class TreeNodeViewModel {
    private final String id;
    private final String label;
    private final int iconType;
    private final List<TreeNodeViewModel> children;
    private final boolean isLeaf;
    
    private TreeNodeViewModel(String id, String label, int iconType, 
                            List<TreeNodeViewModel> children, boolean isLeaf) {
        this.id = id;
        this.label = label;
        this.iconType = iconType;
        this.children = Collections.unmodifiableList(children);
        this.isLeaf = isLeaf;
    }
    
    public static TreeNodeViewModel from(TreeNode node) {
        return new TreeNodeViewModel(
            node.getId(),
            node.getLabel(),
            node.getIconType(),
            node.getChildren().stream()
                .map(TreeNodeViewModel::from)
                .collect(Collectors.toList()),
            node.isLeaf()
        );
    }
    
    // Getters for UI binding
    public String getId() { return id; }
    public String getLabel() { return label; }
    public int getIconType() { return iconType; }
    public List<TreeNodeViewModel> getChildren() { return children; }
    public boolean isLeaf() { return isLeaf; }
}
```

---

### Phase 4: Swing Presentation Layer (Weeks 9-10)

**Goal**: Rewrite Swing UI to consume ViewModels & Use Cases

**Tasks:**
1. Create reusable Swing components (panels, dialogs, trees)
2. Wire UI components to ViewModels (data binding)
3. Wire UI actions to Use Cases (command execution)
4. Implement reactive Swing bindings (RxJava → Swing)
5. Create MainWindow orchestrator

**Deliverables:**
- Reusable Swing components
- UI-ViewModel binding layer
- Updated MainWindow
- 40%+ test coverage (UI integration tests)

**Example Swing Binding (Phase 4):**

```java
// Swing Component: Reusable
public class ProjectExplorerPanel extends JPanel {
    private final ProjectExplorerViewModel viewModel;
    private final JTree tree;
    private final JLabel loadingLabel;
    private final Disposable subscription;
    
    public ProjectExplorerPanel(ProjectExplorerViewModel viewModel) {
        this.viewModel = viewModel;
        
        setLayout(new BorderLayout());
        
        // Create tree
        this.tree = new JTree();
        add(new JScrollPane(tree), BorderLayout.CENTER);
        
        // Create loading indicator
        this.loadingLabel = new JLabel("Loading...");
        add(loadingLabel, BorderLayout.SOUTH);
        
        // Bind ViewModel to Swing components
        this.subscription = bindViewModel();
        
        // Wire user actions to ViewModel
        wireMouseActions();
    }
    
    private Disposable bindViewModel() {
        return CompositeDisposable.from(
            // Bind tree nodes
            viewModel.getTreeNodes()
                .observeOn(SwingSchedulers.edt())
                .subscribe(nodes -> {
                    DefaultMutableTreeNode root = buildTreeModel(nodes);
                    tree.setModel(new DefaultTreeModel(root));
                }),
            
            // Bind loading state
            viewModel.getLoadingState()
                .observeOn(SwingSchedulers.edt())
                .subscribe(state -> {
                    loadingLabel.setVisible(state == ProjectLoadingState.LOADING);
                }),
            
            // Bind errors
            viewModel.getErrors()
                .filter(error -> error != null)
                .observeOn(SwingSchedulers.edt())
                .subscribe(error -> {
                    JOptionPane.showErrorDialog(this, 
                        error.getMessage(), 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                })
        );
    }
    
    private void wireMouseActions() {
        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                TreePath path = tree.getPathForLocation(e.getX(), e.getY());
                if (path != null) {
                    Object node = path.getLastPathComponent();
                    if (node instanceof DefaultMutableTreeNode) {
                        DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) node;
                        if (dmtn.getUserObject() instanceof TreeNodeViewModel) {
                            TreeNodeViewModel viewNode = (TreeNodeViewModel) dmtn.getUserObject();
                            viewModel.selectNode(viewNode.getId());
                        }
                    }
                }
            }
        });
    }
    
    private DefaultMutableTreeNode buildTreeModel(TreeNodeViewModel node) {
        DefaultMutableTreeNode dmtn = new DefaultMutableTreeNode(node);
        for (TreeNodeViewModel child : node.getChildren()) {
            dmtn.add(buildTreeModel(child));
        }
        return dmtn;
    }
    
    public void cleanup() {
        subscription.dispose();
    }
}

// Entry Point: Main Window (updated)
public class MainWindow extends JFrame {
    private final ApplicationContext appContext;  // Dependency injection
    
    public MainWindow(ApplicationContext appContext) {
        this.appContext = appContext;
        
        setTitle("DexForge GUI");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        
        // Create panels using use cases & view models
        OpenProjectUseCase openProjectUseCase = appContext.getOpenProjectUseCase();
        ProjectExplorerViewModel viewModel = appContext.getProjectExplorerViewModel();
        ProjectExplorerPanel explorerPanel = new ProjectExplorerPanel(viewModel);
        
        setContentPane(explorerPanel);
        setVisible(true);
    }
}
```

---

### Phase 5: Infrastructure Adapters (Weeks 11-12)

**Goal**: Implement concrete adapters for ports

**Tasks:**
1. Create JADX decompiler adapter (implements DecompileEngine port)
2. Create cache adapter (implements CachePort)
3. Create search adapter (Lucene, etc.)
4. Create notification adapter (Swing notifications)
5. Migrate event bus implementation
6. Create dependency injection container (ApplicationContext)

**Deliverables:**
- Adapter implementations
- Configuration/wiring
- 50%+ test coverage
- No direct Swing dependencies in adapters

**Example Adapter (Phase 5):**

```java
// Infrastructure: JADX Adapter
@Adapter
public class JadxDecompileEngineAdapter implements DecompileEnginePort {
    private final JadxDecompiler decompiler; // Upstream JADX
    
    public JadxDecompileEngineAdapter(JadxDecompiler decompiler) {
        this.decompiler = decompiler;
    }
    
    @Override
    public DecompileResult decompile(SourceFileRef sourceFileRef) throws DecompilationException {
        try {
            JavaClass javaClass = decompiler.decompile(sourceFileRef.getClassPath());
            String code = javaClass.getCodeAsString();
            
            return DecompileResult.success(code);
        } catch (Exception e) {
            return DecompileResult.failure("Failed to decompile", e);
        }
    }
    
    @Override
    public void loadProject(ProjectPath projectPath) throws ProjectLoadException {
        // Delegate to JADX
        decompiler.loadProject(projectPath.getPath());
    }
    
    @Override
    public ProjectMetadata getProjectMetadata(ProjectPath projectPath) throws ProjectLoadException {
        // Convert JADX metadata to domain model
        var jadxProject = decompiler.getProject();
        return ProjectMetadata.of(
            jadxProject.getName(),
            jadxProject.getClasses().size(),
            jadxProject.getResources().size()
        );
    }
}

// Infrastructure: Dependency Injection
@Configuration
public class ApplicationContext {
    private final ProjectPath projectPath;
    private final CacheAdapter cacheAdapter;
    private final JadxDecompileEngineAdapter decompileAdapter;
    private final EventBus eventBus;
    
    public ApplicationContext(ProjectPath projectPath) {
        this.projectPath = projectPath;
        this.cacheAdapter = new FileCacheAdapter();
        this.eventBus = new SimpleEventBus();
        
        // Initialize JADX
        JadxDecompiler decompiler = new JadxDecompiler();
        this.decompileAdapter = new JadxDecompileEngineAdapter(decompiler);
    }
    
    // Use cases
    public OpenProjectUseCase getOpenProjectUseCase() {
        return new OpenProjectUseCase(
            getProjectRepository(),
            getNotificationPort(),
            eventBus
        );
    }
    
    public SearchCodeUseCase getSearchCodeUseCase() {
        return new SearchCodeUseCase(
            getDomainSearchService(),
            getSearchRepository(),
            getNotificationPort(),
            eventBus
        );
    }
    
    // Domain services
    private SearchService getDomainSearchService() {
        return new SearchService(decompileAdapter, cacheAdapter);
    }
    
    // Ports (repositories)
    private ProjectRepository getProjectRepository() {
        return new JadxProjectRepository(decompileAdapter, cacheAdapter);
    }
    
    private SearchRepository getSearchRepository() {
        return new CachedSearchRepository(cacheAdapter);
    }
    
    // Infrastructure
    private NotificationPort getNotificationPort() {
        return new SwingNotificationPort();
    }
    
    // View models
    public ProjectExplorerViewModel getProjectExplorerViewModel() {
        return new ProjectExplorerViewModel(
            new GetProjectTreeUseCase(getProjectRepository(), eventBus)
        );
    }
}
```

---

## Bagian 4: SOLID Principles Implementation

### Single Responsibility Principle (SRP)

**❌ Bad (Mixed concerns):**
```java
public class ProjectPanel extends JPanel {
    public void loadProject(File file) {
        // Loading logic
        JadxProject project = new JadxProject();
        project.load(file);
        
        // Decompilation logic
        for (JavaClass javaClass : project.getClasses()) {
            String code = javaClass.getCodeAsString();
        }
        
        // UI rendering logic
        tree.removeAll();
        tree.add(createNodes(project));
        repaint();
    }
}
```

**✅ Good (Separated concerns):**
```java
// Domain service: handles decompilation logic
public class ProjectService {
    public Project loadAndAnalyze(ProjectPath path) {
        // Only project loading
        Project project = Project.load(path);
        return project;
    }
}

// Use case: orchestrates
public class LoadProjectUseCase {
    public void execute(LoadProjectRequest request) {
        Project project = projectService.loadAndAnalyze(request.getPath());
        repository.save(project);
        eventBus.publish(new ProjectLoadedEvent(project));
    }
}

// UI: only rendering
public class ProjectPanel extends JPanel {
    public void displayProject(Project project) {
        tree.removeAll();
        tree.add(createNodes(project));
        repaint();
    }
}
```

### Open/Closed Principle (OCP)

**❌ Bad (Must modify to add feature):**
```java
public class ReportGenerator {
    public void generate(String format) {
        if ("pdf".equals(format)) {
            // PDF logic
        } else if ("html".equals(format)) {
            // HTML logic
        } else if ("xml".equals(format)) {
            // XML logic
        }
        // Adding new format requires modifying this class
    }
}
```

**✅ Good (Open for extension, closed for modification):**
```java
public interface ReportFormatter {
    String format(Report report);
}

public class PdfReportFormatter implements ReportFormatter {
    @Override
    public String format(Report report) {
        // PDF logic
    }
}

public class HtmlReportFormatter implements ReportFormatter {
    @Override
    public String format(Report report) {
        // HTML logic
    }
}

public class ReportGenerator {
    private final Map<String, ReportFormatter> formatters;
    
    public ReportGenerator(Map<String, ReportFormatter> formatters) {
        this.formatters = formatters;
    }
    
    public void generate(String format, Report report) {
        ReportFormatter formatter = formatters.get(format);
        if (formatter == null) throw new IllegalArgumentException("Unknown format");
        return formatter.format(report);
    }
}

// To add new format: just implement ReportFormatter, no modification needed
```

### Liskov Substitution Principle (LSP)

**❌ Bad (Violates contract):**
```java
public interface DecompileEngine {
    String decompile(String classpath);
}

public class JadxDecompileEngine implements DecompileEngine {
    @Override
    public String decompile(String classpath) {
        // Returns null if failed (violates contract)
        return null;
    }
}
```

**✅ Good (Honors contract):**
```java
public interface DecompileEngine {
    DecompileResult decompile(String classpath);
}

public class DecompileResult {
    private final String code;
    private final Exception error;
    private final boolean success;
    
    public static DecompileResult success(String code) {
        return new DecompileResult(code, null, true);
    }
    
    public static DecompileResult failure(Exception error) {
        return new DecompileResult(null, error, false);
    }
}

public class JadxDecompileEngine implements DecompileEngine {
    @Override
    public DecompileResult decompile(String classpath) {
        try {
            String code = /* decompile */;
            return DecompileResult.success(code);
        } catch (Exception e) {
            return DecompileResult.failure(e);
        }
    }
}
```

### Interface Segregation Principle (ISP)

**❌ Bad (Fat interface):**
```java
public interface ProjectManager {
    void openProject(String path);
    void closeProject();
    void decompileClass(String className);
    void generateReport(String format);
    void exportAPK(String path);
    void analyzeSecurity();
}
```

**✅ Good (Segregated interfaces):**
```java
public interface ProjectRepository {
    void open(String path);
    void close();
}

public interface DecompileService {
    String decompile(String className);
}

public interface ReportService {
    void generate(String format);
}

public interface ExportService {
    void exportAPK(String path);
}

public interface SecurityAnalysisService {
    void analyze();
}
```

### Dependency Inversion Principle (DIP)

**❌ Bad (Depends on concrete classes):**
```java
public class ProjectExplorer {
    private JadxDecompiler decompiler = new JadxDecompiler();
    private FileCacheManager cache = new FileCacheManager();
    
    public void explore() {
        // Tightly coupled to concrete implementations
    }
}
```

**✅ Good (Depends on abstractions):**
```java
public class ProjectExplorer {
    private final DecompileEnginePort decompiler;
    private final CachePort cache;
    
    public ProjectExplorer(DecompileEnginePort decompiler, CachePort cache) {
        this.decompiler = decompiler;
        this.cache = cache;
    }
    
    public void explore() {
        // Depends on ports, not concrete classes
    }
}

// Wiring is done by DI container
var decompiler = new JadxDecompileEngineAdapter(...);
var cache = new FileCacheAdapter(...);
var explorer = new ProjectExplorer(decompiler, cache);
```

---

## Bagian 5: DRY & Reusability Strategy

### Shared Components Library

```
dexforge-gui/src/main/java/dexforge/gui/
├── presentation/
│   ├── swing/components/        # Reusable components
│   │   ├── JTreeBuilder.java    # Tree model building
│   │   ├── SearchableTable.java # Table with search
│   │   ├── CodeEditor.java      # Code highlighting
│   │   ├── DialogBuilder.java   # Dialog factory
│   │   └── NotificationCenter.java
│   └── ...
```

**Example Reusable Component (DRY):**

```java
// Reusable: Code editor with syntax highlighting
public class DexForgeCodeEditor extends JPanel {
    private final RSyntaxTextArea textArea;
    private final RTextScrollPane scrollPane;
    
    public DexForgeCodeEditor() {
        setLayout(new BorderLayout());
        
        textArea = new RSyntaxTextArea(20, 60);
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
        textArea.setCodeFoldingEnabled(true);
        textArea.setAntiAliasingEnabled(true);
        textArea.setFont(new Font("Courier New", Font.PLAIN, 12));
        
        scrollPane = new RTextScrollPane(textArea);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    public void setCode(String code) {
        textArea.setText(code);
    }
    
    public String getCode() {
        return textArea.getText();
    }
    
    public void setSyntaxStyle(String style) {
        textArea.setSyntaxEditingStyle(style);
    }
}

// Used in multiple places without duplication
public class ClassViewerPanel {
    private final DexForgeCodeEditor editor = new DexForgeCodeEditor();
    
    public void displayClass(ClassDefinition classDef) {
        editor.setCode(classDef.getDecompiledCode());
        editor.setSyntaxStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
    }
}

public class MethodViewerPanel {
    private final DexForgeCodeEditor editor = new DexForgeCodeEditor();
    
    public void displayMethod(MethodSignature method) {
        editor.setCode(method.getCode());
        editor.setSyntaxStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
    }
}
```

### Cross-Cutting Concerns (Shared Logic)

```java
// Shared: Logging aspect
@Aspect
public class LoggingAspect {
    @Around("execution(* dexforge..usecase.*.execute(..))")
    public Object logUseCase(ProceedingJoinPoint joinPoint) throws Throwable {
        String useCaseName = joinPoint.getTarget().getClass().getSimpleName();
        LOG.info("Executing: {}", useCaseName);
        long start = System.currentTimeMillis();
        
        try {
            return joinPoint.proceed();
        } finally {
            long duration = System.currentTimeMillis() - start;
            LOG.info("Completed: {} in {}ms", useCaseName, duration);
        }
    }
}

// Shared: Error handling
@Aspect
public class ErrorHandlingAspect {
    @Around("execution(* dexforge..usecase.*.execute(..))")
    public Object handleErrors(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (BusinessException e) {
            LOG.error("Business error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            LOG.error("Unexpected error", e);
            throw new UnexpectedException("Operation failed", e);
        }
    }
}

// Shared: Caching
@Aspect
public class CachingAspect {
    @Around("@annotation(dexforge.infrastructure.annotation.Cacheable)")
    public Object cache(ProceedingJoinPoint joinPoint) throws Throwable {
        String key = generateCacheKey(joinPoint);
        Object cached = cacheManager.get(key);
        
        if (cached != null) {
            return cached;
        }
        
        Object result = joinPoint.proceed();
        cacheManager.put(key, result);
        return result;
    }
}
```

---

## Bagian 6: Scalability Strategy

### Feature Modularity (Plugin System)

```
dexforge-gui/
├── core/                        # Core platform
│   ├── domain/
│   ├── application/
│   └── presentation/
│
└── plugins/                     # Feature plugins (optional)
    ├── device-explorer/         # Device integration (optional)
    ├── frida-integration/        # Frida hooks (optional)
    ├── security-scanner/        # Security analysis (optional)
    └── custom-analyzer/         # User plugin template
```

**Plugin Interface:**

```java
// Core: Plugin contract
public interface DexForgePlugin {
    String getName();
    String getVersion();
    List<MenuItem> getMenuItems();
    List<ToolPanel> getToolPanels();
    void activate(PluginContext context);
    void deactivate();
}

// Plugin: Device Explorer
public class DeviceExplorerPlugin implements DexForgePlugin {
    @Override
    public String getName() {
        return "DexForge Device Explorer";
    }
    
    @Override
    public List<MenuItem> getMenuItems() {
        return List.of(
            new MenuItem("File", "Open from Android Device...", 
                event -> showDeviceSelector())
        );
    }
    
    @Override
    public List<ToolPanel> getToolPanels() {
        return List.of(new DeviceExplorerPanel());
    }
    
    @Override
    public void activate(PluginContext context) {
        LOG.info("Device Explorer plugin activated");
    }
}

// Core: Plugin loader
public class PluginManager {
    private final List<DexForgePlugin> plugins = new ArrayList<>();
    
    public void loadPlugin(String className) throws Exception {
        ClassLoader loader = PluginManager.class.getClassLoader();
        Class<?> clazz = loader.loadClass(className);
        
        if (!DexForgePlugin.class.isAssignableFrom(clazz)) {
            throw new IllegalArgumentException("Not a plugin: " + className);
        }
        
        DexForgePlugin plugin = (DexForgePlugin) clazz.getDeclaredConstructor().newInstance();
        plugin.activate(new PluginContextImpl());
        plugins.add(plugin);
        
        LOG.info("Plugin loaded: {}", plugin.getName());
    }
    
    public List<DexForgePlugin> getPlugins() {
        return Collections.unmodifiableList(plugins);
    }
}
```

### Event-Driven Architecture (Scalability)

```java
// Domain: Events
public interface DomainEvent {
    String getEventType();
    LocalDateTime getOccurredAt();
}

public class ClassDecompiledEvent implements DomainEvent {
    private final ClassId classId;
    private final String code;
    private final LocalDateTime occurredAt = LocalDateTime.now();
    
    @Override
    public String getEventType() {
        return "ClassDecompiled";
    }
}

// Infrastructure: Event Bus
public class EventBus {
    private final Map<String, List<EventListener>> listeners = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    
    public void subscribe(String eventType, EventListener listener) {
        listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
                 .add(listener);
    }
    
    public void publish(DomainEvent event) {
        List<EventListener> eventListeners = listeners.get(event.getEventType());
        if (eventListeners != null) {
            eventListeners.forEach(listener -> 
                executor.execute(() -> {
                    try {
                        listener.handle(event);
                    } catch (Exception e) {
                        LOG.error("Error handling event", e);
                    }
                })
            );
        }
    }
}

// Scalable subscribers
public class SearchIndexer implements EventListener {
    @Override
    public void handle(DomainEvent event) {
        if (event instanceof ClassDecompiledEvent) {
            ClassDecompiledEvent cevent = (ClassDecompiledEvent) event;
            // Index the decompiled code for search
            searchIndex.index(cevent.getClassId(), cevent.getCode());
        }
    }
}

public class SecurityAnalyzer implements EventListener {
    @Override
    public void handle(DomainEvent event) {
        if (event instanceof ClassDecompiledEvent) {
            ClassDecompiledEvent cevent = (ClassDecompiledEvent) event;
            // Analyze for security issues
            SecurityFindings findings = analyzer.analyze(cevent.getCode());
            notificationBus.notify(findings);
        }
    }
}

// Register subscribers
eventBus.subscribe("ClassDecompiled", new SearchIndexer());
eventBus.subscribe("ClassDecompiled", new SecurityAnalyzer());
```

---

## Bagian 7: Migration Checklist

- [ ] Phase 0: Foundation (Weeks 1-2)
  - [ ] Setup module structure
  - [ ] Create base domain classes
  - [ ] Implement event infrastructure
  
- [ ] Phase 1: Domain Model (Weeks 3-4)
  - [ ] Create Project aggregate
  - [ ] Create SourceFile aggregate
  - [ ] Create domain services
  - [ ] Write domain tests (80%+ coverage)
  
- [ ] Phase 2: Application Layer (Weeks 5-6)
  - [ ] Create 10-15 use cases
  - [ ] Define DTOs
  - [ ] Define port interfaces
  - [ ] Write application tests (70%+ coverage)
  
- [ ] Phase 3: Presentation Model (Weeks 7-8)
  - [ ] Create ViewModels
  - [ ] Create mappers
  - [ ] Implement reactive bindings
  - [ ] Write presentation tests (60%+ coverage)
  
- [ ] Phase 4: Swing UI (Weeks 9-10)
  - [ ] Create reusable components
  - [ ] Implement UI bindings
  - [ ] Update MainWindow
  - [ ] Write integration tests (40%+ coverage)
  
- [ ] Phase 5: Adapters (Weeks 11-12)
  - [ ] Create JADX adapter
  - [ ] Create cache adapter
  - [ ] Create search adapter
  - [ ] Setup DI container
  - [ ] Write adapter tests (50%+ coverage)

---

## Bagian 8: Tools & Dependencies

### Required Gradle Dependencies

```kotlin
dependencies {
    // Existing
    implementation(project(":jadx-core"))
    implementation("com.fifesoft:rsyntaxtextarea:3.6.1")
    implementation("com.formdev:flatlaf:3.7")
    implementation("io.reactivex.rxjava3:rxjava:3.1.12")
    
    // New: DDD & Architecture
    implementation("com.google.guava:guava:31.1-jre")           // Immutable collections
    implementation("org.apache.commons:commons-lang3:3.20.0") // Utilities
    
    // New: Spring for DI & AOP (optional, can use manual DI)
    implementation("org.springframework:spring-context:6.1.0")
    implementation("org.springframework:spring-aop:6.1.0")
    implementation("org.aspectj:aspectjweaver:1.9.21")
    
    // New: Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.2.0")
    testImplementation("org.assertj:assertj-core:3.24.1")
}
```

### Development Tools

- **IDE**: IntelliJ IDEA (with Spring, Architecture plugins)
- **Build**: Gradle 8.1+
- **Testing**: JUnit 4/5, Mockito, AssertJ
- **Code Quality**: SonarQube, Checkstyle, SpotBugs
- **Documentation**: Swagger/OpenAPI (for JSON-RPC contracts)

---

## Ringkasan

**Strategi Bertahap:**
1. Foundation infrastructure (events, base classes)
2. Pure domain layer (no UI/framework dependencies)
3. Application orchestration (use cases)
4. Presentation models (ViewModels)
5. Swing UI (data binding)
6. Infrastructure adapters (concrete implementations)

**Key Principles:**
- ✅ Clean separation of concerns
- ✅ SOLID principles applied consistently
- ✅ DDD bounded contexts
- ✅ Testability (layers can be tested independently)
- ✅ Reusability (components & services)
- ✅ Scalability (plugin system, event-driven)
- ✅ Backward compatibility (JADX integration)

**Success Metrics:**
- 80%+ domain test coverage
- All layers have clear responsibilities
- Zero UI logic in domain/application
- New features can be added without modifying existing code
- Plugins can be added/removed independently

---

## Referensi Lebih Lanjut

- Clean Architecture: [Robert C. Martin](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- DDD: [Eric Evans - Domain-Driven Design](https://www.domainlanguage.com/ddd/)
- SOLID Principles: [Uncle Bob SOLID](https://blog.cleancoder.com/uncle-bob/2020/10/18/Solid-Relevance.html)
- Event Sourcing: [Martin Fowler](https://martinfowler.com/eaaDev/EventSourcing.html)
- Reactive Programming: [RxJava Documentation](https://github.com/ReactiveX/RxJava)
