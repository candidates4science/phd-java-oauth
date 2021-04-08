package com.kit.phd.oauthserver.payload;



import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;




public class OAuthSignupRequest {

	@NotNull
	@Size(min = 3, max = 15)
	private String username;

	@NotNull
	@Size(max = 100)
	private String email;

	@NotNull
	@Size(max = 100)
	private String password;

	@NotNull
	private String authService;

	@NotNull
	@Size(max = 100)
	private String originalEmail;

	public String getOriginalEmail() {
		return originalEmail;
	}

	public void setOriginalEmail(String originalEmail) {
		this.originalEmail = originalEmail;
	}

	public String getAuthService() {
		return authService;
	}

	public void setAuthService(String authService) {
		this.authService = authService;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
