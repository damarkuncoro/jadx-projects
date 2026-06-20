package dexforge.application.port;

/**
 * Port (Output adapter): Untuk notify user tentang operation results.
 * Implementasi bisa berbeda untuk CLI, GUI, atau API.
 */
public interface NotificationPort {
	/**
	 * Notify success message.
	 */
	void notifySuccess(String message);

	/**
	 * Notify error message.
	 */
	void notifyError(String message);

	/**
	 * Notify info message.
	 */
	void notifyInfo(String message);

	/**
	 * Notify warning message.
	 */
	void notifyWarning(String message);

	/**
	 * Notify dengan progress update.
	 */
	void notifyProgress(String taskName, int progress, int total);
}
