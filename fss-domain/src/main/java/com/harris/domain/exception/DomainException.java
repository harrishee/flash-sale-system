package com.harris.domain.exception;

public class DomainException extends RuntimeException {
    public DomainException(DomainErrCode domainErrCode) {
        super(domainErrCode.getErrDesc());
    }
}
