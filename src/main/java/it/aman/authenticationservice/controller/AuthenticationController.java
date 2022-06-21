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
import it.aman.authenticationservice.annotation.Loggable;
import it.aman.authenticationservice.config.exception.AuthException;
import it.aman.authenticationservice.config.exception.AuthExceptionEnums;
import it.aman.authenticationservice.service.AuthenticationServiceImpl;
import it.aman.authenticationservice.swagger.api.AccountApi;
import it.aman.authenticationservice.swagger.model.ModelLogin;
import it.aman.authenticationservice.swagger.model.ResponseBase;
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
        } catch (AuthException e) {
            status = e.getHttpCode();
            response = fillFailResponseEthException(responseClass, e);
        } catch (DisabledException e) {
            status = HttpStatus.NOT_FOUND;
            response = fillFailResponseEthException(responseClass, AuthExceptionEnums.ACCOUNT_NOT_FOUND.get());
        } catch (AuthenticationException e) {
            status = HttpStatus.UNAUTHORIZED;
            response = fillFailResponseEthException(responseClass, AuthExceptionEnums.USERNAME_OR_PASSWORD_INCORECT.get());
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
            boolean success = authenticationService.validateToken(httpServletRequest);
            if(success) {
                response = fillSuccessResponse(new ResponseBase());
            } else {
                response = fillFailResponseEthException(responseClass,  AuthExceptionEnums.UNAUTHORIZED_EXCEPTION.get());
            }
        } catch (AuthException e) {
            status = e.getHttpCode();
            response = fillFailResponseEthException(responseClass, e);
        } 
        catch(DisabledException e) {
            status = HttpStatus.NOT_FOUND;
            response = fillFailResponseEthException(responseClass, AuthExceptionEnums.ACCOUNT_NOT_FOUND.get());
        } catch(AuthenticationException e) {
            status = HttpStatus.UNAUTHORIZED;
            response = fillFailResponseEthException(responseClass, AuthExceptionEnums.USERNAME_OR_PASSWORD_INCORECT.get());
        }  catch(JwtException e) {
            status = HttpStatus.UNAUTHORIZED;
            response = fillFailResponseEthException(responseClass, AuthExceptionEnums.UNAUTHORIZED_EXCEPTION.get());
        } catch (Exception e) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            response = fillFailResponseGeneric(responseClass);
        }

        return new ResponseEntity<>(response, headers, status);
    }
}
