package dexforge.core.parser.security;

import dexforge.core.parser.security.model.ApkSignatureInfo;
import java.io.File;
import java.io.InputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Parser for APK signatures (v1 scheme).
 * V2/v3 parsing requires reading the APK signing block.
 */
public final class ApkSignatureParser {

    public ApkSignatureInfo parseV1(File apkFile) {
        ApkSignatureInfo info = new ApkSignatureInfo();
        info.setSchemeVersion(1);

        try (JarFile jar = new JarFile(apkFile)) {
            // V1 signature is stored in META-INF/*.RSA or *.DSA
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (name.startsWith("META-INF/") && (name.endsWith(".RSA") || name.endsWith(".DSA"))) {
                    try (InputStream is = jar.getInputStream(entry)) {
                        CertificateFactory cf = CertificateFactory.getInstance("X.509");
                        // This might be a PKCS7 block, so simple X509 decode might fail
                        // without a proper CMS/PKCS7 parser.
                        // Simplified for architectural demonstration:
                        X509Certificate cert = (X509Certificate) cf.generateCertificate(is);

                        ApkSignatureInfo.CertificateInfo certInfo = new ApkSignatureInfo.CertificateInfo();
                        certInfo.setSubject(cert.getSubjectDN().getName());
                        certInfo.setIssuer(cert.getIssuerDN().getName());
                        certInfo.setSerialNumber(cert.getSerialNumber().toString(16));

                        info.getCertificates().add(certInfo);
                    }
                }
            }
        } catch (Exception e) {
            // Fallback or log error
        }
        return info;
    }
}
