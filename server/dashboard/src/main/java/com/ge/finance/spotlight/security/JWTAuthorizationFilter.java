package com.ge.finance.spotlight.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.ge.finance.spotlight.exceptions.UnauthorizedException;
import com.ge.finance.spotlight.models.User;
import com.ge.finance.spotlight.repositories.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class JWTAuthorizationFilter extends BasicAuthenticationFilter {

    private String secret;
    private UserRepository userRepository;

    public JWTAuthorizationFilter(AuthenticationManager authenticationManager, UserRepository userRepository, String secret) {
        super(authenticationManager);

        this.userRepository = userRepository;
        this.secret = secret;
    }

    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {
        String header = req.getHeader(Constants.HEADER);
        if (header == null) {
            chain.doFilter(req, res);
            return;
        }
        SecurityContextHolder.getContext().setAuthentication(getAuthentication(req));
        chain.doFilter(req, res);
    }

    private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {
        String token = request.getHeader(Constants.HEADER);
        if (token != null) {
            try {
                String sso = JWT.require(Algorithm.HMAC512(secret.getBytes())).build().verify(token.replace("Bearer ", "")).getSubject();
                if (sso != null) {
                    User user = userRepository.findFirstBySso(Long.parseLong(sso));
                    if (user != null) {
                        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(user.getRole().getDescription()));
                        return new UsernamePasswordAuthenticationToken(user.getSso(), null, authorities);
                    }
                }
            } catch (SignatureVerificationException e) {
                e.printStackTrace();
            }
        }
        UsernamePasswordAuthenticationToken unauthenticated = new UsernamePasswordAuthenticationToken(null, null);
        unauthenticated.setAuthenticated(false);
        return unauthenticated;
    }

}
