package jadx.frida;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FridaSnippetRegistry {
	private final List<IFridaSnippet> snippets = new ArrayList<>();

	public void registerSnippet(IFridaSnippet snippet) {
		snippets.add(snippet);
	}

	public List<IFridaSnippet> getAllSnippets() {
		return new ArrayList<>(snippets);
	}

	public Optional<IFridaSnippet> findByDisplayName(String displayName) {
		return snippets.stream()
				.filter(s -> s.getDisplayName().equals(displayName))
				.findFirst();
	}

	public void registerDefaultSnippets() {
		for (FridaSnippets snippet : FridaSnippets.values()) {
			registerSnippet(new IFridaSnippet() {
				@Override
				public String getDisplayName() {
					return snippet.getDisplayName();
				}

				@Override
				public String getScript() {
					return snippet.getScript();
				}
			});
		}
	}
}
