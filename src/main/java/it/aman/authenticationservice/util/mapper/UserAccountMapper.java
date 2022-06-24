package it.aman.authenticationservice.util.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import it.aman.authentication_service.client.model.ModelUser;
import it.aman.authenticationservice.dal.entity.AuthUser;

@Mapper(componentModel = "spring")
public interface UserAccountMapper {

    @Mapping(target = "gender", source = "gender")
    @Mapping(target = "account.epsAccountType", source = "account.accountType")
    @Mapping(target = "account.email", source = "account.email")
    @Mapping(target = "account.id", ignore = true)
    @Mapping(target = "account.avatar", ignore = true)
    //TODO add address, if needed
    AuthUser map(ModelUser dto);
    
//    @InheritInverseConfiguration(name = "map")
    ModelUser map(AuthUser entity);
}
