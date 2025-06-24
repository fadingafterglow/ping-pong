package ua.edu.ukma.cs.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import ua.edu.ukma.cs.entity.UserEntity;

import java.util.Properties;

public class JwtServices {

    private static final String USER_ID_CLAIM = "USER_ID";
    private final Algorithm algorithm;

    public JwtServices(Properties properties) {
        String secret = properties.getProperty("jwt.secret");
        this.algorithm = Algorithm.HMAC256(secret);
    }

    public String generateToken(UserEntity user) {
        return JWT.create()
                .withSubject(user.getUsername())
                .withClaim(USER_ID_CLAIM, user.getId())
                .sign(algorithm);
    }

    public SecurityContext verifyToken(String token) throws JWTVerificationException {
        DecodedJWT jwt = JWT.require(algorithm)
                .withClaimPresence(USER_ID_CLAIM)
                .build()
                .verify(token);
        return new SecurityContext(jwt.getClaim(USER_ID_CLAIM).asInt(), jwt.getSubject());
    }
}
