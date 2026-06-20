package dexforge.application.usecase;

import java.util.List;

import dexforge.application.dto.ListPackagesRequest;
import dexforge.domain.model.device.DevicePackage;

/**
 * Use Case: ListPackagesUseCase
 * Orchestrates listing packages on a device.
 */
@UseCase
public class ListPackagesUseCase {

	public List<DevicePackage> execute(ListPackagesRequest request) {
		// TODO: implement using ADB integration
		return List.of();
	}
}
