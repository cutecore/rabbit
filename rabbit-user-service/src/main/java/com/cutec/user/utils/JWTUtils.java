package com.cutec.user.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Slf4j
@Component
public class JWTUtils {

    @Value("${jwt-expiration}")
    private Long expiration;

    private Algorithm algorithm = null;

    public JWTUtils(@Value("${jwt}") String jwt) {
        this.algorithm = Algorithm.HMAC256(jwt);
    }

    public String create(Integer userId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deadLocalTime = now.plusMinutes(expiration);
        Date deadline = Date.from(deadLocalTime.atZone(ZoneId.systemDefault()).toInstant());
        return JWT.create().
                withIssuer("cutec").
                withIssuedAt(new Date()).
                withExpiresAt(deadline)
                .withClaim("userId", userId).
                sign(algorithm);
    }

    public Integer parse(String token) {
        try {
            JWTVerifier verifier = JWT.require(algorithm)
                    .build();
            DecodedJWT jwt = verifier.verify(token);
            return jwt.getClaim("userId").asInt();
        } catch (TokenExpiredException exception) {
            return -1;
        } catch (Exception e) {
            return null;
        }
    }
}
