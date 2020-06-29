package com.altimetrik.sso.model;

import java.io.Serializable;

public class RefreshTokenErrorResponse implements Serializable  {

	private static final long serialVersionUID = -8091879091924046845L;
	private final String message;
	
	public RefreshTokenErrorResponse(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}
	
	
}
