package com.ge.finance.spotlight.controllers;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.ge.finance.spotlight.exceptions.NotFoundException;
import com.ge.finance.spotlight.exceptions.UnauthorizedException;
import com.ge.finance.spotlight.libs.GEOneHRConnection;
import com.ge.finance.spotlight.models.AuditLog;
import com.ge.finance.spotlight.models.ProcessUser;
import com.ge.finance.spotlight.models.Role;
import com.ge.finance.spotlight.models.User;
import com.ge.finance.spotlight.repositories.AuditLogRepository;
import com.ge.finance.spotlight.repositories.ProcessUserRepository;
import com.ge.finance.spotlight.repositories.RoleRepository;
import com.ge.finance.spotlight.repositories.UserRepository;
import com.ge.finance.spotlight.requests.TokenRequest;
import com.ge.finance.spotlight.responses.TokenResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/security")
public class SecurityController {
    
    @Value("${client_id}") private String clientID;
    @Value("${client_secret}") private String clientSecret;
    @Value("${dash_secret}") private String secret;

    private AuditLogRepository auditLogRepository;
    private ProcessUserRepository processUserRepository;
    private RoleRepository roleRepository;
    private UserRepository userRepository;

    public SecurityController(AuditLogRepository auditLogRepository,
                              ProcessUserRepository processUserRepository,
                              RoleRepository roleRepository,
                              UserRepository userRepository) {
        this.auditLogRepository = auditLogRepository;
        this.processUserRepository = processUserRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
    }

    private User findUserBySso(Long sso) {
        return userRepository.findOptionalBySSO(sso).orElseGet(() -> {
            User user = new User();
            user.setRole(roleRepository.findById(Role.USER).orElseThrow(NotFoundException::new));
            user.setSso(sso);
            try {
                user.setName(GEOneHRConnection.getManagerSSOFromOneHR(sso, "name"));
            } catch (Exception e) {
                user.setName("Unknown");
                e.printStackTrace();
            }
            return userRepository.save(user);
        });
    }

    @PostMapping("/token")
    TokenResponse createToken(@RequestBody TokenRequest tokenRequest) {
        try {            
            String URL = "https://fssfed.ge.com/fss/as/token.oauth2?grant_type=authorization_code&code="+tokenRequest.getCode()+"&redirect_uri="+tokenRequest.getRedirectURI()+"&client_id="+this.clientID+"&client_secret="+this.clientSecret;
            String tkn = request(URL);
            DecodedJWT dJWT = JWT.decode(tkn);
            Long sso = Long.parseLong(dJWT.getSubject());
            User user = findUserBySso(sso);
            String token = JWT.create().withSubject(Long.toString(sso)).sign(HMAC512(secret.getBytes()));
            TokenResponse jwtResponse = new TokenResponse();
            String source = (tokenRequest.getSource().equals("System")) ? AuditLog.LOGIN : AuditLog.MOBILE_LOGIN;
            AuditLog userLog = new AuditLog();
            jwtResponse.setToken(token);
            jwtResponse.setUser(user);
            jwtResponse.setProcesses(processUserRepository.findByUserId(user.getId()).stream().map(ProcessUser::getProcessId).collect(Collectors.toList()));
            userLog.setUser(user);
            userLog.setModule(source);
            userLog.setLogInTime(new Date());
            auditLogRepository.save(userLog);
            return jwtResponse;
        } catch (Exception e) {
            e.printStackTrace();
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
