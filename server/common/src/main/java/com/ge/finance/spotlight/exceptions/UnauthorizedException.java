package com.ge.finance.spotlight.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.UNAUTHORIZED, reason = "Not authorized")
public class UnauthorizedException extends RuntimeException {

	/**
	 *
	 */
	private static final long serialVersionUID = 8649386969471885711L;
}
