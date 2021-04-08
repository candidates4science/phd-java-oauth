package com.kit.phd.oauthserver.model;



import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;






@Entity
@Table(name = "usersinfo")
public class UserInfo {
        @Id
        @GeneratedValue
        @Column(name = "userid",updatable=false)
        private Long id;
        
        @Column(name = "username",unique=true)
        @NotNull
        @Size(max=100)
        private String username;
        
        @Column(name = "password")
        //@NotEmpty(message="error.password.empty")
        @Size(max=100)
        private String password;
        
     
        
   
        
        @Column(name="email",unique=true,updatable=false)
        @NotNull
        @Size(max=100)
        private String email;
        
        @Column(name="authservice")
        @NotNull
        private String authService;
        
        @Column(name="role")
        @NotNull
        private Roles role;
       
        @Column(name="status")
        private Status status;
        
     
        
        

		public Roles getRole() {
			return role;
		}
		public void setRole(Roles role) {
			this.role = role;
		}
		public Status getStatus() {
			return status;
		}
		public void setStatus(Status status) {
			this.status = status;
		}
		public String getEmail() {
			return email;
		}
		public void setEmail(String email) {
			this.email = email;
		}
		
		public Long getId() {
			return id;
		}
		public void setId(Long id) {
			this.id = id;
		}
		public String getUsername() {
                return username;
        }
        public void setUsername(String username) {
                this.username = username;
        }
        public String getPassword() {
                return password;
        }
        public void setPassword(String password) {
                this.password = password;
        }
    
      
        
        public String getAuthService() {
     			return authService;
     	}
 		public void setAuthService(String authService) {
 			this.authService = authService;
 		}
 		
        public String getErrorMessage() {
            return null;
        }
}