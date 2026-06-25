package dexforge.core.infrastructure.persistence;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.Objects;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

import dexforge.api.persistence.DexForgeProjectState;
import dexforge.api.persistence.DexForgeProjectStore;

/**
 * Persists project state as a JSON file using GSON.
 */
public final class JsonProjectStore implements DexForgeProjectStore {
	private static final Gson GSON = new GsonBuilder()
			.registerTypeAdapter(Instant.class, (JsonSerializer<Instant>) (src, typeOfSrc, context) -> new JsonPrimitive(src.toString()))
			.registerTypeAdapter(Instant.class, (JsonDeserializer<Instant>) (json, typeOfT, context) -> Instant.parse(json.getAsString()))
			.setPrettyPrinting()
			.create();

	@Override
	public void save(DexForgeProjectState state, File file) throws IOException {
		Objects.requireNonNull(state, "state");
		Objects.requireNonNull(file, "file");

		Path target = file.toPath();
		Path parent = target.toAbsolutePath().getParent();
		if (parent != null) {
			Files.createDirectories(parent);
		}

		Path tempFile = Files.createTempFile(parent, target.getFileName().toString(), ".tmp");
		try (Writer writer = Files.newBufferedWriter(tempFile, StandardCharsets.UTF_8)) {
			GSON.toJson(state, writer);
		}
		try {
			Files.move(tempFile, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			Files.move(tempFile, target, StandardCopyOption.REPLACE_EXISTING);
		}
	}

	@Override
	public DexForgeProjectState load(File file) throws IOException {
		Objects.requireNonNull(file, "file");
		if (!file.isFile()) {
			throw new IOException("Project file not found: " + file.getAbsolutePath());
		}
		try (Reader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
			DexForgeProjectState state = GSON.fromJson(reader, DexForgeProjectState.class);
			if (state == null) {
				throw new IOException("Project file is empty or invalid: " + file.getAbsolutePath());
			}
			normalize(state);
			return state;
		}
	}

	/**
	 * Serializes the state to a JSON string.
	 */
	public String toJson(DexForgeProjectState state) {
		return GSON.toJson(Objects.requireNonNull(state, "state"));
	}

	/**
	 * Deserializes the state from a JSON string.
	 */
	public DexForgeProjectState fromJson(String json) {
		DexForgeProjectState state = GSON.fromJson(Objects.requireNonNull(json, "json"), DexForgeProjectState.class);
		if (state != null) {
			normalize(state);
		}
		return state;
	}

	private static void normalize(DexForgeProjectState state) {
		state.setInputFiles(state.getInputFiles());
		state.setFingerprint(state.getFingerprint());
		state.setModules(state.getModules());
		state.setRenameHistory(state.getRenameHistory());
	}
}
