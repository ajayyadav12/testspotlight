package com.ge.finance.spotlight.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.CONFLICT)
public class ConflictException extends RuntimeException {

	/**
	 *
	 */
	private static final long serialVersionUID = -1421879259284897765L;

	public ConflictException(String message) {
		super(message);
	}

	public ConflictException() {	
		super("Request could not be performed due to a conflict");
	}

	
}
