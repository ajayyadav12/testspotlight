package com.ge.finance.spotlight.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.CONFLICT, reason = "Request could not be performed due to a conflict")
public class ConflictException extends RuntimeException {
}
