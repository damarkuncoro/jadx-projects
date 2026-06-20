package dexforge.application.usecase;

import dexforge.domain.model.device.Device;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

public class ListDevicesUseCaseTest {
	private ListDevicesUseCase useCase;

	@BeforeEach
	public void setUp() {
		useCase = new ListDevicesUseCase();
	}

	@Test
	public void testExecuteReturnsEmptyList() {
		// When
		List<Device> result = useCase.execute();

		// Then
		assertThat(result).isEmpty();
	}
}