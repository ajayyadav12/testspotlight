package com.ge.finance.spotlight.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.FORBIDDEN)
public class ForbiddenException extends RuntimeException {

	/**
	 *
	 */
	private static final long serialVersionUID = -3442933296676805102L;

	 public ForbiddenException() {
        super("You are forbidden to see this data");
    }

    public ForbiddenException(String message) {
        super(message);
    }
}
