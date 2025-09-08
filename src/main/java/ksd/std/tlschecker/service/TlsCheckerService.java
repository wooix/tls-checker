package ksd.std.tlschecker.service;

import org.springframework.stereotype.Service;

import javax.net.ssl.*;
import java.security.cert.X509Certificate;
import java.util.*;

/**
 * TLS 버전과 지원 알고리즘을 확인하는 서비스
 */
@Service
public class TlsCheckerService {

    private static final int CONNECTION_TIMEOUT = 10000; // 10초
    private static final String[] TLS_VERSIONS = {
        "TLSv1", "TLSv1.1", "TLSv1.2", "TLSv1.3"
    };

    /**
     * 도메인의 TLS 지원 정보를 확인합니다.
     *
     * @param domain 확인할 도메인
     * @return TLS 지원 정보 맵
     */
    public Map<String, TlsInfo> checkTlsSupport(String domain) {
        Map<String, TlsInfo> results = new LinkedHashMap<>();
        
        for (String tlsVersion : TLS_VERSIONS) {
            TlsInfo tlsInfo = checkTlsVersion(domain, tlsVersion);
            // 지원 여부와 관계없이 모든 결과를 포함
            results.put(tlsVersion, tlsInfo);
        }
        
        return results;
    }

    /**
     * 특정 TLS 버전에 대한 지원 여부와 알고리즘을 확인합니다.
     *
     * @param domain 도메인
     * @param tlsVersion TLS 버전
     * @return TLS 정보
     */
    private TlsInfo checkTlsVersion(String domain, String tlsVersion) {
        TlsInfo tlsInfo = new TlsInfo(tlsVersion);
        
        try {
            // SSL 컨텍스트 생성
            SSLContext sslContext = SSLContext.getInstance(tlsVersion);
            sslContext.init(null, new TrustManager[]{new AcceptAllTrustManager()}, null);
            
            // SSL 소켓 팩토리 생성
            SSLSocketFactory factory = sslContext.getSocketFactory();
            
            // 연결 시도
            try (SSLSocket socket = (SSLSocket) factory.createSocket(domain, 443)) {
                socket.setSoTimeout(CONNECTION_TIMEOUT);
                
                // TLS 버전 설정
                socket.setEnabledProtocols(new String[]{tlsVersion});
                
                // 연결 시작
                socket.startHandshake();
                
                // 연결 성공 시 정보 수집
                tlsInfo.setSupported(true);
                tlsInfo.setCipherSuites(Arrays.asList(socket.getEnabledCipherSuites()));
                tlsInfo.setSupportedProtocols(Arrays.asList(socket.getEnabledProtocols()));
                
                // 인증서 정보 수집
                SSLSession session = socket.getSession();
                tlsInfo.setCertificateInfo(extractCertificateInfo(session));
                
            }
        } catch (Exception e) {
            tlsInfo.setSupported(false);
            tlsInfo.setErrorMessage(e.getMessage());
        }
        
        return tlsInfo;
    }

    /**
     * 인증서 정보를 추출합니다.
     *
     * @param session SSL 세션
     * @return 인증서 정보
     */
    private CertificateInfo extractCertificateInfo(SSLSession session) {
        CertificateInfo certInfo = new CertificateInfo();
        
        try {
            X509Certificate cert = (X509Certificate) session.getPeerCertificates()[0];
            certInfo.setSubject(cert.getSubjectDN().toString());
            certInfo.setIssuer(cert.getIssuerDN().toString());
            certInfo.setValidFrom(cert.getNotBefore());
            certInfo.setValidTo(cert.getNotAfter());
            certInfo.setSignatureAlgorithm(cert.getSigAlgName());
        } catch (Exception e) {
            certInfo.setErrorMessage("인증서 정보를 가져올 수 없습니다: " + e.getMessage());
        }
        
        return certInfo;
    }

    /**
     * 모든 인증서를 신뢰하는 TrustManager
     */
    private static class AcceptAllTrustManager implements X509TrustManager {
        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }

        @Override
        public void checkClientTrusted(X509Certificate[] certs, String authType) {
            // 모든 클라이언트 인증서를 신뢰
        }

        @Override
        public void checkServerTrusted(X509Certificate[] certs, String authType) {
            // 모든 서버 인증서를 신뢰
        }
    }

    /**
     * TLS 정보를 담는 클래스
     */
    public static class TlsInfo {
        private final String version;
        private boolean supported;
        private List<String> cipherSuites;
        private List<String> supportedProtocols;
        private CertificateInfo certificateInfo;
        private String errorMessage;

        public TlsInfo(String version) {
            this.version = version;
            this.supported = false;
            this.cipherSuites = new ArrayList<>();
            this.supportedProtocols = new ArrayList<>();
        }

        // Getters and Setters
        public String getVersion() { return version; }
        public boolean isSupported() { return supported; }
        public void setSupported(boolean supported) { this.supported = supported; }
        public List<String> getCipherSuites() { return cipherSuites; }
        public void setCipherSuites(List<String> cipherSuites) { this.cipherSuites = cipherSuites; }
        public List<String> getSupportedProtocols() { return supportedProtocols; }
        public void setSupportedProtocols(List<String> supportedProtocols) { this.supportedProtocols = supportedProtocols; }
        public CertificateInfo getCertificateInfo() { return certificateInfo; }
        public void setCertificateInfo(CertificateInfo certificateInfo) { this.certificateInfo = certificateInfo; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }

    /**
     * 인증서 정보를 담는 클래스
     */
    public static class CertificateInfo {
        private String subject;
        private String issuer;
        private Date validFrom;
        private Date validTo;
        private String signatureAlgorithm;
        private String errorMessage;

        // Getters and Setters
        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }
        public String getIssuer() { return issuer; }
        public void setIssuer(String issuer) { this.issuer = issuer; }
        public Date getValidFrom() { return validFrom; }
        public void setValidFrom(Date validFrom) { this.validFrom = validFrom; }
        public Date getValidTo() { return validTo; }
        public void setValidTo(Date validTo) { this.validTo = validTo; }
        public String getSignatureAlgorithm() { return signatureAlgorithm; }
        public void setSignatureAlgorithm(String signatureAlgorithm) { this.signatureAlgorithm = signatureAlgorithm; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }
}
