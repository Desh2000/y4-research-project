package com.research.mano.exception;

public class JwtTokenExpiredException extends ManoException {
    public JwtTokenExpiredException() {
        super("JWT_TOKEN_EXPIRED", "JWT token has expired");
    }
}
