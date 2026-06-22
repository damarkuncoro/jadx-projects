package dexforge.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class DexForgeProjectIndex {
	private final List<DexForgeWorkspaceSymbol> symbols = new CopyOnWriteArrayList<>();

	public void addSymbol(DexForgeWorkspaceSymbol symbol) {
		symbols.add(symbol);
	}

	public List<DexForgeWorkspaceSymbol> search(String query, int limit) {
		String normalizedQuery = query == null ? "" : query.toLowerCase();
		List<DexForgeWorkspaceSymbol> results = new ArrayList<>();
		for (DexForgeWorkspaceSymbol symbol : symbols) {
			if (results.size() >= limit) {
				break;
			}
			if (symbol.getName().toLowerCase().contains(normalizedQuery)) {
				results.add(symbol);
			}
		}
		return results;
	}

	public int size() {
		return symbols.size();
	}

	public void clear() {
		symbols.clear();
	}
}
