package dexforge.application.usecase;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dexforge.application.dto.ListPackagesRequest;
import dexforge.domain.model.device.DevicePackage;

import static org.assertj.core.api.Assertions.*;

public class ListPackagesUseCaseTest {
	private ListPackagesUseCase useCase;

	@BeforeEach
	public void setUp() {
		useCase = new ListPackagesUseCase();
	}

	@Test
	public void testExecuteReturnsEmptyList() {
		// Given
		ListPackagesRequest request = new ListPackagesRequest("emulator-5554", 0, "all");

		// When
		List<DevicePackage> result = useCase.execute(request);

		// Then
		assertThat(result).isEmpty();
	}
}
