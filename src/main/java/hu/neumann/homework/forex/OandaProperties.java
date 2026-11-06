package hu.neumann.homework.forex;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class OandaProperties {

    @Value("${oanda.api.base-url}")
    private String baseUrl;

    @Value("${oanda.api.token-file}")
    private String tokenFile;

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getApiToken() {
        return readSecrets().getOrDefault("OANDA_API_TOKEN", "");
    }

    public String getAccountId() {
        return readSecrets().getOrDefault("OANDA_ACCOUNT_ID", "");
    }

    public boolean isConfigured() {
        String token = getApiToken();
        String account = getAccountId();
        return token != null && !token.isBlank()
                && !token.contains("your_practice")
                && account != null && !account.isBlank()
                && !account.contains("your_account");
    }

    private Map<String, String> readSecrets() {
        Map<String, String> map = new HashMap<>();
        Path path = resolveTokenFile();
        if (path == null || !Files.exists(path)) {
            return map;
        }
        try {
            List<String> lines = Files.readAllLines(path);
            for (String line : lines) {
                String trimmed = stripBom(line).trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#") || !trimmed.contains("=")) {
                    continue;
                }
                int idx = trimmed.indexOf('=');
                map.put(trimmed.substring(0, idx).trim(), trimmed.substring(idx + 1).trim());
            }
        } catch (IOException ignored) {
            // Missing or unreadable secrets file — pages show a clear UI message.
        }
        return map;
    }

    private Path resolveTokenFile() {
        Path configured = Path.of(tokenFile);
        if (Files.exists(configured)) {
            return configured.toAbsolutePath().normalize();
        }
        Path besideJar = Path.of(System.getProperty("user.dir", ".")).resolve(tokenFile);
        if (Files.exists(besideJar)) {
            return besideJar.toAbsolutePath().normalize();
        }
        return configured;
    }

    private String stripBom(String value) {
        if (value != null && !value.isEmpty() && value.charAt(0) == '\uFEFF') {
            return value.substring(1);
        }
        return value;
    }
}
