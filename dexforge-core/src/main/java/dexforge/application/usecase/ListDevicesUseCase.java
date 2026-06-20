package dexforge.application.usecase;

import java.util.List;

import dexforge.domain.model.device.Device;

/**
 * Use Case: ListDevicesUseCase
 * Orchestrates listing connected Android devices.
 */
@UseCase
public class ListDevicesUseCase {

	public List<Device> execute() {
		// TODO: implement using ADB integration
		return List.of();
	}
}
