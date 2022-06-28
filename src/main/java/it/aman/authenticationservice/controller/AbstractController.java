package it.aman.authenticationservice.controller;

import java.util.Objects;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;

import it.aman.authentication_service.client.model.ResponseBase;
import it.aman.common.ERPConstants;
import it.aman.common.exception.ERPException;
import it.aman.common.exception.ERPExceptionEnums;

@Component
public class AbstractController {
    
    @Resource
    protected HttpServletResponse servletResponse;
    
    protected <T extends ResponseBase> T fillSuccessResponse(T response) {
        Objects.requireNonNull(response);
        response.success(true);
        response.message(null);
        response.errors(null);
        response.resultCode(ERPConstants.SUCCESS);
        response.transactionId(servletResponse.getHeader(ERPConstants.TRANSACTION_ID_KEY));

        return response;
    }

    protected <T extends ResponseBase> T fillSuccessResponseWithMessage(Class<T> response, String message) {
        T resp = getNewInstance(response);
        resp.success(true);
        resp.errors(null);
        resp.message(message);
        resp.resultCode(ERPConstants.SUCCESS);
        resp.transactionId(servletResponse.getHeader(ERPConstants.TRANSACTION_ID_KEY));

        return resp;
    }

    protected <T extends ResponseBase> T fillFailResponseEthException(Class<T> response, ERPException e) {
        T res = getNewInstance(response);
        res.success(false);
        res.errors(e.getErrors());
        res.resultCode(e.getHttpCode().value());
        res.internalCode(e.getInternalCode());
        res.message(e.getErrorMessage());
        res.transactionId(servletResponse.getHeader(ERPConstants.TRANSACTION_ID_KEY));

        return res;
    }

    protected <T extends ResponseBase> T fillFailResponseGeneric(Class<T> response1) {
        T response = getNewInstance(response1);
        response.success(false);
        response.errors(null);
        response.message(ERPExceptionEnums.UNHANDLED_EXCEPTION.get().getMessage());
        response.resultCode(ERPExceptionEnums.UNHANDLED_EXCEPTION.get().getHttpCode().value());
        response.internalCode(ERPExceptionEnums.UNHANDLED_EXCEPTION.get().getInternalCode());
        response.transactionId(servletResponse.getHeader(ERPConstants.TRANSACTION_ID_KEY));

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
