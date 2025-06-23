package ua.edu.ukma.cs.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import ua.edu.ukma.cs.entity.UserEntity;

public class JwtServices {
    // TODO: move secret
    private static final String SECRET = "thisissecret";
    private static final Algorithm ALGORITHM = Algorithm.HMAC256(SECRET);

    public String generateToken(UserEntity user) {
        return JWT.create()
                .withSubject(user.getUsername())
                .sign(ALGORITHM);
    }

    public SecurityContext verifyToken(String token) throws JWTVerificationException {
        DecodedJWT jwt = JWT.require(ALGORITHM)
                .build()
                .verify(token);
        return new SecurityContext(jwt.getSubject());
    }
}
