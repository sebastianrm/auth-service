package com.yanki.auth.domain.model;

import java.util.List;

/**
 * Represents the result of a token validation process.
 * Provides information about the token's validity, associated username, customer ID, and roles.
 * 
 * @author Sebastian Retamal
 * @contact semorale@emeal.nttdata.com
 */
public class TokenValidationResult {
    private boolean valid;
    private String username;
    private String customerId;
    private List<String> roles;

    public TokenValidationResult(boolean valid, String username, String customerId, List<String> roles) {
        this.valid = valid;
        this.username = username;
        this.customerId = customerId;
        this.roles = roles;
    }

    /**
     * Checks if the token is valid.
     * 
     * @return true if the token is valid, false otherwise
     */
    public boolean valid() {
        return valid;
    }

    /**
     * Retrieves the username associated with the token.
     * 
     * @return the username
     */
    public String username() {
        return username;
    }

    /**
     * Retrieves the customer ID associated with the token.
     * 
     * @return the customer ID
     */
    public String customerId() {
        return customerId;
    }

    /**
     * Retrieves the roles associated with the token.
     * 
     * @return a list of roles
     */
    public List<String> roles() {
        return roles;
    }
}
