package it.aman.authenticationservice.controller;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.annotations.ApiParam;
import it.aman.authentication_service.client.api.UsersApi;
import it.aman.authentication_service.client.model.RequestSaveUser;
import it.aman.authentication_service.client.model.ResponseBase;
import it.aman.authentication_service.client.model.ResponseUserSingle;
import it.aman.authenticationservice.service.UserAccountServiceImpl;
import it.aman.common.annotation.Loggable;
import it.aman.common.exception.ERPException;
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
        } catch (ERPException e) {
            status = e.getHttpCode();
            response = fillFailResponseEthException(responseClass, e);
        } catch (Exception e) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            response = fillFailResponseGeneric(responseClass);
        }
        return new ResponseEntity<>(response, status);
    }

    @Override
    public ResponseEntity<ResponseBase> deleteUser(Integer userId) {
        return null;
    }

    @Override
    public ResponseEntity<ResponseUserSingle> getUserById(Integer userId) {
        return null;
    }

    @Override
    public ResponseEntity<ResponseBase> updateProfileImage(Integer userId, MultipartFile profileImage) {
        return null;
    }

}
