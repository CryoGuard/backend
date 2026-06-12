package com.example.cryoguard.iam.domain.model.aggregates;

import com.example.cryoguard.iam.domain.model.entities.Role;
import com.example.cryoguard.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * User aggregate root
 * <p>
 *     This class represents the aggregate root for the User entity.
 *     Adapted for CryoGuard with additional fields: email, status, lastLogin.
 * </p>
 *
 * @see AuditableAbstractAggregateRoot
 */
@Entity
@Table(name = "users")
public class User extends AuditableAbstractAggregateRoot<User> {

    @NotBlank
    @Size(max = 50)
    @Column(unique = true, nullable = false)
    private String username;

    @NotBlank
    @Size(max = 120)
    @Column(nullable = false)
    private String password;

    @NotBlank
    @Size(max = 255)
    @Column(unique = true, nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private UserStatus status = UserStatus.ACTIVE;

    @Column
    private Date lastLogin;

    @Size(max = 20)
    @Column(length = 20, nullable = true)
    private String telefono;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles;

    public User() {
        this.roles = new HashSet<>();
    }

    public User(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.status = UserStatus.ACTIVE;
        this.roles = new HashSet<>();
    }

    public User(String username, String password, String email, List<Role> roles) {
        this(username, password, email);
        addRoles(roles);
    }

    /**
     * Add a role to the user
     * @param role the role to add
     * @return the user with the added role
     */
    public User addRole(Role role) {
        this.roles.add(role);
        return this;
    }

    /**
     * Add a list of roles to the user
     * @param roles the list of roles to add
     * @return the user with the added roles
     */
    public User addRoles(List<Role> roles) {
        var validatedRoleSet = Role.validateRoleSet(roles);
        this.roles.addAll(validatedRoleSet);
        return this;
    }

    /**
     * Update the last login timestamp
     */
    public void recordLogin() {
        this.lastLogin = new Date();
    }

    /**
     * Disable the user account
     */
    public void disable() {
        this.status = UserStatus.INACTIVE;
    }

    /**
     * Activate the user account
     */
    public void activate() {
        this.status = UserStatus.ACTIVE;
    }

    // Manual getters
    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public String getEmail() {
        return this.email;
    }

    public UserStatus getStatus() {
        return this.status;
    }

    public Date getLastLogin() {
        return this.lastLogin;
    }

    public Set<Role> getRoles() {
        return this.roles;
    }

    // Manual setters
    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public String getTelefono() {
        return this.telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    /**
     * User status enum
     */
    public enum UserStatus {
        ACTIVE,
        INACTIVE,
        LOCKED
    }

    /**
     * Lock the user account
     */
    public void lock() {
        this.status = UserStatus.LOCKED;
    }

    /**
     * Check if user is locked
     */
    public boolean isLocked() {
        return this.status == UserStatus.LOCKED;
    }
}