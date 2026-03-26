package com.example.payflow.merchant.api;

import com.example.payflow.merchant.MerchantAlreadyExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

@RestControllerAdvice
class MerchantExceptionHandler {

    @ExceptionHandler(MerchantAlreadyExistsException.class)
    ProblemDetail handleMerchantAlreadyExists(MerchantAlreadyExistsException ex) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setType(URI.create("https://payflow.io/errors/merchant-already-exists"));
        return problem;
    }
}
