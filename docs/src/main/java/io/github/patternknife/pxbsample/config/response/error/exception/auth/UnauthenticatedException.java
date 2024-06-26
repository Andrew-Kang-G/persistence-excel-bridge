package io.github.patternknife.pxbsample.config.response.error.exception.auth;

import io.github.patternknife.pxbsample.config.logger.dto.ErrorMessages;
import io.github.patternknife.pxbsample.config.response.error.exception.ErrorMessagesContainedExceptionForSecurityAuthentication;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// authenticated : 401
// authorized : 403
@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
public class UnauthenticatedException extends ErrorMessagesContainedExceptionForSecurityAuthentication {
    public UnauthenticatedException() {
    }

    public UnauthenticatedException(String message) {
        super(message);
    }

    public UnauthenticatedException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnauthenticatedException(ErrorMessages errorMessages) {
        super(errorMessages);
    }

}