package com.ge.finance.spotlight.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
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
import java.util.stream.Collectors;

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
        Optional<UsernamePasswordAuthenticationToken> authenticationToken = getAuthentication(req);
        if (authenticationToken.isPresent()) {
            SecurityContextHolder.getContext().setAuthentication(authenticationToken.get());
        }
        chain.doFilter(req, res);
    }

    private Optional<UsernamePasswordAuthenticationToken> getAuthentication(HttpServletRequest request) {           
        String token = request.getHeader(Constants.HEADER);
        if (token != null) {            
            String sso = JWT.require(Algorithm.HMAC512(secret.getBytes())).build().verify(token.replace("Bearer ", "")).getSubject();            
            if (sso != null) {                                
                User user = userRepository.findFirstBySso(Long.parseLong(sso));
                if(user != null){                                        
                    List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(user.getRole().getDescription()));                    
                    return Optional.of(new UsernamePasswordAuthenticationToken(user.getSso(), null, authorities));                
                }                
            }
        }
        return Optional.empty();                                
    }

}
