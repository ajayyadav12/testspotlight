package com.ge.finance.spotlight.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.UNAUTHORIZED, reason = "You are forbidden to see this data")
public class ForbiddenException extends RuntimeException {
}
