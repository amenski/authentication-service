package it.aman.authenticationservice.util;

import it.aman.authenticationservice.config.exception.AuthException;

public class AuthConstants {

    // LOGGER
    public static final String PARAMETER_2 = "{} {}";
    public static final String PARAMETER_3 = "{} {} {}";
    public static final String METHOD_START = " method start.";
    public static final String METHOD_END = " method end.";
    public static final String INPUT_PARAMETER = " input parameter ";

    public static final int SUCCESS = 0;
    public static final int FAIL = 1;

    // auth
    public static final String ACCOUNT_ACTIVE = "ACTIVE";
    public static final String ACCOUNT_NOT_FOUND = "User account not found, Please check again!";
    public static final String INSUFFICENT_PERMISSION = "Insufficient permission to perform this action.";

    public static final int AUTH_TOKEN_VALIDITY = 1000 * 60 * 30; // 30min
    public static final String AUTH_TOKEN_PREFIX = "Bearer ";
    public static final String AUTH_HEADER_STRING = "Authorization";

    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    public static final String X_REQUESTED_URL = "X-Requested-Url";
    public static final String TRANSACTION_ID_KEY = "X-Transaction-Id";

    public static final String ANONYMOUS_USER = "anonymousUser";

    // article
    public static final int MIN_ABSTRACT_TEXT_SIZE = 100;
    public static final int MAX_ABSTRACT_TEXT_SIZE = 350;
    public static final int MIN_TITLE_TEXT_SIZE = 5;
    public static final int MAX_TITLE_TEXT_SIZE = 30;
    public static final int MAX_RANDOM_ARTICLE_SIZE = 10;

    // converter
    public static final String ATTRIBUTE_SEPARATOR = "#";

    private AuthConstants() throws AuthException {
        throw new AuthException("Utility class.");
    }
}
