package it.aman.authenticationservice.dal.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Data
@Builder
@NoArgsConstructor
@Table(name = "auth_token_storage")
@NamedQuery(name = "AuthTokenStorage.findAll", query = "SELECT e FROM AuthTokenStorage e")
public class AuthTokenStorage implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "token_string")
    private String tokenString;
    
    private Long expiration;
    
    private String owner;
    
    // @Builder
    public AuthTokenStorage(Integer id, String token, Long exp, String owner) {
        this.id = id;
        this.tokenString = token;
        this.expiration = exp;
        this.owner = owner;
    }
}