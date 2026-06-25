package dexforge.api.persistence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import dexforge.api.rename.DexForgeRenameAction;

/**
 * Data Transfer Object (DTO) for saving/loading the entire project state.
 */
public final class DexForgeProjectState {
	private String name;
	private String description;
	private String engineId;
	private List<String> inputFiles = new ArrayList<>();
	private Map<String, String> fingerprint = new HashMap<>();
	private List<DexForgeModuleState> modules = new ArrayList<>();
	private List<DexForgeRenameAction> renameHistory = new ArrayList<>();
	private long lastModified;

	public static final class DexForgeModuleState {
		private String name;
		private String type;
		private String path;
		private long size;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getPath() {
			return path;
		}

		public void setPath(String path) {
			this.path = path;
		}

		public long getSize() {
			return size;
		}

		public void setSize(long size) {
			this.size = size;
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getEngineId() {
		return engineId;
	}

	public void setEngineId(String engineId) {
		this.engineId = engineId;
	}

	public List<String> getInputFiles() {
		return inputFiles;
	}

	public void setInputFiles(List<String> inputFiles) {
		this.inputFiles = new ArrayList<>(Objects.requireNonNullElse(inputFiles, List.of()));
	}

	public Map<String, String> getFingerprint() {
		return fingerprint;
	}

	public void setFingerprint(Map<String, String> fingerprint) {
		this.fingerprint = new HashMap<>(Objects.requireNonNullElse(fingerprint, Map.of()));
	}

	public List<DexForgeModuleState> getModules() {
		return modules;
	}

	public void setModules(List<DexForgeModuleState> modules) {
		this.modules = new ArrayList<>(Objects.requireNonNullElse(modules, List.of()));
	}

	public List<DexForgeRenameAction> getRenameHistory() {
		return renameHistory;
	}

	public void setRenameHistory(List<DexForgeRenameAction> renameHistory) {
		this.renameHistory = new ArrayList<>(Objects.requireNonNullElse(renameHistory, List.of()));
	}

	public long getLastModified() {
		return lastModified;
	}

	public void setLastModified(long lastModified) {
		this.lastModified = lastModified;
	}
}
