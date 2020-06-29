package com.altimetrik.sso.model;

public class RefreshJwtRequest {

	private static final long serialVersionUID = 5926468583005150708L;
	private String refreshToken;
	
	public RefreshJwtRequest() {
	}
	
	public RefreshJwtRequest(String refreshToken) {
		this.setRefreshToken(refreshToken);

	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}
	
	
}
