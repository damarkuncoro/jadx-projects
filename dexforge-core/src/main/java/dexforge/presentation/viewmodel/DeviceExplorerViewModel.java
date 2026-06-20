package dexforge.presentation.viewmodel;

import java.util.List;

import dexforge.domain.model.device.Device;

/**
 * View Model: DeviceExplorerViewModel
 * UI state model untuk Device Explorer panel.
 */
public class DeviceExplorerViewModel {
	private final List<Device> devices;
	private final String selectedDevice;
	private final boolean isLoading;
	private final String errorMessage;

	private DeviceExplorerViewModel(List<Device> devices, String selectedDevice, boolean isLoading, String errorMessage) {
		this.devices = devices != null ? devices : List.of();
		this.selectedDevice = selectedDevice;
		this.isLoading = isLoading;
		this.errorMessage = errorMessage;
	}

	public static DeviceExplorerViewModel empty() {
		return new DeviceExplorerViewModel(List.of(), null, false, null);
	}

	public static DeviceExplorerViewModel loading() {
		return new DeviceExplorerViewModel(List.of(), null, true, null);
	}

	public static DeviceExplorerViewModel error(String message) {
		return new DeviceExplorerViewModel(List.of(), null, false, message);
	}

	public static DeviceExplorerViewModel of(List<Device> devices, String selectedDevice) {
		return new DeviceExplorerViewModel(devices, selectedDevice, false, null);
	}

	public List<Device> getDevices() {
		return devices;
	}

	public String getSelectedDevice() {
		return selectedDevice;
	}

	public boolean isLoading() {
		return isLoading;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public int getDeviceCount() {
		return devices.size();
	}
}
