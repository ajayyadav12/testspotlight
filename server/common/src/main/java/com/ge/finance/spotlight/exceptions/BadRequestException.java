package com.ge.finance.spotlight.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.BAD_REQUEST, reason = "Request is missing some value")
public class BadRequestException extends RuntimeException {
}
