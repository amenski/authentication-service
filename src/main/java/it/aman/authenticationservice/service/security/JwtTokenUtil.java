package it.aman.authenticationservice.service.security;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import it.aman.common.annotation.Loggable;
import it.aman.common.util.ERPConstants;

/**
 * 
 * @author prg
 *
 */
@Service
public class JwtTokenUtil {

    private Logger logger = LoggerFactory.getLogger(JwtTokenUtil.class);
    
    private static final String APP_SECRET = "7rlJFfn3mavKBcFV74G4sVasuURQm5yy4cF1IwgtcTTGn1EF1pOiGngTMV5poTg";
    
    private static final SignatureAlgorithm SIGNATURE_ALGORITHM = SignatureAlgorithm.HS512;
    
    @Autowired
    private Environment enviroment;
    
    @Autowired
    private HttpServletResponse httpServletResponse;
    
    @Loggable
	public String generateToken(UserPrincipal userDetails) {
        try {
    		if(Objects.isNull(userDetails)) return StringUtils.EMPTY; //On validation empty string should fail
    		
    		Map<String, Object> claims = new HashMap<>();
    		//claims.put() also suffices, computeIfAbset() is a must when there is a obj nesting or collection in the map for a key
    		claims.computeIfAbsent("sub", val -> userDetails.getUsername());
    		claims.computeIfAbsent("permissions", val -> userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));
    		return doGenerateToken(claims, APP_SECRET);
        } catch(Exception e) {
            throw e;
        }
	}

    @Loggable
	public boolean verifyToken(String token, UserDetails userDetails) {
		try {
			Claims claims = getAllClaims(token, APP_SECRET);
			final String username = claims.getSubject();
			if(StringUtils.isBlank(username) || !username.equals(userDetails.getUsername()))
				throw new AccessDeniedException(ERPConstants.INSUFFICENT_PERMISSION);
			
			if(Boolean.TRUE.equals(isTokenExpired(token)))
			    throw new AccessDeniedException(ERPConstants.INSUFFICENT_PERMISSION);
			
			return true;
		} catch (Exception e) {
			throw new AccessDeniedException(ERPConstants.INSUFFICENT_PERMISSION);
		}
	}
	
	// while creating the token -
	
	// 1. Define claims of the token, like Issuer, Expiration, Subject, and the
	// ID
	
	// 2. Sign the JWT using the HS512 algorithm and secret key.
	
	// 3. According to JWS Compact
	// Serialization(https://tools.ietf.org/html/draft-ietf-jose-json-web-signature-41#section-3.1)
	
	// compaction of the JWT to a URL-safe string
    private String doGenerateToken(Map<String, Object> claims, String secretKey) {
        Date exp = new Date(System.currentTimeMillis() + ERPConstants.AUTH_TOKEN_VALIDITY);

        byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(secretKey);
        Key signingKey = new SecretKeySpec(apiKeySecretBytes, SIGNATURE_ALGORITHM.getJcaName());

        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(exp)
                .signWith(SIGNATURE_ALGORITHM, signingKey)
                .compact();
        
        // log for non prod
        if (StringUtils.equalsAny(enviroment.getProperty("spring.profiles.active"), "dev", "development")) {
            logger.debug(ERPConstants.PARAMETER_2, "doGenerateToken()", token);
        }
        // http-only cookie
        addCookieToResponse(token, ERPConstants.AUTH_TOKEN_VALIDITY / 1000); // adapt to FE date format/length
        return token;
    }
	
    private Claims getAllClaims(String token, String appSecret) {
        try {
            Jwt<?, ?> jws = Jwts.parser().setSigningKey(DatatypeConverter.parseBase64Binary(appSecret)).parse(token);

            // Validate algorithm
            // https://auth0.com/blog/a-look-at-the-latest-draft-for-jwt-bcp/
            if (jws instanceof Jws) {
                if (SIGNATURE_ALGORITHM != SignatureAlgorithm.forName(((JwsHeader<?>) jws.getHeader()).getAlgorithm())) {
                    throw new MalformedJwtException("Algorithm must be " + SIGNATURE_ALGORITHM);
                }
            } else {
                throw new RuntimeException("Unknown jws format.");
            }
            return (Claims) jws.getBody();
        } catch (Exception e) {
            logger.error("Error retrieving claiims from token.");
            throw e;
        }
    }
	
	private Boolean isTokenExpired(String token) {
        Date expirationDate = getAllClaims(token, APP_SECRET).getExpiration();
        return expirationDate.before(new Date());
    }
    
	@Loggable
    public Object extractClaim(final String authToken, final String claim) {
        switch (claim) {
        case "sub":
            return getAllClaims(authToken, APP_SECRET).getSubject();
        case "permissions":
            return getAllClaims(authToken, APP_SECRET).get("permissions", Object.class);
        case "exp":
            return getAllClaims(authToken, APP_SECRET).get("exp", Date.class);
        default:
            logger.error("Unknown claim name. ");
            return null;
        }
    }

    @Loggable
    public void addCookieToResponse(final String token, int authTokenValidity) {
        Cookie cookie = new Cookie("token", token);
        cookie.setHttpOnly(true);
        cookie.setMaxAge(authTokenValidity);
        cookie.setPath("/");

        httpServletResponse.addCookie(cookie);
    }
}
