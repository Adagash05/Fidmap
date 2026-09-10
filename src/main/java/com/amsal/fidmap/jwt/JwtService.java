package com.amsal.fidmap.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    @Value("${application.security.jwt.refresh-token.expiration}")
    private long refreshExpiration;

    public boolean validateToken(String token) {


//        Claims claims = extractAllClaims(token);

//        claim.getPayload().getExpiration()

        return isTokenExpired(token);



    }

    public String extractUserName(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims,T> claimResolver ) {

        Claims claims = extractAllClaims(token);

        return claimResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails ) {
        return generateToken(new HashMap<>(),userDetails);
    }

    public String generateToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails
    ) {
        extraClaims.put("tokenType", "ACCESS");
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    /*
    refresh token will be expiring after every 24 hours
    while the access token will expire after 15 minute
     */
    public String generateRefreshToken(UserDetails userDetails) {

        Map<String, Object> claims = new HashMap<>();
        claims.put("tokenType", "REFRESH");

        return buildToken(claims, userDetails, refreshExpiration);
    }

    public boolean isRefreshToken(String token) {
        String tokenType = extractClaim(token, claims -> claims.get("tokenType", String.class));
        return "REFRESH".equals(tokenType);
    }

    private String buildToken(
            Map<String,Object> extraClaims,
            UserDetails userDetails,
            long expiration
    ) {

        return Jwts
                .builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }


    public boolean isTokenValid(String token,UserDetails userDetails) {

        final String userName = extractUserName(token);

        return (userName.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }



    private boolean isTokenExpired(String token) {

        return extractExpiration(token).before(new Date());
    }



    private Date extractExpiration(String token) {

        return extractClaim(token, Claims::getExpiration);
    }

    public Claims extractAllClaims(String token) {

        return Jwts.parser()
                .verifyWith((SecretKey) getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

    }

    private Key getSigningKey() {

        byte[] keyBytes = Decoders.BASE64.decode(secretKey);

        return Keys.hmacShaKeyFor(keyBytes);
    }
}
