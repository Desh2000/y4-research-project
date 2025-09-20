package com.reserch.mano.service.serviceImpl;

import org.springframework.security.core.userdetails.UserDetails;

/**
 * Service interface for handling JSON Web Tokens (JWT).
 * Defines the contract for generating, validating, and extracting information from JWTs.
 */
public interface JwtService {

    /**
     * Extracts the username (subject) from a given JWT.
     *
     * @param token The JWT.
     * @return The username contained within the token.
     */
    String extractUserName(String token);

    /**
     * Generates a new JWT for the given user details.
     *
     * @param userDetails The user details to be included in the token.
     * @return The generated JWT as a string.
     */
    String generateToken(UserDetails userDetails);

    /**
     * Validates a JWT.
     * Checks if the token belongs to the user and if it has not expired.
     *
     * @param token The JWT to validate.
     * @param userDetails The user details to validate against.
     * @return true if the token is valid, false otherwise.
     */
    boolean isTokenValid(String token, UserDetails userDetails);
}

