package com.altimetrik.sso.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JwtTokenUtil implements Serializable {

	private static final long serialVersionUID = -2550185165626007488L;

	@Value("${jwt.access.token.validity.milliseconds}")
	private long JWT_TOKEN_VALIDITY;
	
	@Value("${jwt.refresh.token.validity.milliseconds}")
	private long JWT_REFRESH_TOKEN_VALIDITY;

	@Value("${jwt.secret}")
	private String secret;

	// retrieve username from jwt token
	public String getUsernameFromToken(String token) {
		return getClaimFromToken(token, Claims::getSubject);
	}

	// retrieve expiration date from jwt token
	public Date getExpirationDateFromToken(String token) {
		return getClaimFromToken(token, Claims::getExpiration);
	}

	public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = getAllClaimsFromToken(token);
		return claimsResolver.apply(claims);
	}

	// for retrieveing any information from token we will need the secret key
	private Claims getAllClaimsFromToken(String token) {
		return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
	}

	// check if the token has expired
	protected Boolean isTokenExpired(String token) {
		final Date expiration = getExpirationDateFromToken(token);
		return expiration.before(new Date());
	}

	// generate token for user
	public List<String> generateToken(UserDetails userDetails) {
		Map<String, Object> claims = new HashMap<>();
		return doGenerateToken(claims, userDetails.getUsername());
	}
	
	// while creating the token -
	// 1. Define claims of the token, like Issuer, Expiration, Subject, and the ID
	// 2. Sign the JWT using the HS512 algorithm and secret key.
	// 3. According to JWS Compact
	// Serialization(https://tools.ietf.org/html/draft-ietf-jose-json-web-signature-41#section-3.1)
	// compaction of the JWT to a URL-safe string
	private List<String> doGenerateToken(Map<String, Object> claims, String subject) {
		/*
		 * return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new
		 * Date(System.currentTimeMillis())) .setExpiration(new
		 * Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY))
		 * .signWith(SignatureAlgorithm.HS512, secret).compact();
		 */
		
		List<String> tokenList = new ArrayList<String>();
		
		String accessToken = Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY))
				.signWith(SignatureAlgorithm.HS512, secret).compact();
		
		String refreshToken = Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + JWT_REFRESH_TOKEN_VALIDITY))
				.signWith(SignatureAlgorithm.HS512, secret).compact();
		
		tokenList.add(accessToken);
		tokenList.add(refreshToken);
		
		return tokenList;
		
		
		/*
		 * return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new
		 * Date(System.currentTimeMillis())) .setExpiration(new
		 * Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY))
		 * .signWith(SignatureAlgorithm.HS512, secret).compact();
		 */
	}

	// validate token
	public Boolean validateToken(String token, UserDetails userDetails) {
		final String username = getUsernameFromToken(token);
		return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));

	}

	public boolean isRefreshTokenExpired(String refreshToken) throws ExpiredJwtException{
		return isTokenExpired(refreshToken);
	}
	
	public Date getIssuedAtDateFromToken(String token) {
		  //return getClaimFromToken(token).getIssuedAt();
		  return getClaimFromToken(token, Claims::getIssuedAt);
		}
}
