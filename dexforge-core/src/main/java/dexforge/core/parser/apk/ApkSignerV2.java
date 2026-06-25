package dexforge.core.parser.apk;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;

/**
 * Foundation for APK Signature Scheme v2.
 * V2 signing protects the entire APK file, not just individual files.
 */
public final class ApkSignerV2 {

	private static final long APK_SIG_BLOCK_MAGIC_HI = 0x3234206b636f6c42L; // "Block 42"
	private static final long APK_SIG_BLOCK_MAGIC_LO = 0x20676953204b5041L; // "APK Sig "
	private static final int APK_SIGNATURE_SCHEME_V2_BLOCK_ID = 0x7109871a;

	public void sign(File apkFile) throws Exception {
		try (RandomAccessFile raf = new RandomAccessFile(apkFile, "rw")) {
			long centralDirOffset = findCentralDirOffset(raf);

			// 1. Calculate digests for the 3 sections:
			// Section 1: Contents of ZIP entries
			// Section 2: ZIP Central Directory
			// Section 3: ZIP End of Central Directory
			byte[] digest = calculateApkDigest(raf, centralDirOffset);

			// 2. Build the APK Signing Block
			byte[] signingBlock = buildSigningBlock(digest);

			// 3. Shift Central Directory and EOCD to make room for Signing Block
			shiftTail(raf, centralDirOffset, signingBlock.length);

			// 4. Write Signing Block
			raf.seek(centralDirOffset);
			raf.write(signingBlock);

			// 5. Update EOCD with new Central Directory offset
			updateEocd(raf, centralDirOffset + signingBlock.length);
		}
	}

	private long findCentralDirOffset(RandomAccessFile raf) throws Exception {
		// Simple EOCD search (usually at the very end if no comment)
		raf.seek(raf.length() - 22);
		byte[] eocd = new byte[22];
		raf.readFully(eocd);
		ByteBuffer bb = ByteBuffer.wrap(eocd).order(ByteOrder.LITTLE_ENDIAN);
		if (bb.getInt() != 0x06054b50) {
			throw new Exception("Could not find EOCD");
		}
		bb.position(16);
		return bb.getInt() & 0xFFFFFFFFL;
	}

	private byte[] calculateApkDigest(RandomAccessFile raf, long centralDirOffset) throws Exception {
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		// Simplified: In real V2, this is done in 1MB chunks and combined.
		// We hash the three sections.

		// Section 1
		raf.seek(0);
		hashSection(raf, 0, centralDirOffset, md);

		// Section 2 (Central Directory)
		long eocdOffset = raf.length() - 22; // Simplified
		hashSection(raf, centralDirOffset, eocdOffset - centralDirOffset, md);

		// Section 3 (EOCD)
		hashSection(raf, eocdOffset, 22, md);

		return md.digest();
	}

	private void hashSection(RandomAccessFile raf, long start, long len, MessageDigest md) throws Exception {
		byte[] buffer = new byte[8192];
		raf.seek(start);
		long remaining = len;
		while (remaining > 0) {
			int read = raf.read(buffer, 0, (int) Math.min(buffer.length, remaining));
			if (read == -1) {
				break;
			}
			md.update(buffer, 0, read);
			remaining -= read;
		}
	}

	private byte[] buildSigningBlock(byte[] digest) {
		// Extremely simplified V2 block structure for architectural foundation
		// Real V2 block contains X.509 certs, signatures, etc.
		ByteBuffer pairValue = ByteBuffer.allocate(1024).order(ByteOrder.LITTLE_ENDIAN);
		pairValue.putInt(1); // sequence length
		// signer block...
		pairValue.putInt(digest.length);
		pairValue.put(digest);

		int pairSize = 8 + 4 + pairValue.position();
		int blockSize = 8 + pairSize + 8 + 16;

		ByteBuffer block = ByteBuffer.allocate(blockSize).order(ByteOrder.LITTLE_ENDIAN);
		block.putLong(blockSize - 8); // size of block

		// Pair
		block.putLong(pairSize - 8);
		block.putInt(APK_SIGNATURE_SCHEME_V2_BLOCK_ID);
		block.put(pairValue.array(), 0, pairValue.position());

		block.putLong(blockSize - 8); // size of block
		block.putLong(APK_SIG_BLOCK_MAGIC_LO);
		block.putLong(APK_SIG_BLOCK_MAGIC_HI);

		return block.array();
	}

	private void shiftTail(RandomAccessFile raf, long offset, int amount) throws Exception {
		long len = raf.length();
		byte[] tail = new byte[(int) (len - offset)];
		raf.seek(offset);
		raf.readFully(tail);
		raf.seek(offset + amount);
		raf.write(tail);
	}

	private void updateEocd(RandomAccessFile raf, long newOffset) throws Exception {
		raf.seek(raf.length() - 6); // Offset of CD in EOCD is at offset 16 (length - 6)
		ByteBuffer bb = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
		bb.putInt((int) newOffset);
		raf.write(bb.array());
	}
}
