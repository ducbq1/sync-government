package org.webflux.helper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class JWTUtil {

    @Value("${fundamental.app.jwtSecret}")
    private String secret;

    @Value("${fundamental.app.jwtExpirationMs}")
    private String expirationTime;

    @Value("${fundamental.app.jwtExpirationMsRemember}")
    private String expirationTimeRemember;

    private Key key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    public String getUsernameFromToken(String token) {
        return getAllClaimsFromToken(token).getSubject();
    }

    public Date getExpirationDateFromToken(String token) {
        return getAllClaimsFromToken(token).getExpiration();
    }

    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    public String generateToken(UserDetails user, Boolean rememberMe) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(x -> !x.startsWith("MENU_"))
                .collect(Collectors.joining(",")));
        claims.put("urls", user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(x -> x.startsWith("MENU_"))
                .map(x -> x.replaceAll("MENU_", ""))
                .collect(Collectors.joining(",")));
        return doGenerateToken(claims, user.getUsername(), rememberMe);
    }

    private String doGenerateToken(Map<String, Object> claims, String username, Boolean rememberMe) {
        Long expirationTimeLong = rememberMe ? Long.parseLong(expirationTimeRemember) : Long.parseLong(expirationTime);
        final Date createdDate = new Date();
        final Date expirationDate = new Date(createdDate.getTime() + expirationTimeLong * 1000);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(createdDate)
                .setExpiration(expirationDate)
                .signWith(key)
                .compact();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        return getUsernameFromToken(token).equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

}