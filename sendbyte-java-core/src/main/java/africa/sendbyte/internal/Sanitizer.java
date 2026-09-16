package africa.sendbyte.internal;

import java.util.regex.Pattern;

public final class Sanitizer {
    private static final Pattern AUTHORIZATION =
            Pattern.compile("(?i)(authorization\\s*[:=]\\s*bearer\\s+)[^\\s,;]+");
    private static final Pattern API_KEY = Pattern.compile("(?i)sk_[a-z0-9_\\-]{6,}");
    private static final Pattern EMAIL =
            Pattern.compile("[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}");
    private static final Pattern URL = Pattern.compile("https?://[^\\s,;]+");
    private static final Pattern PHONE =
            Pattern.compile("(?<!\\w)\\+?[0-9][0-9 .()\\-]{7,}[0-9](?!\\w)");
    private static final Pattern LONG_IDENTIFIER =
            Pattern.compile(
                    "(?<![a-zA-Z0-9_\\-])[a-zA-Z0-9][a-zA-Z0-9_\\-]{23,}(?![a-zA-Z0-9_\\-])");

    private Sanitizer() {}

    public static String sanitize(final String value) {
        if (value == null) {
            return null;
        }

        String sanitized = AUTHORIZATION.matcher(value).replaceAll("$1[REDACTED]");
        sanitized = API_KEY.matcher(sanitized).replaceAll("[REDACTED]");
        sanitized = EMAIL.matcher(sanitized).replaceAll("[REDACTED_EMAIL]");
        sanitized = URL.matcher(sanitized).replaceAll("[REDACTED_URL]");
        sanitized = PHONE.matcher(sanitized).replaceAll("[REDACTED_PHONE]");
        return LONG_IDENTIFIER.matcher(sanitized).replaceAll("[REDACTED_ID]");
    }
}
