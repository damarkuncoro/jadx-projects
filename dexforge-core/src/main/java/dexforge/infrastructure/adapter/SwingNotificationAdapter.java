package dexforge.infrastructure.adapter;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import dexforge.application.port.NotificationPort;

/**
 * Infrastructure Adapter: SwingNotificationAdapter
 * Implements NotificationPort for Swing UI.
 */
public class SwingNotificationAdapter implements NotificationPort {

	@Override
	public void notifySuccess(String message) {
		showMessage("Success", message, JOptionPane.INFORMATION_MESSAGE);
	}

	@Override
	public void notifyError(String message) {
		showMessage("Error", message, JOptionPane.ERROR_MESSAGE);
	}

	@Override
	public void notifyInfo(String message) {
		showMessage("Info", message, JOptionPane.INFORMATION_MESSAGE);
	}

	@Override
	public void notifyProgress(String taskName, int progress, int total) {
		// Progress not supported in simple Swing dialog
	}

	@Override
	public void notifyWarning(String message) {
		showMessage("Warning", message, JOptionPane.WARNING_MESSAGE);
	}

	private void showMessage(String title, String message, int messageType) {
		SwingUtilities.invokeLater(() -> {
			JOptionPane.showMessageDialog(null, message, title, messageType);
		});
	}
}
