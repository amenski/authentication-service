package it.aman.authenticationservice.controller;

import java.util.Objects;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;

import it.aman.authenticationservice.config.exception.AuthException;
import it.aman.authenticationservice.config.exception.AuthExceptionEnums;
import it.aman.authenticationservice.swagger.model.ResponseBase;
import it.aman.authenticationservice.util.AuthConstants;

@Component
public class AbstractController {
    
    @Resource
    protected HttpServletResponse servletResponse;
    
    protected <T extends ResponseBase> T fillSuccessResponse(T response) {
        Objects.requireNonNull(response);
        response.success(true);
        response.message(null);
        response.errors(null);
        response.resultCode(AuthConstants.SUCCESS);
        response.transactionId(servletResponse.getHeader(AuthConstants.TRANSACTION_ID_KEY));

        return response;
    }

    protected <T extends ResponseBase> T fillSuccessResponseWithMessage(Class<T> response, String message) {
        T resp = getNewInstance(response);
        resp.success(true);
        resp.errors(null);
        resp.message(message);
        resp.resultCode(AuthConstants.SUCCESS);
        resp.transactionId(servletResponse.getHeader(AuthConstants.TRANSACTION_ID_KEY));

        return resp;
    }

    protected <T extends ResponseBase> T fillFailResponseEthException(Class<T> response, AuthException e) {
        T res = getNewInstance(response);
        res.success(false);
        res.errors(e.getErrors());
        res.resultCode(e.getHttpCode().value());
        res.internalCode(e.getInternalCode());
        res.message(e.getErrorMessage());
        res.transactionId(servletResponse.getHeader(AuthConstants.TRANSACTION_ID_KEY));

        return res;
    }

    protected <T extends ResponseBase> T fillFailResponseGeneric(Class<T> response1) {
        T response = getNewInstance(response1);
        response.success(false);
        response.errors(null);
        response.message(AuthExceptionEnums.UNHANDLED_EXCEPTION.get().getMessage());
        response.resultCode(AuthExceptionEnums.UNHANDLED_EXCEPTION.get().getHttpCode().value());
        response.internalCode(AuthExceptionEnums.UNHANDLED_EXCEPTION.get().getInternalCode());
        response.transactionId(servletResponse.getHeader(AuthConstants.TRANSACTION_ID_KEY));

        return response;
    }

    @SuppressWarnings("unchecked")
    private <T> T getNewInstance(Class<T> clazz) {
        T newInstance;
        try {
            newInstance = clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            newInstance = (T) new ResponseBase();
        }
        return newInstance;
    }
}
