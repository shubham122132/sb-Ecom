package com.ecommerce.project.security.jwt;

import com.ecommerce.project.security.service.UserDetailsImpl;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${spring.app.jwtSecret}")
    private String jwtSecret;

    @Value("${spring.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    @Value("${spring.ecom.app.jwtCookieName}")
    private String jwtCookie;



//        getting JWT from header
//    public String getJwtFromHeader(HttpServletRequest request){
//        String bearerToken = request.getHeader("Authorization");
//        logger.debug("Authorization Header: {}", bearerToken);
//        if(bearerToken != null && bearerToken.startsWith("Bearer ")){
//            return bearerToken.substring(7);
//        }
//        return null;
//    }

         // getting JWT from cookies
    public String getJwtFromCookie(HttpServletRequest request){

        Cookie cookie = WebUtils.getCookie(request,jwtCookie);
        if (cookie != null ){
            return cookie.getValue();
        }
        else{
            return null;
        }
    }

        // generate JWT cookie
    public ResponseCookie generateJwtCookie(UserDetailsImpl userPrinciple){
        String jwt = generateTokenFromUsername(userPrinciple.getUsername());
        ResponseCookie cookie = ResponseCookie.from(jwtCookie,jwt)
                .path("/api")
                .maxAge(24*60*60)
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .build();
        return cookie;
    }

    // generate Clean Cookie
    public ResponseCookie getCleanJwtCookie() {
        return ResponseCookie.from(jwtCookie, "")
                .path("/api")
                .maxAge(0)
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .build();
    }

    // getting Token from username
    public String generateTokenFromUsername(String username){

        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date((new Date().getTime() + jwtExpirationMs)))
                .signWith(key())
                .compact();
    }

    // getting username from token

    public String getUsernameFromJwtToken(String token){
        return Jwts.parser()
                .verifyWith(key())
                .build().parseSignedClaims(token)
                .getPayload().getSubject();
    }
    //Generating signing key

    public SecretKey key(){
        return Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(jwtSecret)
        );
    }
    //Validate JWT Token
    public boolean validateJwtToken(String authToken){
        try{
            System.out.println("validate");
            Jwts.parser()
                    .verifyWith(key())
                    .build()
                    .parseSignedClaims(authToken)
                    .getPayload()
                    .getSubject();
            return true;
        } catch (MalformedJwtException e) {
            logger.debug("JWT validation failed: {}", e.getClass().getSimpleName());
        } catch (ExpiredJwtException e) {
            logger.error("Jwt Token is Expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("Jwt Token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("Jwt claims string is empty: {}", e.getMessage());
        }
        return false;
    }
}
