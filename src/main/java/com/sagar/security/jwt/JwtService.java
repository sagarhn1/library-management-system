package com.sagar.security.jwt;

import com.sagar.user.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.UserDetails;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${security.jwt.secret-key}")
    private String secretKey;

    @Value("${security.jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${security.jwt.expiration}")
    private long jwtExpiration;

    /**
     * Generates an access token for an authenticated user.
     *
     * @param user authenticated user
     * @return signed JWT access token
     */
    public String generateAccessToken(User user) {

        Date issuedAt = new Date();
        Date expiration = new Date(
                issuedAt.getTime() + accessTokenExpiration
        );

        return Jwts.builder()
                .subject(user.getEmail())
                .claim("userId", user.getId())
                .claim("role", user.getRole().name())
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extracts the user's email from a JWT.
     *
     * @param token JWT access token
     * @return email stored in the token subject
     */
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts a specific claim from the JWT.
     *
     * @param token JWT access token
     * @param claimsResolver function used to extract the desired claim
     * @param <T> claim type
     * @return extracted claim
     */
    public <T> T extractClaim(
            String token,
            Function<Claims, T> claimsResolver
    ) {

        Claims claims = extractAllClaims(token);

        return claimsResolver.apply(claims);
    }

    /**
     * Validates the JWT against the authenticated user's email
     * and verifies that the token has not expired.
     *
     * @param token JWT access token
     * @param userDetails authenticated user's Spring Security details
     * @return true when the token is valid
     */
    public boolean isTokenValid(
            String token,
            UserDetails userDetails
    ) {

        String email = extractEmail(token);

        return email.equals(userDetails.getUsername())
                && !isTokenExpired(token);
    }

    /**
     * Checks whether the JWT has expired.
     *
     * @param token JWT access token
     * @return true when the token is expired
     */
    private boolean isTokenExpired(String token) {

        return extractExpiration(token)
                .before(new Date());
    }

    /**
     * Extracts the expiration timestamp from the JWT.
     *
     * @param token JWT access token
     * @return expiration date
     */
    private Date extractExpiration(String token) {

        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Parses and extracts all claims from the JWT.
     *
     * @param token JWT access token
     * @return JWT claims
     */
    private Claims extractAllClaims(String token) {

        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Creates the signing key from the Base64 encoded secret.
     *
     * @return JWT signing key
     */
    private SecretKey getSigningKey() {

        byte[] keyBytes = Decoders.BASE64.decode(secretKey);

        return Keys.hmacShaKeyFor(keyBytes);
    }

    public long getExpirationTime() {
        return jwtExpiration/1000;
    }
}