package africa.sendbyte;

/**
 * Stable classification of a SendByte API failure, resolved from the wire error code.
 *
 * <p>The reviewed contract defines twenty-four error codes. The typed exception hierarchy only
 * distinguishes the six that map onto HTTP semantics, so the remaining codes previously collapsed
 * into a bare {@link africa.sendbyte.exception.ApiException} with no way to branch on them. Every
 * contract code resolves to exactly one category here, which lets callers react to failure meaning,
 * such as a suppressed recipient or an unverified domain, without matching on code strings.
 */
public enum ApiErrorCategory {
    /** The account is suspended. */
    ACCOUNT,
    /** Credentials are missing, malformed, or expired. */
    AUTHENTICATION,
    /** Credentials are valid but lack permission. */
    AUTHORIZATION,
    /** The request conflicts with existing state and is not safe to repeat unchanged. */
    CONFLICT,
    /** The message content was rejected. */
    CONTENT,
    /** The sending domain is not verified. */
    DOMAIN,
    /** A token or request window has expired. */
    EXPIRATION,
    /** The target exists but the operation does not apply to it. */
    NOT_ELIGIBLE,
    /** The target does not exist. */
    NOT_FOUND,
    /** Billing or payment state blocks the request. */
    PAYMENT,
    /** A plan, test, or account quota is exhausted. */
    QUOTA,
    /** The caller exceeded a rate limit and may retry after the indicated delay. */
    RATE_LIMIT,
    /** The recipient address is suppressed or not verified. */
    RECIPIENT,
    /** The service failed or is unavailable. */
    SERVER,
    /** The request was rejected as invalid. */
    VALIDATION,
    /** The failure could not be classified. */
    UNKNOWN;

    /**
     * Resolves the category from a contract error code, falling back to the HTTP status code when
     * the code is absent or not one the contract defines.
     */
    public static ApiErrorCategory fromCode(final String code, final int statusCode) {
        if (code != null) {
            final ApiErrorCategory category = ApiErrorCategory.fromContractCode(code);
            if (category != null) {
                return category;
            }
        }
        return ApiErrorCategory.fromStatusCode(statusCode);
    }

    private static ApiErrorCategory fromContractCode(final String code) {
        switch (code) {
            case "validation_error":
            case "limit_exceeded":
                return VALIDATION;
            case "authentication_error":
            case "invalid_token":
                return AUTHENTICATION;
            case "authorization_error":
                return AUTHORIZATION;
            case "not_found":
            case "not_suppressed":
                return NOT_FOUND;
            case "idempotency_conflict":
            case "duplicate_request":
            case "cooldown_active":
                return CONFLICT;
            case "rate_limit_exceeded":
                return RATE_LIMIT;
            case "quota_exceeded":
            case "test_quota_exceeded":
                return QUOTA;
            case "payment_required":
                return PAYMENT;
            case "content_flagged":
                return CONTENT;
            case "domain_not_verified":
                return DOMAIN;
            case "suppressed_recipient":
            case "unverified_recipient":
                return RECIPIENT;
            case "account_suspended":
                return ACCOUNT;
            case "not_removable":
                return NOT_ELIGIBLE;
            case "expired_token":
                return EXPIRATION;
            case "email_failed":
            case "internal_error":
            case "service_unavailable":
                return SERVER;
            default:
                return null;
        }
    }

    private static ApiErrorCategory fromStatusCode(final int statusCode) {
        switch (statusCode) {
            case 401:
                return AUTHENTICATION;
            case 402:
                return PAYMENT;
            case 403:
                return AUTHORIZATION;
            case 404:
                return NOT_FOUND;
            case 409:
                return CONFLICT;
            case 422:
                return VALIDATION;
            case 429:
                return RATE_LIMIT;
            default:
                return statusCode >= 500 ? SERVER : UNKNOWN;
        }
    }
}
