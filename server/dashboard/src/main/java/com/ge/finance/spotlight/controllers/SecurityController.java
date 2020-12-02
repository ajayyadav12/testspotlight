package com.ge.finance.spotlight.controllers;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.ge.finance.spotlight.exceptions.NotFoundException;
import com.ge.finance.spotlight.exceptions.UnauthorizedException;
import com.ge.finance.spotlight.libs.GEOneHRConnection;
import com.ge.finance.spotlight.models.AuditLog;
import com.ge.finance.spotlight.models.Role;
import com.ge.finance.spotlight.models.User;
import com.ge.finance.spotlight.repositories.AuditLogRepository;
import com.ge.finance.spotlight.repositories.RoleRepository;
import com.ge.finance.spotlight.repositories.UserRepository;
import com.ge.finance.spotlight.requests.TokenRequest;
import com.ge.finance.spotlight.responses.TokenResponse;
import com.ge.finance.spotlight.security.Constants;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/security")
public class SecurityController {

    private UserRepository userRepository;
    private AuditLogRepository auditLogRepository;
    private RoleRepository roleRepository;

    public SecurityController(UserRepository userRepository, AuditLogRepository auditLogRepository,
            RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
        this.roleRepository = roleRepository;
    }

    private Optional<User> findUserBySso(Long sso) throws Exception {
        User user = userRepository.findFirstBySso(sso);
        if (user == null) {
            user = new User();
            user.setRole(roleRepository.findById(Role.USER).orElseThrow(NotFoundException::new));
            user.setSso(sso);
            user.setName(GEOneHRConnection.getManagerSSOFromOneHR(sso, "name"));
            userRepository.save(user);
        }
        return Optional.of(user);
    }

    @PostMapping("/token")
    TokenResponse createToken(@RequestBody TokenRequest tokenRequest) {
        try {
            String tkn = request(tokenRequest.getUrl());
            DecodedJWT dJWT = JWT.decode(tkn);
            Long sso = Long.parseLong(dJWT.getSubject());
            User user = findUserBySso(sso).orElseThrow(NotFoundException::new);

            String token = JWT.create().withSubject(Long.toString(sso))
                    .withExpiresAt(new Date(System.currentTimeMillis() + Constants.EXPIRES))
                    .sign(HMAC512(Constants.DASHBOARD_SECRET.getBytes()));
            TokenResponse jwtResponse = new TokenResponse();
            String source = (tokenRequest.getSource().equals("System")) ? AuditLog.LOGIN : AuditLog.MOBILE_LOGIN;
            AuditLog userLog = new AuditLog();
            jwtResponse.setToken(token);
            jwtResponse.setUser(user);
            userLog.setUser(user);
            userLog.setModule(source);
            userLog.setLogInTime(new Date());
            auditLogRepository.save(userLog);
            return jwtResponse;
        } catch (Exception e) {
            throw new UnauthorizedException();
        }

    }

    /**
     * Get id_token from SSO server that authenticates user.
     * 
     * @param URL
     * @return
     * @throws Exception
     */
    private String request(String URL) throws Exception {
        String token = null;
        URL url = new URL(URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        DataOutputStream out = new DataOutputStream(conn.getOutputStream());
        out.flush();
        out.close();
        int status = conn.getResponseCode();
        if (status == 200) {
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuffer content = new StringBuffer();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            String response = content.toString();

            Pattern SUBMISSION_ID_REGEX = Pattern.compile("(?:\"id_token\":\")(.*?)(?:\")");
            Matcher matcher = SUBMISSION_ID_REGEX.matcher(response);
            if (matcher.find()) {
                token = matcher.group(1);
            }
        }
        return token;
    }
}
