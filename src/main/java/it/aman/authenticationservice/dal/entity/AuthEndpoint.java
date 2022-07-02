package it.aman.authenticationservice.dal.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name = "auth_endpoint")
@NamedQuery(name = "AuthEndpoint.findAll", query = "SELECT e FROM AuthEndpoint e")
public class AuthEndpoint implements Serializable {
    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private AuthEndpointId id;

    private String permission;

    @Data
    public static class AuthEndpointId implements Serializable {
        private static final long serialVersionUID = 8062648192618222610L;

        @Column(name = "service_name")
        private String serviceName;

        private String endpoint;

        @Column(name = "http_method")
        private String httpMethod;
    }
}