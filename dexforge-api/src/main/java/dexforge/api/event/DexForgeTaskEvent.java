package dexforge.api.event;

import java.time.Instant;

/**
 * Event representing background task progress (e.g. decompilation progress).
 */
public final class DexForgeTaskEvent implements DexForgeEvent {
	private final Instant timestamp = Instant.now();
	private final String taskName;
	private final int progress; // 0 to 100
	private final String message;

	public DexForgeTaskEvent(String taskName, int progress, String message) {
		this.taskName = taskName;
		this.progress = progress;
		this.message = message;
	}

	@Override
	public Instant getTimestamp() { return timestamp; }

	@Override
	public String getMessage() { return message; }

	public String getTaskName() { return taskName; }

	public int getProgress() { return progress; }
}
