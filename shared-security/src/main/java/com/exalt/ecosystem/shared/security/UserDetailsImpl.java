package com.exalt.ecosystem.shared.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * UserDetails implementation for Spring Security.
 * Provides user information and authorities for authentication and authorization.
 * 
 * @author Exalt Development Team
 * @since 1.0.0
 */
public class UserDetailsImpl implements UserDetails {
    
    private static final long serialVersionUID = 1L;
    
    private UUID id;
    private String username;
    private String email;
    private String userType;
    
    @JsonIgnore
    private String password;
    
    private Collection<? extends GrantedAuthority> authorities;
    
    private boolean accountNonExpired = true;
    private boolean accountNonLocked = true;
    private boolean credentialsNonExpired = true;
    private boolean enabled = true;
    
    /**
     * Default constructor.
     */
    public UserDetailsImpl() {
    }
    
    /**
     * Constructor with all fields.
     * 
     * @param id User ID
     * @param username Username
     * @param email Email address
     * @param password Password (will be ignored in JSON)
     * @param userType User type
     * @param authorities User authorities
     */
    public UserDetailsImpl(UUID id, String username, String email, String password, 
                          String userType, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.userType = userType;
        this.authorities = authorities;
    }
    
    /**
     * Constructor with account status fields.
     * 
     * @param id User ID
     * @param username Username
     * @param email Email address
     * @param password Password
     * @param userType User type
     * @param authorities User authorities
     * @param accountNonExpired Account not expired
     * @param accountNonLocked Account not locked
     * @param credentialsNonExpired Credentials not expired
     * @param enabled Account enabled
     */
    public UserDetailsImpl(UUID id, String username, String email, String password, 
                          String userType, Collection<? extends GrantedAuthority> authorities,
                          boolean accountNonExpired, boolean accountNonLocked, 
                          boolean credentialsNonExpired, boolean enabled) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.userType = userType;
        this.authorities = authorities;
        this.accountNonExpired = accountNonExpired;
        this.accountNonLocked = accountNonLocked;
        this.credentialsNonExpired = credentialsNonExpired;
        this.enabled = enabled;
    }
    
    /**
     * Creates UserDetailsImpl from user data.
     * 
     * @param id User ID
     * @param username Username
     * @param email Email address
     * @param password Password
     * @param userType User type
     * @param roles List of role names
     * @return UserDetailsImpl instance
     */
    public static UserDetailsImpl create(UUID id, String username, String email, 
                                        String password, String userType, List<String> roles) {
        List<GrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
        
        return new UserDetailsImpl(id, username, email, password, userType, authorities);
    }
    
    /**
     * Creates UserDetailsImpl with account status.
     * 
     * @param id User ID
     * @param username Username
     * @param email Email address
     * @param password Password
     * @param userType User type
     * @param roles List of role names
     * @param accountNonExpired Account not expired
     * @param accountNonLocked Account not locked
     * @param credentialsNonExpired Credentials not expired
     * @param enabled Account enabled
     * @return UserDetailsImpl instance
     */
    public static UserDetailsImpl create(UUID id, String username, String email, 
                                        String password, String userType, List<String> roles,
                                        boolean accountNonExpired, boolean accountNonLocked, 
                                        boolean credentialsNonExpired, boolean enabled) {
        List<GrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
        
        return new UserDetailsImpl(id, username, email, password, userType, authorities,
                                  accountNonExpired, accountNonLocked, credentialsNonExpired, enabled);
    }
    
    /**
     * Gets the user ID.
     * 
     * @return User ID
     */
    public UUID getId() {
        return id;
    }
    
    /**
     * Sets the user ID.
     * 
     * @param id User ID
     */
    public void setId(UUID id) {
        this.id = id;
    }
    
    /**
     * Gets the email address.
     * 
     * @return Email address
     */
    public String getEmail() {
        return email;
    }
    
    /**
     * Sets the email address.
     * 
     * @param email Email address
     */
    public void setEmail(String email) {
        this.email = email;
    }
    
    /**
     * Gets the user type.
     * 
     * @return User type
     */
    public String getUserType() {
        return userType;
    }
    
    /**
     * Sets the user type.
     * 
     * @param userType User type
     */
    public void setUserType(String userType) {
        this.userType = userType;
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }
    
    /**
     * Sets the authorities.
     * 
     * @param authorities User authorities
     */
    public void setAuthorities(Collection<? extends GrantedAuthority> authorities) {
        this.authorities = authorities;
    }
    
    @Override
    public String getPassword() {
        return password;
    }
    
    /**
     * Sets the password.
     * 
     * @param password Password
     */
    public void setPassword(String password) {
        this.password = password;
    }
    
    @Override
    public String getUsername() {
        return username;
    }
    
    /**
     * Sets the username.
     * 
     * @param username Username
     */
    public void setUsername(String username) {
        this.username = username;
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }
    
    /**
     * Sets account non-expired status.
     * 
     * @param accountNonExpired Account non-expired status
     */
    public void setAccountNonExpired(boolean accountNonExpired) {
        this.accountNonExpired = accountNonExpired;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }
    
    /**
     * Sets account non-locked status.
     * 
     * @param accountNonLocked Account non-locked status
     */
    public void setAccountNonLocked(boolean accountNonLocked) {
        this.accountNonLocked = accountNonLocked;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }
    
    /**
     * Sets credentials non-expired status.
     * 
     * @param credentialsNonExpired Credentials non-expired status
     */
    public void setCredentialsNonExpired(boolean credentialsNonExpired) {
        this.credentialsNonExpired = credentialsNonExpired;
    }
    
    @Override
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Sets enabled status.
     * 
     * @param enabled Enabled status
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    /**
     * Checks if user has a specific role.
     * 
     * @param role Role name (without ROLE_ prefix)
     * @return true if user has the role
     */
    public boolean hasRole(String role) {
        return authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_" + role));
    }
    
    /**
     * Checks if user has any of the specified roles.
     * 
     * @param roles Role names (without ROLE_ prefix)
     * @return true if user has any of the roles
     */
    public boolean hasAnyRole(String... roles) {
        for (String role : roles) {
            if (hasRole(role)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Gets list of role names (without ROLE_ prefix).
     * 
     * @return List of role names
     */
    public List<String> getRoles() {
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> auth.startsWith("ROLE_"))
                .map(auth -> auth.substring(5)) // Remove "ROLE_" prefix
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserDetailsImpl)) return false;
        UserDetailsImpl that = (UserDetailsImpl) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "UserDetailsImpl{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", userType='" + userType + '\'' +
                ", authorities=" + authorities +
                ", accountNonExpired=" + accountNonExpired +
                ", accountNonLocked=" + accountNonLocked +
                ", credentialsNonExpired=" + credentialsNonExpired +
                ", enabled=" + enabled +
                '}';
    }
}