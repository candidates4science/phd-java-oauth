package com.kit.phd.oauthserver.payload;



import javax.validation.constraints.NotNull;

public class LoginRequest {
    @NotNull
    private String usernameOrEmail;

    @NotNull
    private String password;
    
    @NotNull
    private String authType;
    

    public String getUsernameOrEmail() {
        return usernameOrEmail;
    }

    public void setUsernameOrEmail(String usernameOrEmail) {
        this.usernameOrEmail = usernameOrEmail;
    } 

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

	public String getAuthType() {
		return authType;
	}

	public void setAuthType(String authType) {
		this.authType = authType;
	}
    
    
    
    
    
}
