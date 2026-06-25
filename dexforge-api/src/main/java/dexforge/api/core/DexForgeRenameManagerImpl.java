package dexforge.api.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Stack;

import dexforge.api.model.DexForgeNode;
import dexforge.api.model.DexForgeNodeType;
import dexforge.api.rename.DexForgeRenameAction;
import dexforge.api.rename.DexForgeRenameManager;

/**
 * Implementation of Rename Manager with Undo/Redo capabilities.
 */
final class DexForgeRenameManagerImpl implements DexForgeRenameManager {
	private final DexForgeProject project;
	private final List<DexForgeRenameAction> history = new ArrayList<>();
	private final Stack<DexForgeRenameAction> undoStack = new Stack<>();
	private final Stack<DexForgeRenameAction> redoStack = new Stack<>();

	DexForgeRenameManagerImpl(DexForgeProject project) {
		this.project = project;
	}

	@Override
	public void rename(DexForgeNode node, String newName) {
		String oldName = node.getName();
		if (oldName.equals(newName)) {
			return;
		}

		applyRename(node, newName);

		DexForgeRenameAction action = new DexForgeRenameAction(node, oldName, newName);
		undoStack.push(action);
		history.add(action);
		redoStack.clear();
	}

	@Override
	public boolean undo() {
		if (undoStack.isEmpty()) {
			return false;
		}
		DexForgeRenameAction action = undoStack.pop();
		redoStack.push(action);

		findNode(action).ifPresent(node -> applyRename(node, action.getOldName()));

		return true;
	}

	@Override
	public boolean redo() {
		if (redoStack.isEmpty()) {
			return false;
		}
		DexForgeRenameAction action = redoStack.pop();
		undoStack.push(action);

		findNode(action).ifPresent(node -> applyRename(node, action.getNewName()));

		return true;
	}

	@Override
	public List<DexForgeRenameAction> getHistory() {
		return Collections.unmodifiableList(history);
	}

	@Override
	public void loadHistory(List<DexForgeRenameAction> history) {
		this.history.clear();
		this.history.addAll(history);
		this.undoStack.clear();
		this.redoStack.clear();

		// Re-apply renames to the engine
		for (DexForgeRenameAction action : history) {
			findNode(action).ifPresent(node -> applyRename(node, action.getNewName()));
		}
	}

	@Override
	public void resetHistory() {
		history.clear();
		undoStack.clear();
		redoStack.clear();
	}

	private Optional<? extends DexForgeNode> findNode(DexForgeRenameAction action) {
		String id = action.getNodeId();
		DexForgeNodeType type = action.getNodeType();

		if (type == null) {
			// Fallback for older saved projects
			return project.search().classes().named(id).findFirst();
		}

		// Remove prefixes if they exist (e.g., "cls:", "mth:")
		String rawId = id.contains(":") ? id.substring(id.indexOf(":") + 1) : id;

		switch (type) {
			case CLASS:
				return project.search().classes().named(rawId).findFirst();
			case METHOD:
				return project.search().methods().filter(m -> m.getFullName().equals(rawId) || m.getId().equals(id)).findFirst();
			case FIELD:
				return project.search().fields().filter(f -> f.getFullName().equals(rawId) || f.getId().equals(id)).findFirst();
			case PACKAGE:
				return project.getPackages().stream().filter(p -> p.getFullName().equals(rawId) || p.getId().equals(id)).findFirst();
			default:
				return Optional.empty();
		}
	}

	private void applyRename(DexForgeNode node, String name) {
		node.removeAlias(); // JADX logic to clear existing
		// Actually JADX doesn't have a direct 'rename' on JavaNode that takes String easily without going through Deobfuscator
		// But our DexForgeNode implementations have unwrap() which we can use internally.
		// For now we use the existing bridge logic.
		node.rename(name);
	}
}
