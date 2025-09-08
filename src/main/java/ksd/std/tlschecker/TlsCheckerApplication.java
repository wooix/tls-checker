package ksd.std.tlschecker;

import ksd.std.tlschecker.service.TlsCheckerService;
import ksd.std.tlschecker.util.ConsoleOutputUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.*;

/**
 * TLS Checker 메인 애플리케이션
 * 
 * 도메인을 입력받아 TLS 지원 현황과 사용 가능한 알고리즘을 확인하는 프로그램입니다.
 * xterm 콘솔에서 색상과 함께 결과를 출력합니다.
 */
@SpringBootApplication
public class TlsCheckerApplication implements CommandLineRunner {

    @Autowired
    private TlsCheckerService tlsCheckerService;

    @Autowired
    private ConsoleOutputUtil consoleOutputUtil;

    public static void main(String[] args) {
        // 웹 서버 비활성화
        System.setProperty("spring.main.web-application-type", "none");
        
        ConfigurableApplicationContext context = SpringApplication.run(TlsCheckerApplication.class, args);
        
        // 애플리케이션 종료 시 컨텍스트도 함께 종료
        Runtime.getRuntime().addShutdownHook(new Thread(context::close));
    }

    @Override
    public void run(String... args) throws Exception {
        // 명령행 인수가 있는 경우
        if (args.length > 0) {
            String domain = args[0];
            processDomain(domain);
            return;
        }

        // 대화형 모드
        runInteractiveMode();
    }

    /**
     * 대화형 모드를 실행합니다.
     */
    private void runInteractiveMode() {
        consoleOutputUtil.printUsage();

        while (true) {
            try {
                String input = readInputWithTimeout("Enter domain (quit/exit to end): ", 10);
                
                if (input == null) {
                    System.out.println("\nInput timeout. Exiting program.");
                    break;
                }

                if (input.isEmpty()) {
                    continue;
                }

                if ("quit".equalsIgnoreCase(input) || "exit".equalsIgnoreCase(input)) {
                    System.out.println("Exiting program.");
                    break;
                }

                if ("help".equalsIgnoreCase(input)) {
                    consoleOutputUtil.printUsage();
                    continue;
                }

                processDomain(input);
                System.out.println();
            } catch (Exception e) {
                System.out.println("\nInput error occurred. Exiting program.");
                break;
            }
        }
    }

    /**
     * 타임아웃이 있는 입력을 받습니다.
     *
     * @param prompt 프롬프트 메시지
     * @param timeoutSeconds 타임아웃 시간 (초)
     * @return 입력된 문자열 또는 null (타임아웃)
     */
    private String readInputWithTimeout(String prompt, int timeoutSeconds) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Scanner scanner = new Scanner(System.in);
        
        try {
            System.out.print(prompt);
            
            Future<String> future = executor.submit(() -> {
                try {
                    return scanner.nextLine().trim();
                } catch (java.util.NoSuchElementException e) {
                    return null;
                }
            });
            
            return future.get(timeoutSeconds, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            return null;
        } catch (Exception e) {
            return null;
        } finally {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
            }
        }
    }

    /**
     * 도메인을 처리합니다.
     *
     * @param domain 처리할 도메인
     */
    private void processDomain(String domain) {
        try {
            // 도메인 정규화 (https:// 제거)
            String normalizedDomain = normalizeDomain(domain);
            
            System.out.println("Checking TLS support status...");
            System.out.println("Domain: " + normalizedDomain);
            System.out.println();

            // TLS 체크 실행
            Map<String, TlsCheckerService.TlsInfo> results = tlsCheckerService.checkTlsSupport(normalizedDomain);

            // 결과 출력
            consoleOutputUtil.printTlsResults(normalizedDomain, results);

        } catch (Exception e) {
            System.err.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 도메인을 정규화합니다.
     *
     * @param domain 원본 도메인
     * @return 정규화된 도메인
     */
    private String normalizeDomain(String domain) {
        if (domain == null || domain.trim().isEmpty()) {
            throw new IllegalArgumentException("Domain is empty.");
        }

        String normalized = domain.trim().toLowerCase();

        // 프로토콜 제거
        if (normalized.startsWith("https://")) {
            normalized = normalized.substring(8);
        } else if (normalized.startsWith("http://")) {
            normalized = normalized.substring(7);
        }

        // 포트 번호 제거
        if (normalized.contains(":")) {
            normalized = normalized.substring(0, normalized.indexOf(":"));
        }

        // 경로 제거
        if (normalized.contains("/")) {
            normalized = normalized.substring(0, normalized.indexOf("/"));
        }

        // 도메인 유효성 검사
        if (normalized.isEmpty() || !isValidDomain(normalized)) {
            throw new IllegalArgumentException("Invalid domain: " + domain);
        }

        return normalized;
    }

    /**
     * 도메인 유효성을 검사합니다.
     *
     * @param domain 검사할 도메인
     * @return 유효한 도메인인지 여부
     */
    private boolean isValidDomain(String domain) {
        if (domain == null || domain.isEmpty()) {
            return false;
        }

        // 기본적인 도메인 형식 검사
        return domain.matches("^[a-zA-Z0-9]([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?(\\.([a-zA-Z0-9]([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?))*$");
    }
}
