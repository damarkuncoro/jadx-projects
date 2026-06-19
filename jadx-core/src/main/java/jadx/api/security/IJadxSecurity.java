package jadx.api.security;

import java.io.InputStream;

import org.w3c.dom.Document;

import dexforge.zip.security.IDexforgeZipSecurity;

public interface IJadxSecurity extends IDexforgeZipSecurity {

	/**
	 * Check if application package is safe
	 *
	 * @return normalized/sanitized string or same string if safe
	 */
	String verifyAppPackage(String appPackage);

	/**
	 * XML document parser
	 */
	Document parseXml(InputStream in);
}
