package it.aman.authenticationservice.controller;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.jsonwebtoken.JwtException;
import io.swagger.annotations.ApiParam;
import it.aman.authentication_service.client.api.AccountApi;
import it.aman.authentication_service.client.model.ModelLogin;
import it.aman.authentication_service.client.model.ResponseBase;
import it.aman.authenticationservice.service.AuthenticationServiceImpl;
import it.aman.common.StringUtils;
import it.aman.common.annotation.Loggable;
import it.aman.common.exception.ERPException;
import it.aman.common.exception.ERPExceptionEnums;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AuthenticationController extends AbstractController implements AccountApi {

    private final AuthenticationServiceImpl authenticationService;
    
    private final HttpServletRequest httpServletRequest;

    @Override
    @Loggable(exclusions = "body")
    public ResponseEntity<ResponseBase> authenticate(@ApiParam(value = ""  )  @Valid @RequestBody ModelLogin body) {
        ResponseBase response = null;
        Class<ResponseBase> responseClass = ResponseBase.class;
        MultiValueMap<String, String> headers = new HttpHeaders();
        HttpStatus status = HttpStatus.OK;
        try {
            String token = authenticationService.authenticate(body.getUsername(), body.getPassword());
            response = fillSuccessResponseWithMessage(responseClass, token);
        } catch (ERPException e) {
            status = e.getHttpCode();
            response = fillFailResponseEthException(responseClass, e);
        } catch (DisabledException e) {
            status = HttpStatus.NOT_FOUND;
            response = fillFailResponseEthException(responseClass, ERPExceptionEnums.ACCOUNT_NOT_FOUND.get());
        } catch (AuthenticationException e) {
            status = HttpStatus.UNAUTHORIZED;
            response = fillFailResponseEthException(responseClass, ERPExceptionEnums.USERNAME_OR_PASSWORD_INCORECT.get());
        } catch (Exception e) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            response = fillFailResponseGeneric(responseClass);
        }

        return new ResponseEntity<>(response, headers, status);
    }
    
    @Override
    @Loggable
    public ResponseEntity<ResponseBase> validateToken() {
        ResponseBase response = null;
        Class<ResponseBase> responseClass = ResponseBase.class;
        MultiValueMap<String, String> headers = new HttpHeaders();
        HttpStatus status = HttpStatus.OK;
        try {
            final String username = authenticationService.validateToken(httpServletRequest);
            if(!StringUtils.isBlank(username)) {
                response = fillSuccessResponseWithMessage(responseClass, username);
            } else {
                response = fillFailResponseEthException(responseClass,  ERPExceptionEnums.UNAUTHORIZED_EXCEPTION.get());
            }
        } catch (ERPException e) {
            status = e.getHttpCode();
            response = fillFailResponseEthException(responseClass, e);
        } 
        catch(DisabledException e) {
            status = HttpStatus.NOT_FOUND;
            response = fillFailResponseEthException(responseClass, ERPExceptionEnums.ACCOUNT_NOT_FOUND.get());
        } catch(AuthenticationException e) {
            status = HttpStatus.UNAUTHORIZED;
            response = fillFailResponseEthException(responseClass, ERPExceptionEnums.USERNAME_OR_PASSWORD_INCORECT.get());
        }  catch(JwtException e) {
            status = HttpStatus.UNAUTHORIZED;
            response = fillFailResponseEthException(responseClass, ERPExceptionEnums.UNAUTHORIZED_EXCEPTION.get());
        } catch (Exception e) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            response = fillFailResponseGeneric(responseClass);
        }

        return new ResponseEntity<>(response, headers, status);
    }
}
