package it.aman.authenticationservice.dal.entity;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * The persistent class for the eps_account database table.
 */
@Entity
@Data
@NoArgsConstructor
@Table(name = "auth_account")
@NamedQuery(name = "AuthAccount.findAll", query = "SELECT e FROM AuthAccount e")
public class AuthAccount implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String avatar;

    @Column(nullable = false, unique = true, length = 50)
    private String email;

    @Column(name = "last_access", nullable = false)
    private OffsetDateTime lastAccess;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "modified_by")
    private String modifiedBy;

    @Column(name = "modified_at")
    private OffsetDateTime modifiedAt;

    @Column(name = "account_type")
    private String epsAccountType;

    private boolean enabled;

    @OneToOne(mappedBy = "account", cascade = CascadeType.ALL)
    private AuthUser user;

    //bi-directional many-to-many association to EpsRole
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name = "auth_account_roles",
            joinColumns = {@JoinColumn(name = "account_id", nullable = false)},
            inverseJoinColumns = {@JoinColumn(name = "role_id", nullable = false)})
    private Set<AuthRole> epsRoles;
}