package com.kit.phd.oauthserver.security;



import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;

import java.util.Calendar;
import java.util.Date;



@Component
public class JwtTokenProvider {


    @Value("${app.jwtSecret}")
    private String jwtSecret;

    @Value("${app.jwtExpirationInMs}")
    private int jwtExpirationInMs;

    public String generateToken(Authentication authentication) {

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        //Date now = new Date();
        //Date expiryDate = new Date(now.getTime() + Long.valueOf(jwtExpirationInMs*1000));
        //Calendar calendar = Calendar.getInstance();
        Date expiryDate = calendarToExpireDate();

        return Jwts.builder()
                .setSubject(Long.toString(userPrincipal.getId()))
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    public Long getUserIdFromJWT(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();

        return Long.parseLong(claims.getSubject());
    }


    public boolean validateToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken);
            return true;
        } catch (SignatureException ex) {
            
            return false;
        } catch (MalformedJwtException ex) {
            
            return false;
        } catch (ExpiredJwtException ex) {
           
            return false;
        } catch (UnsupportedJwtException ex) {
        	return false;
        } catch (IllegalArgumentException ex) {
        	return false;
        }
        //return false;
    }
    
    private Date calendarToExpireDate() {
        Calendar calendar = Calendar.getInstance();
        int year=calendar.get(Calendar.YEAR);
        calendar.set(Calendar.YEAR, year+10);
        Date date = calendar.getTime(); 
        return date;
    }
}

