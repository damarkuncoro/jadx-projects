package jadx.gui.treemodel;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ElfParserTest {

	@Test
	void testInvalidElf() {
		byte[] shortBytes = new byte[20];
		String res = JResource.parseElfHeader(shortBytes);
		assertThat(res).contains("too short");

		byte[] notElfBytes = new byte[64];
		notElfBytes[0] = 'N';
		notElfBytes[1] = 'O';
		notElfBytes[2] = 'T';
		String res2 = JResource.parseElfHeader(notElfBytes);
		assertThat(res2).contains("Not an ELF file");
	}

	@Test
	void testElf64LittleEndian() {
		byte[] bytes = new byte[64];
		// Magic
		bytes[0] = 0x7f;
		bytes[1] = 'E';
		bytes[2] = 'L';
		bytes[3] = 'F';

		bytes[4] = 2; // ELF64
		bytes[5] = 1; // Little Endian
		bytes[6] = 1; // Version
		bytes[7] = 3; // OS/ABI: GNU/Linux
		bytes[8] = 0; // ABI version

		// Type: DYN (Shared Object)
		bytes[16] = 0x03;
		bytes[17] = 0x00;

		// Machine: AArch64 (ARM 64-bit)
		bytes[18] = (byte) 0xB7;
		bytes[19] = 0x00;

		// Object version: 1
		bytes[20] = 0x01;
		bytes[21] = 0x00;
		bytes[22] = 0x00;
		bytes[23] = 0x00;

		// Entry point: 0x1000
		bytes[24] = 0x00;
		bytes[25] = 0x10;
		bytes[26] = 0x00;
		bytes[27] = 0x00;

		// Start of Program Headers: 64
		bytes[32] = 0x40;

		// Start of Section Headers: 4096
		bytes[40] = 0x00;
		bytes[41] = 0x10;

		// Size of this Header: 64
		bytes[52] = 0x40;

		// Number of Program Headers: 4
		bytes[56] = 0x04;

		// Number of Section Headers: 12
		bytes[60] = 0x0C;

		String report = JResource.parseElfHeader(bytes);

		assertThat(report).contains("ELF64 (64-bit)");
		assertThat(report).contains("little endian");
		assertThat(report).contains("UNIX - GNU/Linux");
		assertThat(report).contains("DYN (Shared object / Shared library)");
		assertThat(report).contains("AArch64 (ARM 64-bit)");
		assertThat(report).contains("Entry Point Address            : 0x0000000000001000");
		assertThat(report).contains("Start of Program Headers       : 64");
		assertThat(report).contains("Start of Section Headers       : 4096");
		assertThat(report).contains("Number of Program Headers      : 4");
		assertThat(report).contains("Number of Section Headers      : 12");
	}

	@Test
	void testElf32BigEndian() {
		byte[] bytes = new byte[52];
		// Magic
		bytes[0] = 0x7f;
		bytes[1] = 'E';
		bytes[2] = 'L';
		bytes[3] = 'F';

		bytes[4] = 1; // ELF32
		bytes[5] = 2; // Big Endian
		bytes[6] = 1; // Version
		bytes[7] = 0; // OS/ABI: System V
		bytes[8] = 0; // ABI version

		// Type: EXEC
		bytes[16] = 0x00;
		bytes[17] = 0x02;

		// Machine: Intel 80386 (x86)
		bytes[18] = 0x00;
		bytes[19] = 0x03;

		// Object version: 1
		bytes[20] = 0x00;
		bytes[21] = 0x00;
		bytes[22] = 0x00;
		bytes[23] = 0x01;

		// Entry point: 0x8048000
		bytes[24] = 0x08;
		bytes[25] = 0x04;
		bytes[26] = (byte) 0x80;
		bytes[27] = 0x00;

		// Start of Program Headers: 52
		bytes[28] = 0x00;
		bytes[29] = 0x00;
		bytes[30] = 0x00;
		bytes[31] = 0x34;

		// Start of Section Headers: 8192
		bytes[32] = 0x00;
		bytes[33] = 0x00;
		bytes[34] = 0x20;
		bytes[35] = 0x00;

		// Size of this Header: 52
		bytes[40] = 0x00;
		bytes[41] = 0x34;

		// Number of Program Headers: 3
		bytes[44] = 0x00;
		bytes[45] = 0x03;

		// Number of Section Headers: 8
		bytes[48] = 0x00;
		bytes[49] = 0x08;

		String report = JResource.parseElfHeader(bytes);

		assertThat(report).contains("ELF32 (32-bit)");
		assertThat(report).contains("big endian");
		assertThat(report).contains("UNIX - System V");
		assertThat(report).contains("EXEC (Executable file)");
		assertThat(report).contains("Intel 80386 (x86)");
		assertThat(report).contains("Entry Point Address            : 0x08048000");
		assertThat(report).contains("Start of Program Headers       : 52");
		assertThat(report).contains("Start of Section Headers       : 8192");
		assertThat(report).contains("Number of Program Headers      : 3");
		assertThat(report).contains("Number of Section Headers      : 8");
	}
}
