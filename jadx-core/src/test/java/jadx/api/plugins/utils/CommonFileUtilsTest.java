package dexforge.api.plugins.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommonFileUtilsTest {

	@Test
	void isZipFileExtSupportsProfAndProfm() {
		assertTrue(CommonFileUtils.isZipFileExt("archive.prof"));
		assertTrue(CommonFileUtils.isZipFileExt("archive.profm"));
		assertFalse(CommonFileUtils.isZipFileExt("archive.txt"));
	}
}
