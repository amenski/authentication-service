package it.aman.authenticationservice.controller;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiParam;
import it.aman.authenticationservice.annotation.Loggable;
import it.aman.authenticationservice.config.exception.AuthException;
import it.aman.authenticationservice.service.UserAccountServiceImpl;
import it.aman.authenticationservice.swagger.api.UsersApi;
import it.aman.authenticationservice.swagger.model.RequestSaveUser;
import it.aman.authenticationservice.swagger.model.ResponseBase;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class UserAccountController extends AbstractController implements UsersApi {
    
    private final UserAccountServiceImpl userService;
    
    @Override
    @Loggable
    public ResponseEntity<ResponseBase> createNewUser(@ApiParam(value = ""  )  @Valid @RequestBody RequestSaveUser userInfo) {
        HttpStatus status = HttpStatus.OK;
        Class<ResponseBase> responseClass = ResponseBase.class;
        ResponseBase response = new ResponseBase();
        
        try {
            userService.saveUserDetails(userInfo.getSchema());
            response = fillSuccessResponse(response);
        } catch (AuthException e) {
            status = e.getHttpCode();
            response = fillFailResponseEthException(responseClass, e);
        } catch (Exception e) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            response = fillFailResponseGeneric(responseClass);
        }
        return new ResponseEntity<>(response, status);
    }

}
