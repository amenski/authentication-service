package it.aman.authenticationservice.dal.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Data
@Table(name = "auth_endpoint")
@NamedQuery(name = "AuthEndpoint.findAll", query = "SELECT e FROM AuthEndpoint e")
public class AuthEndpoint implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String endpoint;

    @Column(name = "http_method")
    private String httpMethod;

    private String permission;
}