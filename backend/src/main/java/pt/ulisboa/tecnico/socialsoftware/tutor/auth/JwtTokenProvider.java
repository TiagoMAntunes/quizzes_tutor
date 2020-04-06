package pt.ulisboa.tecnico.socialsoftware.tutor.auth;

import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository;

import javax.servlet.http.HttpServletRequest;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;

@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);
    private UserRepository userRepository;
    private static PublicKey publicKey;
    private static PrivateKey privateKey;

    public JwtTokenProvider(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private static void generateKeys() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            privateKey = keyPair.getPrivate();
            publicKey = keyPair.getPublic();
        } catch (Exception e) {
            logger.error("Unable to generate keys");
        }
    }

    static String generateToken(User user) {
        if (publicKey == null) {
            generateKeys();
        }

        Claims claims = Jwts.claims().setSubject(String.valueOf(user.getId()));
        claims.put("role", user.getRole());

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 1000*60*60*24);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(privateKey)
                .compact();
    }

    static String getToken(HttpServletRequest req) {
        String authHeader = req.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        } else if (authHeader != null && authHeader.startsWith("AUTH")) {
            return authHeader.substring(4);
        } else if (authHeader != null) {
            return authHeader;
        }
        return "";
    }
    static int getUserId(String token) {
        try {
            return Integer.parseInt(Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(token).getBody().getSubject());
        } catch (MalformedJwtException ex) {
            logger.error("Invalkey JWT token");
        } catch (ExpiredJwtException ex) {
            logger.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            logger.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string is empty.");
        }
        throw new TutorException(AUTHENTICATION_ERROR);
    }

    Authentication getAuthentication(String token) {
        User user = this.userRepository.findById(getUserId(token)).orElseThrow(() -> new TutorException(USER_NOT_FOUND, getUserId(token)));
        return new UsernamePasswordAuthenticationToken(user, "", user.getAuthorities());
    }
}