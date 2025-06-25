package ua.edu.ukma.cs.api.filters;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;
import lombok.RequiredArgsConstructor;
import ua.edu.ukma.cs.security.JwtServices;
import ua.edu.ukma.cs.security.SecurityContext;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtTokenFilter extends Filter {
    public static final String SECURITY_CONTEXT_KEY = "SECURITY_CONTEXT_KEY";

    private final JwtServices jwtServices;

    @Override
    public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
        String token = exchange.getRequestHeaders().getFirst("Authentication");
        if (token != null) {
            try {
                SecurityContext securityContext = jwtServices.verifyToken(token);
                exchange.setAttribute(SECURITY_CONTEXT_KEY, securityContext);
            } catch (JWTVerificationException e) {
                exchange.sendResponseHeaders(403, 0);
                exchange.close();
            }
        }
        chain.doFilter(exchange);
    }

    @Override
    public String description() {
        return "Sets SecurityContext if JWT token is present";
    }
}
