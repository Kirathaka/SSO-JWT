package com.altimetrik.sso.model;

import java.io.Serializable;

public class JwtResponse implements Serializable {

	private static final long serialVersionUID = -8091879091924046844L;
	private final String accessToken;
	private final String refreshToken;

	public JwtResponse(String jwttoken,String refreshToken) {
		this.accessToken = jwttoken;
		this.refreshToken = refreshToken;
	}

	public String getToken() {
		return this.accessToken;
	}

	public String getRefreshToken() {
		return refreshToken;
	}
	
	
}
