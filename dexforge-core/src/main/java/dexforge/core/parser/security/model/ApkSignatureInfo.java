package dexforge.core.parser.security.model;

import java.util.ArrayList;
import java.util.List;

public final class ApkSignatureInfo {
    private int schemeVersion; // 1, 2, 3, or 4
    private final List<CertificateInfo> certificates = new ArrayList<>();

    public int getSchemeVersion() { return schemeVersion; }
    public void setSchemeVersion(int schemeVersion) { this.schemeVersion = schemeVersion; }
    public List<CertificateInfo> getCertificates() { return certificates; }

    public static final class CertificateInfo {
        private String subject;
        private String issuer;
        private String serialNumber;
        private String fingerprintSha1;
        private String fingerprintSha256;

        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }
        public String getIssuer() { return issuer; }
        public void setIssuer(String issuer) { this.issuer = issuer; }
        public String getSerialNumber() { return serialNumber; }
        public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }
        public String getFingerprintSha1() { return fingerprintSha1; }
        public void setFingerprintSha1(String fingerprintSha1) { this.fingerprintSha1 = fingerprintSha1; }
        public String getFingerprintSha256() { return fingerprintSha256; }
        public void setFingerprintSha256(String fingerprintSha256) { this.fingerprintSha256 = fingerprintSha256; }
    }
}
