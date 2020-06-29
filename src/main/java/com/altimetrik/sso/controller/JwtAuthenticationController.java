package com.altimetrik.sso.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.altimetrik.sso.config.JwtTokenUtil;
import com.altimetrik.sso.model.JwtRequest;
import com.altimetrik.sso.model.JwtResponse;
import com.altimetrik.sso.model.RefreshTokenErrorResponse;
import com.altimetrik.sso.service.JwtUserDetailsService;

import io.jsonwebtoken.ExpiredJwtException;

@RestController
@CrossOrigin
public class JwtAuthenticationController {

	@Autowired
	private AuthenticationManager authenticationManager;
	@Autowired
	private JwtTokenUtil jwtTokenUtil;
	@Autowired
	private JwtUserDetailsService userDetailsService;

	@Value("${jwt.refresh.token.validity.milliseconds}")
	private long JWT_REFRESH_TOKEN_VALIDITY;

	@PostMapping(value = "/authenticate")
	public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtRequest authenticationRequest) throws Exception {
		authenticate(authenticationRequest.getUsername(), authenticationRequest.getPassword());
		final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());
		final List<String> tokenList = jwtTokenUtil.generateToken(userDetails);

		return ResponseEntity.ok(new JwtResponse(tokenList.get(0), tokenList.get(1)));
	}

	@PostMapping(value = "/refreshToken")
	public ResponseEntity<?> refreshToken(@RequestHeader("refreshToken") String refreshToken) throws Exception {

		try {

			String username = jwtTokenUtil.getUsernameFromToken(refreshToken);
			final UserDetails userDetails = userDetailsService.loadUserByUsername(username);

			final List<String> tokenList = jwtTokenUtil.generateToken(userDetails);
			return ResponseEntity.ok(new JwtResponse(tokenList.get(0), tokenList.get(1)));

		} catch (ExpiredJwtException e) {
			return ResponseEntity.status(401).body(new RefreshTokenErrorResponse("Refresh Token has Expired!!"));
		} catch (Exception e) {
			return ResponseEntity.status(401).body(new RefreshTokenErrorResponse("Unauthorized Access!!"));
		}

	}

	private void authenticate(String username, String password) throws Exception {
		try {
			authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
		} catch (DisabledException e) {
			throw new Exception("USER_DISABLED", e);
		} catch (BadCredentialsException e) {
			throw new Exception("INVALID_CREDENTIALS", e);
		}
	}

}
