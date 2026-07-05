package com.adgr.smartcommerce.admin.auth.service;

import com.adgr.smartcommerce.admin.auth.config.JwtProperties;
import com.adgr.smartcommerce.admin.auth.dto.LoginToken;
import com.adgr.smartcommerce.admin.auth.dto.LoginUserPrincipal;
import com.adgr.smartcommerce.admin.user.entity.SysUser;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {

    private final JwtProperties jwtProperties;

    public JwtTokenService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public LoginToken createToken(SysUser user, List<String> roleCodes) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plusSeconds(jwtProperties.getExpirationSeconds());
        Algorithm algorithm = Algorithm.HMAC256(jwtProperties.getSecret());
        String token = JWT.create()
                .withIssuer(jwtProperties.getIssuer())
                .withSubject(String.valueOf(user.getId()))
                .withIssuedAt(Date.from(issuedAt))
                .withExpiresAt(Date.from(expiresAt))
                .withClaim("userId", user.getId())
                .withClaim("username", user.getUsername())
                .withClaim("userType", user.getUserType())
                .withArrayClaim("roles", roleCodes.toArray(String[]::new))
                .sign(algorithm);
        return new LoginToken(token, expiresAt);
    }

    public LoginUserPrincipal parseToken(String token) {
        Algorithm algorithm = Algorithm.HMAC256(jwtProperties.getSecret());
        DecodedJWT decodedJWT = JWT.require(algorithm)
                .withIssuer(jwtProperties.getIssuer())
                .build()
                .verify(token);

        List<String> roles = decodedJWT.getClaim("roles").asList(String.class);
        return new LoginUserPrincipal(
                decodedJWT.getClaim("userId").asLong(),
                decodedJWT.getClaim("username").asString(),
                decodedJWT.getClaim("userType").asInt(),
                roles == null ? List.of() : List.copyOf(roles));
    }
}
