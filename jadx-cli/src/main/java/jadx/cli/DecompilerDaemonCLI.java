package jadx.cli;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;

import jadx.cli.daemon.DaemonCommandRouter;
import jadx.cli.dto.DaemonRequest;
import jadx.cli.dto.DaemonResponse;

public class DecompilerDaemonCLI {
	private static final Gson GSON = new Gson();

	public static int run(String[] args) {
		DaemonCommandRouter router = new DaemonCommandRouter();
		ExecutorService executor = Executors.newSingleThreadExecutor();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
			boolean running = true;
			while (running) {
				String line = reader.readLine();
				if (line == null) {
					break;
				}
				line = line.trim();
				if (line.isEmpty()) {
					continue;
				}
				DaemonRequest request;
				try {
					request = GSON.fromJson(line, DaemonRequest.class);
				} catch (Exception e) {
					sendResponse(DaemonResponse.error(0, "Invalid JSON request format: " + e.getMessage()));
					continue;
				}

				if (request == null || request.getMethod() == null) {
					sendResponse(DaemonResponse.error(0, "Missing 'method' in JSON request"));
					continue;
				}

				if ("exit".equals(request.getMethod())) {
					running = false;
					executor.shutdown();
					try {
						if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
							executor.shutdownNow();
						}
					} catch (InterruptedException e) {
						executor.shutdownNow();
						Thread.currentThread().interrupt();
					}

					try {
						DaemonResponse response = router.route(request);
						sendResponse(response);
					} catch (Exception e) {
						sendResponse(DaemonResponse.error(request.getId(), "Exit command failed: " + e.getMessage()));
					}
					break;
				}

				final DaemonRequest finalReq = request;
				executor.submit(() -> {
					try {
						DaemonResponse response = router.route(finalReq);
						sendResponse(response);
					} catch (Exception e) {
						sendResponse(DaemonResponse.error(finalReq.getId(), "Command execution failed: " + e.getMessage()));
					}
				});
			}
		} catch (Exception e) {
			System.err.println("Daemon error: " + e.getMessage());
			return 1;
		} finally {
			executor.shutdown();
			try {
				if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
					executor.shutdownNow();
				}
			} catch (InterruptedException e) {
				executor.shutdownNow();
				Thread.currentThread().interrupt();
			}
			router.getDaemonService().close();
		}
		return 0;
	}

	private static synchronized void sendResponse(DaemonResponse response) {
		System.out.println(GSON.toJson(response));
		System.out.flush();
	}
}
