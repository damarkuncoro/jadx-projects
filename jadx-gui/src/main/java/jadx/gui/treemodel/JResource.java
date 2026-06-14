package jadx.gui.treemodel;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;

import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.jetbrains.annotations.Nullable;

import jadx.api.ICodeInfo;
import jadx.api.JadxArgs;
import jadx.api.JadxDecompiler;
import jadx.api.JavaClass;
import jadx.api.ResourceFile;
import jadx.api.ResourceType;
import jadx.api.ResourcesLoader;
import jadx.api.impl.SimpleCodeInfo;
import jadx.api.resources.ResourceContentType;
import jadx.core.utils.ListUtils;
import jadx.core.utils.Utils;
import jadx.core.xmlgen.BinaryXMLParser;
import jadx.core.xmlgen.ResContainer;
import jadx.gui.jobs.SimpleTask;
import jadx.gui.ui.MainWindow;
import jadx.gui.ui.codearea.AbstractCodeArea;
import jadx.gui.ui.codearea.BinaryContentPanel;
import jadx.gui.ui.codearea.CodeContentPanel;
import jadx.gui.ui.panel.ContentPanel;
import jadx.gui.ui.panel.FontPanel;
import jadx.gui.ui.panel.ImagePanel;
import jadx.gui.ui.popupmenu.JResourcePopupMenu;
import jadx.gui.ui.tab.TabbedPane;
import jadx.gui.utils.Icons;
import jadx.gui.utils.NLS;
import jadx.gui.utils.UiUtils;
import jadx.gui.utils.res.ResTableHelper;

public class JResource extends JLoadableNode {
	private static final long serialVersionUID = -201018424302612434L;

	private static final ImageIcon ROOT_ICON = UiUtils.openSvgIcon("nodes/resourcesRoot");
	private static final ImageIcon ARSC_ICON = UiUtils.openSvgIcon("nodes/resourceBundle");
	private static final ImageIcon XML_ICON = UiUtils.openSvgIcon("nodes/xml");
	private static final ImageIcon IMAGE_ICON = UiUtils.openSvgIcon("nodes/ImagesFileType");
	private static final ImageIcon SO_ICON = UiUtils.openSvgIcon("nodes/binaryFile");
	private static final ImageIcon MANIFEST_ICON = UiUtils.openSvgIcon("nodes/manifest");
	private static final ImageIcon JAVA_ICON = UiUtils.openSvgIcon("nodes/java");
	private static final ImageIcon APK_ICON = UiUtils.openSvgIcon("nodes/archiveApk");
	private static final ImageIcon AUDIO_ICON = UiUtils.openSvgIcon("nodes/audioFile");
	private static final ImageIcon VIDEO_ICON = UiUtils.openSvgIcon("nodes/videoFile");
	private static final ImageIcon FONT_ICON = UiUtils.openSvgIcon("nodes/fontFile");
	private static final ImageIcon HTML_ICON = UiUtils.openSvgIcon("nodes/html");
	private static final ImageIcon JSON_ICON = UiUtils.openSvgIcon("nodes/json");
	private static final ImageIcon TEXT_ICON = UiUtils.openSvgIcon("nodes/text");
	private static final ImageIcon ARCHIVE_ICON = UiUtils.openSvgIcon("nodes/archive");
	private static final ImageIcon UNKNOWN_ICON = UiUtils.openSvgIcon("nodes/unknown");

	public static final Comparator<JResource> RESOURCES_COMPARATOR =
			Comparator.<JResource>comparingInt(r -> r.type.ordinal())
					.thenComparing(JResource::getName, String.CASE_INSENSITIVE_ORDER);

	public enum JResType {
		ROOT,
		DIR,
		FILE
	}

	private final transient String name;
	private transient String shortName;
	private final transient JResType type;
	private final transient ResourceFile resFile;

	private transient volatile boolean loaded;
	private transient List<JResource> subNodes = Collections.emptyList();
	private transient ICodeInfo content = ICodeInfo.EMPTY;
	private transient Boolean isImage;
	private transient Boolean isElf;
	private transient Boolean isBinaryXml;

	public JResource(@Nullable ResourceFile resFile, String name, JResType type) {
		this(resFile, name, name, type);
	}

	public JResource(@Nullable ResourceFile resFile, String name, String shortName, JResType type) {
		if (resFile == null && type == JResType.FILE) {
			throw new IllegalArgumentException("Null resource file");
		}
		this.resFile = resFile;
		this.name = name;
		this.shortName = shortName;
		this.type = type;
		this.loaded = false;
	}

	public synchronized void update() {
		removeAllChildren();
		if (Utils.isEmpty(subNodes)) {
			if (type == JResType.DIR || type == JResType.ROOT
					|| resFile.getType() == ResourceType.ARSC) {
				// fake leaf to force show expand button
				// real sub nodes will load on expand in loadNode() method
				add(new TextNode(NLS.str("tree.loading")));
			}
		} else {
			for (JResource res : subNodes) {
				res.update();
				add(res);
			}
			if (type != JResType.FILE) {
				// no content, nothing to load
				loaded = true;
			}
		}
	}

	@Override
	public synchronized void loadNode() {
		getCodeInfo();
		update();
	}

	@Override
	public synchronized SimpleTask getLoadTask() {
		if (loaded) {
			return null;
		}
		return new SimpleTask(NLS.str("progress.load"), this::getCodeInfo, this::update);
	}

	@Override
	public String getName() {
		return name;
	}

	public String getShortName() {
		return shortName;
	}

	public JResType getType() {
		return type;
	}

	public List<JResource> getSubNodes() {
		return subNodes;
	}

	public void addSubNode(JResource node) {
		subNodes = ListUtils.safeAdd(subNodes, node);
	}

	public void sortSubNodes() {
		sortResNodes(subNodes);
	}

	private static void sortResNodes(List<JResource> nodes) {
		if (Utils.notEmpty(nodes)) {
			nodes.forEach(JResource::sortSubNodes);
			nodes.sort(RESOURCES_COMPARATOR);
		}
	}

	/**
	 * Collapse single-child DIR chains into one node with a slash-joined display name (GitHub-style).
	 */
	public static void mergeMiddleDirs(JResource root) {
		mergeChildren(root.subNodes);
	}

	public static void mergeMiddleDirs(List<JResource> roots) {
		mergeChildren(roots);
	}

	private static void mergeChildren(List<JResource> children) {
		for (int i = 0; i < children.size(); i++) {
			JResource sub = children.get(i);
			JResource replaced = mergeChain(sub, new ArrayList<>());
			if (replaced != sub) {
				children.set(i, replaced);
			}
			mergeChildren(replaced.subNodes);
		}
	}

	private static JResource mergeChain(JResource node, List<JResource> merged) {
		if (node.type == JResType.DIR) {
			List<JResource> subs = node.subNodes;
			if (subs.size() == 1 && subs.get(0).type == JResType.DIR) {
				merged.add(node);
				return mergeChain(subs.get(0), merged);
			}
		}
		if (!merged.isEmpty()) {
			merged.add(node);
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < merged.size(); i++) {
				if (i > 0) {
					sb.append('/');
				}
				sb.append(merged.get(i).shortName);
			}
			node.shortName = sb.toString();
		}
		return node;
	}

	@Override
	public boolean hasContent() {
		return resFile != null;
	}

	@Override
	public @Nullable ContentPanel getContentPanel(TabbedPane tabbedPane) {
		if (resFile == null) {
			return null;
		}
		// TODO: allow to register custom viewers
		if (resFile.getType() == ResourceType.IMG || isImageMagic()) {
			return new ImagePanel(tabbedPane, this);
		}
		if (resFile.getType() == ResourceType.FONT) {
			return new FontPanel(tabbedPane, this);
		}
		if (isDexMagic()) {
			return new CodeContentPanel(tabbedPane, this);
		}
		if (isBinaryXmlMagic()) {
			return new CodeContentPanel(tabbedPane, this);
		}
		if (getContentType() == ResourceContentType.CONTENT_BINARY) {
			if (isElfMagic()) {
				return new BinaryContentPanel(tabbedPane, this, true);
			}
			return new BinaryContentPanel(tabbedPane, this, false);
		}
		if (getSyntaxByExtension(resFile.getDeobfName()) != null) {
			return new CodeContentPanel(tabbedPane, this);
		}
		// unknown file type, show both text and binary
		return new BinaryContentPanel(tabbedPane, this, true);
	}

	@Override
	public JPopupMenu onTreePopupMenu(MainWindow mainWindow) {
		return new JResourcePopupMenu(mainWindow, this);
	}

	@Override
	public synchronized ICodeInfo getCodeInfo() {
		if (loaded) {
			return content;
		}
		ICodeInfo codeInfo = loadContent();
		content = codeInfo;
		loaded = true;
		return codeInfo;
	}

	@Override
	public ResourceContentType getContentType() {
		if (type == JResType.FILE) {
			return resFile.getType().getContentType();
		}
		return ResourceContentType.CONTENT_NONE;
	}

	private ICodeInfo loadContent() {
		if (resFile == null || type != JResType.FILE) {
			return ICodeInfo.EMPTY;
		}
		ResContainer rc = resFile.loadContent();
		if (rc == null) {
			return ICodeInfo.EMPTY;
		}
		if (rc.getDataType() == ResContainer.DataType.RES_TABLE) {
			ICodeInfo codeInfo = loadCurrentSingleRes(rc);
			List<JResource> nodes = ResTableHelper.buildTree(this, rc);
			sortResNodes(nodes);
			subNodes = nodes;
			UiUtils.uiRun(this::update);
			return codeInfo;
		}
		// single node
		return loadCurrentSingleRes(rc);
	}

	private ICodeInfo loadCurrentSingleRes(ResContainer rc) {
		if (isDexMagic()) {
			try {
				return ResourcesLoader.decodeStream(this.resFile, (size, is) -> {
					byte[] bytes = is.readAllBytes();
					return new SimpleCodeInfo(decompileDexBuf(bytes, this.resFile.getOriginalName()));
				});
			} catch (Exception e) {
				return new SimpleCodeInfo("Failed to decompile DEX resource:\n" + Utils.getStackTrace(e));
			}
		}
		switch (rc.getDataType()) {
			case TEXT:
			case RES_TABLE:
				return rc.getText();

			case RES_LINK:
				try {
					ResourceFile resourceFile = rc.getResLink();
					if (isElfMagic()) {
						return ResourcesLoader.decodeStream(resourceFile, (size, is) -> {
							byte[] bytes = new byte[64];
							int read = is.readNBytes(bytes, 0, 64);
							if (read < 64) {
								byte[] truncated = new byte[read];
								System.arraycopy(bytes, 0, truncated, 0, read);
								bytes = truncated;
							}
							return new SimpleCodeInfo(parseElfHeader(bytes));
						});
					}
					if (isBinaryXmlMagic()) {
						return ResourcesLoader.decodeStream(resourceFile, (size, is) -> {
							BinaryXMLParser parser = new BinaryXMLParser(resourceFile.getDecompiler().getRoot());
							return parser.parse(is);
						});
					}
					return ResourcesLoader.decodeStream(resourceFile, (size, is) -> {
						// TODO: check size before loading
						if (size > 10 * 1024 * 1024L) {
							return new SimpleCodeInfo("File too large for view");
						}
						Charset charset;
						if (resourceFile.getType().getContentType() == ResourceContentType.CONTENT_TEXT) {
							charset = StandardCharsets.UTF_8;
						} else {
							// force one byte charset for binary data to have the same offsets as in a byte array
							charset = StandardCharsets.US_ASCII;
						}
						return ResourcesLoader.loadToCodeWriter(is, charset);
					});
				} catch (Exception e) {
					return new SimpleCodeInfo("Failed to load resource file:\n" + Utils.getStackTrace(e));
				}

			case DECODED_DATA:
			default:
				return new SimpleCodeInfo("Unexpected resource type: " + rc);
		}
	}

	@Override
	public String getSyntaxName() {
		if (resFile == null) {
			return null;
		}
		if (isDexMagic()) {
			return SyntaxConstants.SYNTAX_STYLE_JAVA;
		}
		if (isBinaryXmlMagic()) {
			return SyntaxConstants.SYNTAX_STYLE_XML;
		}
		switch (resFile.getType()) {
			case CODE:
				return super.getSyntaxName();

			case MANIFEST:
			case XML:
			case ARSC:
				return SyntaxConstants.SYNTAX_STYLE_XML;

			default:
				String syntax = getSyntaxByExtension(resFile.getDeobfName());
				if (syntax != null) {
					return syntax;
				}
				return super.getSyntaxName();
		}
	}

	private static final Map<String, String> EXTENSION_TO_FILE_SYNTAX = jadx.core.utils.Utils.newConstStringMap(
			"java", SyntaxConstants.SYNTAX_STYLE_JAVA,
			"smali", AbstractCodeArea.SYNTAX_STYLE_SMALI,
			"js", SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT,
			"ts", SyntaxConstants.SYNTAX_STYLE_TYPESCRIPT,
			"json", SyntaxConstants.SYNTAX_STYLE_JSON,
			"css", SyntaxConstants.SYNTAX_STYLE_CSS,
			"less", SyntaxConstants.SYNTAX_STYLE_LESS,
			"html", SyntaxConstants.SYNTAX_STYLE_HTML,
			"xml", SyntaxConstants.SYNTAX_STYLE_XML,
			"yaml", SyntaxConstants.SYNTAX_STYLE_YAML,
			"properties", SyntaxConstants.SYNTAX_STYLE_PROPERTIES_FILE,
			"ini", SyntaxConstants.SYNTAX_STYLE_INI,
			"sql", SyntaxConstants.SYNTAX_STYLE_SQL);

	private String getSyntaxByExtension(String name) {
		int dot = name.lastIndexOf('.');
		if (dot == -1) {
			return null;
		}
		String ext = name.substring(dot + 1);
		return EXTENSION_TO_FILE_SYNTAX.get(ext);
	}

	@Override
	public Icon getIcon() {
		switch (type) {
			case ROOT:
				return ROOT_ICON;
			case DIR:
				return Icons.FOLDER;

			case FILE:
				if (isImageMagic()) {
					return IMAGE_ICON;
				}
				if (isDexMagic()) {
					return JAVA_ICON;
				}
				if (isElfMagic()) {
					return SO_ICON;
				}
				if (isBinaryXmlMagic()) {
					return XML_ICON;
				}
				ResourceType resType = resFile.getType();
				switch (resType) {
					case MANIFEST:
						return MANIFEST_ICON;
					case ARSC:
						return ARSC_ICON;
					case XML:
						return XML_ICON;
					case IMG:
						return IMAGE_ICON;
					case LIB:
						return SO_ICON;
					case CODE:
						return JAVA_ICON;
					case APK:
						return APK_ICON;
					case VIDEOS:
						return VIDEO_ICON;
					case SOUNDS:
						return AUDIO_ICON;
					case FONT:
						return FONT_ICON;
					case HTML:
						return HTML_ICON;
					case JSON:
						return JSON_ICON;
					case TEXT:
						return TEXT_ICON;
					case ARCHIVE:
						return ARCHIVE_ICON;
					case UNKNOWN:
						return UNKNOWN_ICON;
				}
				return UNKNOWN_ICON;
		}
		return Icons.FILE;
	}

	public static boolean isSupportedForView(ResourceType type) {
		switch (type) {
			case SOUNDS:
			case VIDEOS:
			case ARCHIVE:
			case APK:
				return false;

			case MANIFEST:
			case XML:
			case ARSC:
			case IMG:
			case LIB:
			case FONT:
			case TEXT:
			case JSON:
			case HTML:
			case UNKNOWN:
				return true;
		}
		return true;
	}

	public static boolean isOpenInExternalTool(ResourceType type) {
		switch (type) {
			case SOUNDS:
			case VIDEOS:
				return true;
			default:
				return false;
		}
	}

	public ResourceFile getResFile() {
		return resFile;
	}

	@Override
	public JClass getJParent() {
		return null;
	}

	@Override
	public String getID() {
		if (type == JResType.ROOT) {
			return "JResources";
		}
		return makeString();
	}

	@Override
	public String makeString() {
		return shortName;
	}

	@Override
	public String makeLongString() {
		return name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		JResource other = (JResource) o;
		return name.equals(other.name) && type.equals(other.type);
	}

	@Override
	public int hashCode() {
		return name.hashCode() + 31 * type.ordinal();
	}

	public boolean isImageMagic() {
		if (resFile == null || type != JResType.FILE) {
			return false;
		}
		if (isImage != null) {
			return isImage;
		}
		try {
			boolean detect = ResourcesLoader.decodeStream(resFile, (size, is) -> {
				byte[] header = new byte[12];
				int read = is.readNBytes(header, 0, 12);
				if (read < 3) {
					return false;
				}
				// PNG: 89 50 4E 47 0D 0A 1A 0A
				if (read >= 8
						&& header[0] == (byte) 0x89 && header[1] == (byte) 0x50
						&& header[2] == (byte) 0x4E && header[3] == (byte) 0x47
						&& header[4] == (byte) 0x0D && header[5] == (byte) 0x0A
						&& header[6] == (byte) 0x1A && header[7] == (byte) 0x0A) {
					return true;
				}
				// JPEG: FF D8 FF
				if (header[0] == (byte) 0xFF && header[1] == (byte) 0xD8 && header[2] == (byte) 0xFF) {
					return true;
				}
				// GIF: GIF89a (47 49 46 38 39 61) / GIF87a (47 49 46 38 37 61)
				if (read >= 6
						&& header[0] == (byte) 'G' && header[1] == (byte) 'I' && header[2] == (byte) 'F'
						&& header[3] == (byte) '8' && (header[4] == (byte) '9' || header[4] == (byte) '7')
						&& header[5] == (byte) 'a') {
					return true;
				}
				// WebP: RIFF (52 49 46 46) ... WEBP (57 45 42 50)
				if (read >= 12
						&& header[0] == (byte) 'R' && header[1] == (byte) 'I' && header[2] == (byte) 'F' && header[3] == (byte) 'F'
						&& header[8] == (byte) 'W' && header[9] == (byte) 'E' && header[10] == (byte) 'B' && header[11] == (byte) 'P') {
					return true;
				}
				// BMP: BM (42 4D)
				if (header[0] == (byte) 'B' && header[1] == (byte) 'M') {
					return true;
				}
				return false;
			});
			isImage = detect;
			return detect;
		} catch (Exception e) {
			isImage = false;
			return false;
		}
	}

	public boolean isElfMagic() {
		if (resFile == null || type != JResType.FILE) {
			return false;
		}
		if (isElf != null) {
			return isElf;
		}
		try {
			boolean detect = ResourcesLoader.decodeStream(resFile, (size, is) -> {
				byte[] header = new byte[4];
				int read = is.readNBytes(header, 0, 4);
				return read == 4 && header[0] == 0x7f && header[1] == 'E' && header[2] == 'L' && header[3] == 'F';
			});
			isElf = detect;
			return detect;
		} catch (Exception e) {
			isElf = false;
			return false;
		}
	}

	public boolean isBinaryXmlMagic() {
		if (resFile == null || type != JResType.FILE) {
			return false;
		}
		if (isBinaryXml != null) {
			return isBinaryXml;
		}
		try {
			boolean detect = ResourcesLoader.decodeStream(resFile, (size, is) -> {
				byte[] header = new byte[4];
				int read = is.readNBytes(header, 0, 4);
				if (read < 4) {
					return false;
				}
				return (header[2] == 0x08 && header[3] == 0x00)
						&& (header[1] == 0x00)
						&& (header[0] == 0x03 || header[0] == 0x00);
			});
			isBinaryXml = detect;
			return detect;
		} catch (Exception e) {
			isBinaryXml = false;
			return false;
		}
	}

	public boolean isDexMagic() {
		if (resFile == null || type != JResType.FILE) {
			return false;
		}
		try {
			boolean detect = ResourcesLoader.decodeStream(resFile, (size, is) -> {
				byte[] header = new byte[8];
				int read = is.readNBytes(header, 0, 8);
				if (read < 8) {
					return false;
				}
				return header[0] == 'd' && header[1] == 'e' && header[2] == 'x' && header[3] == '\n'
						&& header[4] >= '0' && header[4] <= '9'
						&& header[5] >= '0' && header[5] <= '9'
						&& header[6] >= '0' && header[6] <= '9'
						&& header[7] == 0;
			});
			return detect;
		} catch (Exception e) {
			return false;
		}
	}

	private String decompileDexBuf(byte[] dexBuf, String name) {
		JadxArgs jadxArgs = new JadxArgs();
		jadxArgs.setInputFiles(Collections.emptyList());
		try {
			jadxArgs.setSkipSources(false);
			jadxArgs.setSkipResources(true);
			jadxArgs.getPluginOptions().put("dex-input.verify-checksum", "false");
		} catch (Exception e) {
			// ignore
		}
		try (JadxDecompiler decompiler = new JadxDecompiler(jadxArgs)) {
			try {
				Class<?> pluginClass = Class.forName("jadx.plugins.input.dex.DexInputPlugin");
				Object pluginInstance = pluginClass.getConstructor().newInstance();
				java.lang.reflect.Method loadDexMethod = pluginClass.getMethod("loadDex", byte[].class, String.class);
				jadx.api.plugins.input.ICodeLoader codeLoader =
						(jadx.api.plugins.input.ICodeLoader) loadDexMethod.invoke(pluginInstance, dexBuf, name);
				decompiler.addCustomCodeLoader(codeLoader);
			} catch (Exception e) {
				return "Failed to load DEX input plugin: " + Utils.getStackTrace(e);
			}
			decompiler.load();
			StringBuilder sb = new StringBuilder();
			sb.append("// Decompiled from DEX resource: ").append(name).append("\n\n");
			List<JavaClass> classes = decompiler.getClasses();
			if (classes.isEmpty()) {
				sb.append("// No classes found in this DEX file.\n");
			} else {
				for (JavaClass cls : classes) {
					sb.append("// -----------------------------------------------------\n");
					sb.append("// Class: ").append(cls.getFullName()).append("\n");
					sb.append("// -----------------------------------------------------\n");
					try {
						sb.append(cls.getCode());
					} catch (Exception e) {
						sb.append("// Failed to decompile class: ").append(cls.getFullName()).append("\n");
						sb.append("// ").append(Utils.getStackTrace(e)).append("\n");
					}
					sb.append("\n\n");
				}
			}
			return sb.toString();
		} catch (Exception e) {
			return "Failed to decompile DEX resource: " + Utils.getStackTrace(e);
		}
	}

	public static String parseElfHeader(byte[] bytes) {
		if (bytes.length < 52) {
			return "Invalid ELF file: too short (" + bytes.length + " bytes)";
		}
		if (bytes[0] != 0x7f || bytes[1] != 'E' || bytes[2] != 'L' || bytes[3] != 'F') {
			return "Not an ELF file";
		}

		StringBuilder sb = new StringBuilder();
		sb.append("=== ELF File Header Info ===\n\n");

		// Class (32-bit vs 64-bit)
		int elfClass = bytes[4] & 0xFF;
		String classStr;
		if (elfClass == 1) {
			classStr = "ELF32 (32-bit)";
		} else if (elfClass == 2) {
			classStr = "ELF64 (64-bit)";
		} else {
			classStr = "Unknown (" + elfClass + ")";
		}
		sb.append(String.format("%-30s : %s\n", "Class", classStr));

		// Data (Endianness)
		int elfData = bytes[5] & 0xFF;
		boolean isLittleEndian = (elfData == 1);
		String dataStr;
		if (elfData == 1) {
			dataStr = "2's complement, little endian";
		} else if (elfData == 2) {
			dataStr = "2's complement, big endian";
		} else {
			dataStr = "Unknown (" + elfData + ")";
		}
		sb.append(String.format("%-30s : %s\n", "Data Indicator", dataStr));

		// Version
		int version = bytes[6] & 0xFF;
		sb.append(String.format("%-30s : %d %s\n", "ELF Version", version, version == 1 ? "(current)" : ""));

		// OS/ABI
		int osAbi = bytes[7] & 0xFF;
		String osAbiStr;
		switch (osAbi) {
			case 0:
				osAbiStr = "UNIX - System V";
				break;
			case 3:
				osAbiStr = "UNIX - GNU/Linux";
				break;
			case 6:
				osAbiStr = "UNIX - Solaris";
				break;
			case 9:
				osAbiStr = "UNIX - FreeBSD";
				break;
			case 255:
				osAbiStr = "Standalone (embedded) application";
				break;
			default:
				osAbiStr = "Unknown (" + osAbi + ")";
				break;
		}
		sb.append(String.format("%-30s : %s\n", "OS/ABI", osAbiStr));

		// ABI Version
		int abiVer = bytes[8] & 0xFF;
		sb.append(String.format("%-30s : %d\n", "ABI Version", abiVer));

		// Type
		int type = read2Bytes(bytes, 16, isLittleEndian);
		String typeStr;
		switch (type) {
			case 1:
				typeStr = "REL (Relocatable file)";
				break;
			case 2:
				typeStr = "EXEC (Executable file)";
				break;
			case 3:
				typeStr = "DYN (Shared object / Shared library)";
				break;
			case 4:
				typeStr = "CORE (Core file)";
				break;
			default:
				typeStr = "Unknown (" + type + ")";
				break;
		}
		sb.append(String.format("%-30s : %s\n", "Type", typeStr));

		// Machine
		int machine = read2Bytes(bytes, 18, isLittleEndian);
		String machineStr;
		switch (machine) {
			case 0x03:
				machineStr = "Intel 80386 (x86)";
				break;
			case 0x28:
				machineStr = "ARM (32-bit)";
				break;
			case 0x3E:
				machineStr = "AMD x86-64 (64-bit)";
				break;
			case 0xB7:
				machineStr = "AArch64 (ARM 64-bit)";
				break;
			case 0x2F:
				machineStr = "Intel IA-64";
				break;
			case 0x14:
				machineStr = "PowerPC";
				break;
			case 0x2A:
				machineStr = "SuperH";
				break;
			case 0x08:
				machineStr = "MIPS";
				break;
			default:
				machineStr = "Unknown (" + String.format("0x%02X", machine) + ")";
				break;
		}
		sb.append(String.format("%-30s : %s\n", "Machine Architecture", machineStr));

		// Object Version
		long objVersion = read4Bytes(bytes, 20, isLittleEndian);
		sb.append(String.format("%-30s : %d\n", "Object File Version", objVersion));

		// Entry point & Offsets
		if (elfClass == 1) {
			long entry = read4Bytes(bytes, 24, isLittleEndian);
			long phoff = read4Bytes(bytes, 28, isLittleEndian);
			long shoff = read4Bytes(bytes, 32, isLittleEndian);
			long flags = read4Bytes(bytes, 36, isLittleEndian);
			int ehsize = read2Bytes(bytes, 40, isLittleEndian);
			int phentsize = read2Bytes(bytes, 42, isLittleEndian);
			int phnum = read2Bytes(bytes, 44, isLittleEndian);
			int shentsize = read2Bytes(bytes, 46, isLittleEndian);
			int shnum = read2Bytes(bytes, 48, isLittleEndian);
			int shstrndx = read2Bytes(bytes, 50, isLittleEndian);

			sb.append(String.format("%-30s : 0x%08X\n", "Entry Point Address", entry));
			sb.append(String.format("%-30s : %d (bytes from file start)\n", "Start of Program Headers", phoff));
			sb.append(String.format("%-30s : %d (bytes from file start)\n", "Start of Section Headers", shoff));
			sb.append(String.format("%-30s : 0x%X\n", "Flags", flags));
			sb.append(String.format("%-30s : %d (bytes)\n", "Size of this Header", ehsize));
			sb.append(String.format("%-30s : %d (bytes)\n", "Size of Program Headers", phentsize));
			sb.append(String.format("%-30s : %d\n", "Number of Program Headers", phnum));
			sb.append(String.format("%-30s : %d (bytes)\n", "Size of Section Headers", shentsize));
			sb.append(String.format("%-30s : %d\n", "Number of Section Headers", shnum));
			sb.append(String.format("%-30s : %d\n", "Section Header String Table Index", shstrndx));
		} else if (elfClass == 2) {
			if (bytes.length < 64) {
				return sb.toString() + "\n[Error: ELF64 file header is truncated]";
			}
			long entry = read8Bytes(bytes, 24, isLittleEndian);
			long phoff = read8Bytes(bytes, 32, isLittleEndian);
			long shoff = read8Bytes(bytes, 40, isLittleEndian);
			long flags = read4Bytes(bytes, 48, isLittleEndian);
			int ehsize = read2Bytes(bytes, 52, isLittleEndian);
			int phentsize = read2Bytes(bytes, 54, isLittleEndian);
			int phnum = read2Bytes(bytes, 56, isLittleEndian);
			int shentsize = read2Bytes(bytes, 58, isLittleEndian);
			int shnum = read2Bytes(bytes, 60, isLittleEndian);
			int shstrndx = read2Bytes(bytes, 62, isLittleEndian);

			sb.append(String.format("%-30s : 0x%016X\n", "Entry Point Address", entry));
			sb.append(String.format("%-30s : %d (bytes from file start)\n", "Start of Program Headers", phoff));
			sb.append(String.format("%-30s : %d (bytes from file start)\n", "Start of Section Headers", shoff));
			sb.append(String.format("%-30s : 0x%X\n", "Flags", flags));
			sb.append(String.format("%-30s : %d (bytes)\n", "Size of this Header", ehsize));
			sb.append(String.format("%-30s : %d (bytes)\n", "Size of Program Headers", phentsize));
			sb.append(String.format("%-30s : %d\n", "Number of Program Headers", phnum));
			sb.append(String.format("%-30s : %d (bytes)\n", "Size of Section Headers", shentsize));
			sb.append(String.format("%-30s : %d\n", "Number of Section Headers", shnum));
			sb.append(String.format("%-30s : %d\n", "Section Header String Table Index", shstrndx));
		}

		return sb.toString();
	}

	private static int read2Bytes(byte[] bytes, int offset, boolean le) {
		int b1 = bytes[offset] & 0xFF;
		int b2 = bytes[offset + 1] & 0xFF;
		return le ? (b2 << 8 | b1) : (b1 << 8 | b2);
	}

	private static long read4Bytes(byte[] bytes, int offset, boolean le) {
		long b1 = bytes[offset] & 0xFF;
		long b2 = bytes[offset + 1] & 0xFF;
		long b3 = bytes[offset + 2] & 0xFF;
		long b4 = bytes[offset + 3] & 0xFF;
		return le ? (b4 << 24 | b3 << 16 | b2 << 8 | b1) : (b1 << 24 | b2 << 16 | b3 << 8 | b4);
	}

	private static long read8Bytes(byte[] bytes, int offset, boolean le) {
		if (le) {
			long l = 0;
			for (int i = 0; i < 8; i++) {
				l |= ((long) (bytes[offset + i] & 0xFF)) << (8 * i);
			}
			return l;
		} else {
			long l = 0;
			for (int i = 0; i < 8; i++) {
				l |= ((long) (bytes[offset + i] & 0xFF)) << (8 * (7 - i));
			}
			return l;
		}
	}
}
