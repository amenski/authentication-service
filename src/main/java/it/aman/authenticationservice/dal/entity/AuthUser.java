package it.aman.authenticationservice.dal.entity;

import javax.persistence.*;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * The persistent class for the eps_users database table.
 */
@Entity
@Data
@NoArgsConstructor
@Table(name = "auth_user")
public class AuthUser implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private int id; // account_id column

    private LocalDate dob;

    @Column(name = "fname")
    private String firstName;

    @Column(name = "gender")
    private String gender;

    @Column(name = "lname")
    private String lastName;

    @Column(name = "phone")
    private String phone;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "modified_by")
    private String modifiedBy;

    @Column(name = "modified_at")
    private OffsetDateTime modifiedAt;

    // this is the owning side. creates account and links user
    // to the account. A new account is expected to be saved for each user
    // Tells Hibernate to use the id column of AuthAccount as both primary key and foreign key.
    @OneToOne(cascade = CascadeType.ALL)
    @MapsId
    private AuthAccount account;
}