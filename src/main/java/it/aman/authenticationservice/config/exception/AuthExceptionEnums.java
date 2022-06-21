package it.aman.authenticationservice.config.exception;

import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * a supplier of custom exceptions
 */
public enum AuthExceptionEnums implements Supplier<AuthException> {

	VALIDATION_EXCEPTION(new AuthException(HttpStatus.BAD_REQUEST , 400001, "Something went wrong, please contact system administrator.")),
	INSUFFICIENT_EXCEPTION(new AuthException(HttpStatus.BAD_REQUEST , 400002, "Insufficient permission.")),
	USERNAME_TAKEN_EXCEPTION(new AuthException(HttpStatus.BAD_REQUEST , 400003, "User-name/Email already in use, please try with a new one.")),
    WRONG_FILE_TYPE_EXCEPTION(new AuthException(HttpStatus.BAD_REQUEST, 400004, "Wrong File type or file type not defined.")),
    INVALID_FIELD_VALUE_EXCEPTION(new AuthException(HttpStatus.BAD_REQUEST, 400005, "Invalid field value.")),

	ITEM_NOT_FOUND(new AuthException(HttpStatus.NOT_FOUND , 404001, "Not found.")),
	USER_NOT_FOUND(new AuthException(HttpStatus.NOT_FOUND , 404002, "User not found.")),
	ACCOUNT_NOT_FOUND(new AuthException(HttpStatus.NOT_FOUND , 404003, "Account does not exist or is not active.")),
	ROLE_NOT_FOUND(new AuthException(HttpStatus.NOT_FOUND , 404004, "User role not found.")),
	
	UNAUTHORIZED_EXCEPTION(new AuthException(HttpStatus.UNAUTHORIZED , 401001, "Unauthorized!!")),
	USERNAME_OR_PASSWORD_INCORECT(new AuthException(HttpStatus.UNAUTHORIZED , 401002, "User-name or password incorrect!")),
	AUTHENTICATION_DATA_REQUIRED(new AuthException(HttpStatus.UNAUTHORIZED , 401003, "Authentication data is requred, please try again!!")),
	
	FORBIDDEN_EXCEPTION(new AuthException(HttpStatus.FORBIDDEN , 403001, "Forbidden to access this resource!")),
	
	UNHANDLED_EXCEPTION(new AuthException(HttpStatus.INTERNAL_SERVER_ERROR , 500001, "Unhandled exception has occured."));
	
	
	/// ======   ======///
	private AuthException e;
	
	/**
	 * needed to put exceptions inside the enums
	 * @param ex
	 */
	private AuthExceptionEnums(AuthException ex) {
		this.e = ex;
	}
	
	public void message(String message){
		e.setErrorMessage(message);
	}
	/**
	 * gets the AuthException instance triggered by the enums
	 * @param e
	 * @return AuthException of defined enums
	 */
	@Override
	public AuthException get() {
		return e;
	}
	
	public static AuthException from(final String from) {
	    for(AuthExceptionEnums enums : AuthExceptionEnums.values()) {
	        if(from.equals(enums.name())) return enums.get(); 
	    }
	    return AuthExceptionEnums.UNHANDLED_EXCEPTION.get();
	}
	
	// validate internal error codes
    static {
        final int errorCodeLength = 6;
        List<List<AuthExceptionEnums>> duplicateCodes = Arrays.stream(AuthExceptionEnums.values())
                .collect(Collectors.groupingBy(e -> e.get().getInternalCode())).values().stream().filter(e -> e.size() > 1)
                .collect(Collectors.toList());

        if (!duplicateCodes.isEmpty()) {
            throw new IllegalStateException("Duplicate error codes found: " + duplicateCodes);

        }

        for (AuthExceptionEnums e : AuthExceptionEnums.values()) {
            if (String.valueOf(e.get().getInternalCode()).length() != errorCodeLength) {
                throw new IllegalStateException(e + " -  Error code must have " + errorCodeLength + " digits");
            }
        }

        for (AuthExceptionEnums e : AuthExceptionEnums.values()) {
            if (!String.valueOf(e.get().getInternalCode()).substring(0, 3).equals(String.valueOf(e.get().getHttpCode().value()))) {
                throw new IllegalStateException(e + " - Error code prefix does not match http code");
            }
        }
    }
}
