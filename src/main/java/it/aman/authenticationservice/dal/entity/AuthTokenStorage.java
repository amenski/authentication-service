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
    
    private String token;
    
    @Column(name = "refresh_token")
    private String refreshToken;
    
    private Long expiration; // refresh token expiration
    
    private String owner;
    
    @Column(name = "renew_count")
    private Integer renewCount;
    
    // @Builder
    public AuthTokenStorage(Integer id, String token, String refreshToken, Long exp, String owner, Integer count) {
        this.id = id;
        this.token = token;
        this.refreshToken = refreshToken;
        this.expiration = exp;
        this.owner = owner;
        this.renewCount = count;
    }
}