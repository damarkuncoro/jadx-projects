package jadx.gui.utils.tools;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import com.formdev.flatlaf.extras.FlatSVGIcon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GeneratePngLogos {
	private static final Logger LOG = LoggerFactory.getLogger(GeneratePngLogos.class);

	public static void main(String[] args) {
		try {
			// Set system property to run headless
			System.setProperty("java.awt.headless", "true");

			Path logosDir = getLogosDir();
			if (logosDir == null) {
				System.err.println("Could not locate logos resource directory!");
				System.exit(1);
			}
			System.out.println("Target logos directory: " + logosDir.toAbsolutePath());

			String svgPath = "logos/dexforge-logo.svg";

			// Sizes we want to generate
			int[] sizes = { 16, 32, 48, 256 };
			List<byte[]> pngBytesList = new ArrayList<>();

			for (int size : sizes) {
				String outputName = size == 256 ? "dexforge-logo.png" : "dexforge-logo-" + size + "px.png";
				Path outputPath = logosDir.resolve(outputName);

				System.out.println("Generating " + size + "x" + size + " png -> " + outputPath.toAbsolutePath());
				byte[] pngBytes = generatePng(svgPath, size, outputPath.toFile());
				pngBytesList.add(pngBytes);
			}

			// Generate ICO file
			Path icoPath = logosDir.resolve("dexforge-logo.ico");
			System.out.println("Generating multi-resolution ico -> " + icoPath.toAbsolutePath());
			generateIco(sizes, pngBytesList, icoPath.toFile());

			System.out.println("Logo asset generation completed successfully!");
		} catch (Exception e) {
			LOG.error("Logo generation failed", e);
			System.exit(2);
		}
	}

	private static byte[] generatePng(String svgResource, int size, File outputFile) throws IOException {
		FlatSVGIcon icon = new FlatSVGIcon(svgResource, size, size);
		if (!icon.hasFound()) {
			throw new RuntimeException("SVG resource not found: " + svgResource);
		}
		BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = image.createGraphics();
		icon.paintIcon(null, g2d, 0, 0);
		g2d.dispose();

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(image, "png", baos);
		byte[] bytes = baos.toByteArray();

		try (FileOutputStream fos = new FileOutputStream(outputFile)) {
			fos.write(bytes);
		}
		return bytes;
	}

	private static void generateIco(int[] sizes, List<byte[]> pngBytesList, File outputFile) throws IOException {
		try (FileOutputStream fos = new FileOutputStream(outputFile)) {
			int count = sizes.length;
			// Write ICO Header: 6 bytes
			fos.write(new byte[] { 0, 0 }); // Reserved: always 0
			fos.write(new byte[] { 1, 0 }); // Type: 1 = ICO
			// Count: 2 bytes (little endian)
			fos.write(new byte[] { (byte) (count & 0xFF), (byte) ((count >> 8) & 0xFF) });

			// Offset calculations
			int offset = 6 + count * 16; // Header (6) + entries (16 bytes each)

			// Write Directory Entries
			for (int i = 0; i < count; i++) {
				int size = sizes[i];
				byte[] pngBytes = pngBytesList.get(i);
				int sizeBytes = pngBytes.length;

				fos.write(size >= 256 ? 0 : size); // Width (0 for 256)
				fos.write(size >= 256 ? 0 : size); // Height (0 for 256)
				fos.write(0); // Color count: 0 for > 8bpp
				fos.write(0); // Reserved: always 0
				fos.write(new byte[] { 1, 0 }); // Planes: 1
				fos.write(new byte[] { 32, 0 }); // Bits per pixel: 32

				// Size of image data: 4 bytes (little endian)
				fos.write(new byte[] {
						(byte) (sizeBytes & 0xFF),
						(byte) ((sizeBytes >> 8) & 0xFF),
						(byte) ((sizeBytes >> 16) & 0xFF),
						(byte) ((sizeBytes >> 24) & 0xFF)
				});

				// Offset of image data: 4 bytes (little endian)
				fos.write(new byte[] {
						(byte) (offset & 0xFF),
						(byte) ((offset >> 8) & 0xFF),
						(byte) ((offset >> 16) & 0xFF),
						(byte) ((offset >> 24) & 0xFF)
				});

				offset += sizeBytes;
			}

			// Write raw PNG data blocks
			for (byte[] pngBytes : pngBytesList) {
				fos.write(pngBytes);
			}
		}
	}

	private static Path getLogosDir() {
		// Try directories relative to root or module
		Path[] candidates = {
				Paths.get("jadx-gui/src/main/resources/logos"),
				Paths.get("src/main/resources/logos"),
				Paths.get("../src/main/resources/logos")
		};
		for (Path cand : candidates) {
			if (Files.exists(cand)) {
				return cand;
			}
		}
		return null;
	}
}
