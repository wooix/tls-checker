package ksd.std.tlschecker.util;

import ksd.std.tlschecker.service.TlsCheckerService;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

/**
 * Java 표준 라이브러리를 사용한 콘솔 출력 유틸리티 클래스
 * 완벽한 ASCII 테이블 출력을 제공합니다.
 */
@Component
public class ConsoleOutputUtil {

    // ANSI 색상 코드
    private static final String RESET = "\033[0m";
    private static final String BOLD = "\033[1m";
    private static final String RED = "\033[31m";
    private static final String GREEN = "\033[32m";
    private static final String YELLOW = "\033[33m";
    private static final String BLUE = "\033[34m";
    private static final String MAGENTA = "\033[35m";
    private static final String CYAN = "\033[36m";
    private static final String WHITE = "\033[37m";
    private static final String BRIGHT_GREEN = "\033[92m";
    private static final String BRIGHT_BLUE = "\033[94m";
    private static final String BRIGHT_CYAN = "\033[96m";

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final int TABLE_WIDTH = 80;

    /**
     * TLS 체크 결과를 콘솔에 출력합니다.
     *
     * @param domain 도메인
     * @param results TLS 체크 결과
     */
    public void printTlsResults(String domain, Map<String, TlsCheckerService.TlsInfo> results) {
        System.out.println();
        printHeader(domain);
        System.out.println();

        if (results.isEmpty()) {
            printNoTlsSupport();
            return;
        }

        // TLS 버전별 결과를 테이블로 출력
        for (Map.Entry<String, TlsCheckerService.TlsInfo> entry : results.entrySet()) {
            String version = entry.getKey();
            TlsCheckerService.TlsInfo tlsInfo = entry.getValue();
            
            printTlsVersionTable(version, tlsInfo);
            System.out.println();
        }

        printSummaryTable(results);
    }

    /**
     * 헤더를 출력합니다.
     */
    private void printHeader(String domain) {
        String title = "TLS Support Status: " + domain;
        int padding = (TABLE_WIDTH - title.length()) / 2;
        
        printHorizontalLine('=');
        printCenteredLine(title, '=');
        printHorizontalLine('=');
    }

    /**
     * TLS 버전 정보를 테이블로 출력합니다.
     */
    private void printTlsVersionTable(String version, TlsCheckerService.TlsInfo tlsInfo) {
        String header = "TLS Version: " + version;
        printHorizontalLine('-');
        printCenteredLine(header, '-');
        printHorizontalLine('-');
        
        // 지원 여부
        String supportStatus = tlsInfo.isSupported() ? 
            BOLD + BRIGHT_GREEN + "SUPPORTED" + RESET : 
            BOLD + RED + "NOT SUPPORTED" + RESET;
        printTableRow("Status", supportStatus);
        
        if (!tlsInfo.isSupported() && tlsInfo.getErrorMessage() != null) {
            printTableRow("Error", BOLD + YELLOW + truncateString(tlsInfo.getErrorMessage(), 50) + RESET);
        }
        
        // 암호화 스위트 정보
        if (tlsInfo.isSupported() && !tlsInfo.getCipherSuites().isEmpty()) {
            List<String> cipherSuites = tlsInfo.getCipherSuites();
            int maxDisplay = Math.min(cipherSuites.size(), 8); // 최대 8개만 표시
            
            printTableRow("Cipher Suites", BOLD + BLUE + "(" + cipherSuites.size() + " total)" + RESET);
            
            for (int i = 0; i < maxDisplay; i++) {
                String cipher = formatCipherSuite(cipherSuites.get(i));
                printTableRow("", "  • " + truncateString(cipher, 50));
            }
            
            if (cipherSuites.size() > 8) {
                printTableRow("", "  ... (total " + cipherSuites.size() + " suites)");
            }
        }
        
        // 인증서 정보
        if (tlsInfo.isSupported() && tlsInfo.getCertificateInfo() != null) {
            TlsCheckerService.CertificateInfo certInfo = tlsInfo.getCertificateInfo();
            
            if (certInfo.getErrorMessage() != null) {
                printTableRow("Certificate Error", BOLD + RED + truncateString(certInfo.getErrorMessage(), 50) + RESET);
            } else {
                if (certInfo.getSubject() != null) {
                    printTableRow("Certificate Subject", truncateString(certInfo.getSubject(), 50));
                }
                if (certInfo.getIssuer() != null) {
                    printTableRow("Certificate Issuer", truncateString(certInfo.getIssuer(), 50));
                }
                if (certInfo.getValidFrom() != null && certInfo.getValidTo() != null) {
                    String validPeriod = DATE_FORMAT.format(certInfo.getValidFrom()) + " ~ " + 
                                       DATE_FORMAT.format(certInfo.getValidTo());
                    printTableRow("Valid Period", truncateString(validPeriod, 50));
                }
                if (certInfo.getSignatureAlgorithm() != null) {
                    printTableRow("Signature Algorithm", truncateString(certInfo.getSignatureAlgorithm(), 50));
                }
            }
        }
        
        printHorizontalLine('-');
    }

    /**
     * 암호화 스위트를 포맷팅합니다.
     */
    private String formatCipherSuite(String cipherSuite) {
        // 주요 암호화 스위트에 색상 적용
        if (cipherSuite.contains("AES_256")) {
            return BRIGHT_GREEN + cipherSuite + RESET;
        } else if (cipherSuite.contains("AES_128")) {
            return GREEN + cipherSuite + RESET;
        } else if (cipherSuite.contains("3DES")) {
            return YELLOW + cipherSuite + RESET;
        } else if (cipherSuite.contains("RC4")) {
            return RED + cipherSuite + RESET;
        } else {
            return WHITE + cipherSuite + RESET;
        }
    }

    /**
     * TLS가 지원되지 않을 때의 메시지를 출력합니다.
     */
    private void printNoTlsSupport() {
        printHorizontalLine('-');
        printCenteredLine(BOLD + RED + "NO TLS SUPPORT" + RESET, '-');
        printHorizontalLine('-');
        printCenteredLine("No supported TLS versions found for this domain.", '|');
        printCenteredLine("Please check the domain name or network connection.", '|');
        printHorizontalLine('-');
    }

    /**
     * 요약 정보를 테이블로 출력합니다.
     */
    private void printSummaryTable(Map<String, TlsCheckerService.TlsInfo> results) {
        printHorizontalLine('-');
        printCenteredLine(BOLD + BRIGHT_CYAN + "SUMMARY" + RESET, '-');
        printHorizontalLine('-');
        
        // 지원되는 TLS 버전 수 계산
        int supportedCount = 0;
        StringBuilder supportedVersions = new StringBuilder();
        
        for (Map.Entry<String, TlsCheckerService.TlsInfo> entry : results.entrySet()) {
            if (entry.getValue().isSupported()) {
                supportedCount++;
                if (supportedVersions.length() > 0) {
                    supportedVersions.append(", ");
                }
                supportedVersions.append(entry.getKey());
            }
        }
        
        printTableRow("Supported TLS Versions", supportedCount + "/4");
        if (supportedCount > 0) {
            printTableRow("Supported Versions", supportedVersions.toString());
        }
        
        // 보안 권장사항
        printTableRow("", "");
        printTableRow(BOLD + YELLOW + "Security Recommendations" + RESET, "");
        
        if (results.containsKey("TLSv1.3") && results.get("TLSv1.3").isSupported()) {
            printTableRow("TLS 1.3", BOLD + GREEN + "SUPPORTED - Latest Security Standard" + RESET);
        } else {
            printTableRow("TLS 1.3", BOLD + RED + "NOT SUPPORTED - Security Upgrade Recommended" + RESET);
        }
        
        if (results.containsKey("TLSv1.2") && results.get("TLSv1.2").isSupported()) {
            printTableRow("TLS 1.2", BOLD + GREEN + "SUPPORTED - Secure Version" + RESET);
        } else {
            printTableRow("TLS 1.2", BOLD + RED + "NOT SUPPORTED - Security Risk" + RESET);
        }
        
        boolean hasOldVersions = (results.containsKey("TLSv1") && results.get("TLSv1").isSupported()) ||
                                (results.containsKey("TLSv1.1") && results.get("TLSv1.1").isSupported());
        if (hasOldVersions) {
            printTableRow("Legacy TLS", BOLD + YELLOW + "WARNING - Potential Security Vulnerabilities" + RESET);
        }
        
        printHorizontalLine('-');
        System.out.println();
    }

    /**
     * 수평선을 출력합니다.
     */
    private void printHorizontalLine(char character) {
        System.out.println("+" + repeatString(String.valueOf(character), TABLE_WIDTH - 2) + "+");
    }

    /**
     * 중앙 정렬된 라인을 출력합니다.
     */
    private void printCenteredLine(String text, char borderChar) {
        int textLength = getDisplayLength(text);
        int padding = (TABLE_WIDTH - 2 - textLength) / 2;
        String leftPadding = repeatString(" ", padding);
        String rightPadding = repeatString(" ", TABLE_WIDTH - 2 - textLength - padding);
        System.out.println("|" + leftPadding + text + rightPadding + "|");
    }

    /**
     * 테이블 행을 출력합니다.
     */
    private void printTableRow(String label, String value) {
        int labelLength = getDisplayLength(label);
        int valueLength = getDisplayLength(value);
        int totalLength = labelLength + valueLength + 3; // " : " 포함
        
        if (totalLength <= TABLE_WIDTH - 2) {
            // 한 줄에 표시
            String padding = repeatString(" ", TABLE_WIDTH - 2 - totalLength);
            System.out.println("| " + label + " : " + value + padding + " |");
        } else {
            // 두 줄에 표시
            System.out.println("| " + label + " : " + repeatString(" ", TABLE_WIDTH - 4 - labelLength) + " |");
            String valuePadding = repeatString(" ", TABLE_WIDTH - 2 - valueLength);
            System.out.println("|   " + value + valuePadding + " |");
        }
    }

    /**
     * ANSI 색상 코드를 제외한 실제 표시 길이를 계산합니다.
     */
    private int getDisplayLength(String text) {
        if (text == null) return 0;
        return text.replaceAll("\033\\[[0-9;]*m", "").length();
    }

    /**
     * 문자열을 지정된 횟수만큼 반복합니다.
     */
    private String repeatString(String str, int count) {
        if (count <= 0) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }

    /**
     * 문자열을 지정된 길이로 자릅니다.
     */
    private String truncateString(String str, int maxLength) {
        if (str == null) return "";
        if (getDisplayLength(str) <= maxLength) return str;
        
        // ANSI 코드를 고려하여 자르기
        String cleanStr = str.replaceAll("\033\\[[0-9;]*m", "");
        if (cleanStr.length() <= maxLength) return str;
        
        String truncated = cleanStr.substring(0, maxLength - 3) + "...";
        // 원본 문자열에서 색상 코드를 유지하면서 자르기
        return str.substring(0, Math.min(str.length(), maxLength)) + "...";
    }

    /**
     * 사용법을 출력합니다.
     */
    public void printUsage() {
        printHorizontalLine('-');
        printCenteredLine(BOLD + BRIGHT_BLUE + "TLS Checker Usage" + RESET, '-');
        printHorizontalLine('-');
        printCenteredLine("Enter a domain to check TLS support status.", '|');
        printCenteredLine("", '|');
        printCenteredLine(BOLD + GREEN + "Examples:" + RESET, '|');
        printCenteredLine("  java -jar tls-checker.jar google.com", '|');
        printCenteredLine("  java -jar tls-checker.jar github.com", '|');
        printCenteredLine("", '|');
        printCenteredLine(BOLD + YELLOW + "Notes:" + RESET, '|');
        printCenteredLine("  • Enter domain name only (without https://)", '|');
        printCenteredLine("  • Connects to port 443 (HTTPS)", '|');
        printCenteredLine("  • Connection timeout: 10 seconds", '|');
        printHorizontalLine('-');
    }
}