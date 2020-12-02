package com.ge.finance.spotlight.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

public class JWTAuthorizationFilter extends BasicAuthenticationFilter {

    private String secret;

    public JWTAuthorizationFilter(AuthenticationManager authenticationManager, String secret) {
        super(authenticationManager);
        this.secret = secret;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {
        String header = req.getHeader(Constants.HEADER);
        if (header == null) {
            chain.doFilter(req, res);
            return;
        }
        Optional<UsernamePasswordAuthenticationToken> authenticationToken = getAuthentication(req);
        if (authenticationToken.isPresent()) {
            SecurityContextHolder.getContext().setAuthentication(authenticationToken.get());
        }
        chain.doFilter(req, res);
    }

    private Optional<UsernamePasswordAuthenticationToken> getAuthentication(HttpServletRequest request) {
        String token = request.getHeader(Constants.HEADER);
        if (token != null) {
            String processId = JWT.require(Algorithm.HMAC512(secret.getBytes())).build().verify(token.replace("Bearer ", "")).getSubject();
            if (processId != null) {
                return Optional.of(new UsernamePasswordAuthenticationToken(processId, null, Collections.emptyList()));
            }
        }
        return Optional.empty();
    }

}
