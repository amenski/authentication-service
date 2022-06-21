package it.aman.authenticationservice.dal.entity;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import it.aman.authenticationservice.dal.util.ListToStringAttributeConverter;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name = "auth_role")
@NamedQuery(name = "AuthRole.findAll", query = "SELECT e FROM AuthRole e")
public class AuthRole implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(unique = true, nullable = false)
    private int id;

    private String name;

    @Convert(converter = ListToStringAttributeConverter.class)
    private List<String> authority;

}