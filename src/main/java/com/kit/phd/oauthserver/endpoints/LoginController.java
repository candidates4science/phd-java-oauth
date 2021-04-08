package com.kit.phd.oauthserver.endpoints;







import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.servlet.view.RedirectView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kit.phd.oauthserver.model.Roles;
import com.kit.phd.oauthserver.model.Status;
import com.kit.phd.oauthserver.model.UserInfo;
import com.kit.phd.oauthserver.payload.JwtAuthenticationResponse;
import com.kit.phd.oauthserver.repos.UserInfoJpaRepository;
import com.kit.phd.oauthserver.security.JwtTokenProvider;
import com.kit.phd.oauthserver.payload.LoginRequest;
import com.kit.phd.oauthserver.payload.OAuthSignupRequest;

import javax.validation.Valid;
import java.net.URI;
import java.security.Principal;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.Random;
import java.util.Optional;


@RestController
@CrossOrigin
public class LoginController {

	
	@Autowired
	private UserInfoJpaRepository userRepository;
 

	
	@Autowired
    RestTemplate restTemplate;
    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
	
    @Value("${postuser.service.uri}")
   	private String postUserServiceUri;
	

    @Value("${signin.user.service.uri}")
   	private String signInUserServiceUri;
    
    @Value("${oauth.login.failed.redirecturl}")
    private String failedUrl;
    
    @Value("${oauth.login.success.redirecturl}")
    private String successUrl;
	
	@GetMapping("/")
	public RedirectView user(@AuthenticationPrincipal OAuth2User principal) {
		 //System.out.println(principal);
		// System.out.println(principal.getAttribute("url").toString().indexOf("github"));
		//System.out.println(principal);
		//System.out.println(principal.getAttribute("iss").toString());
		//System.out.println(principal.getAttribute("url").toString());
		//System.out.println(principal.getAttribute("name").toString());
		
		// github
		if (principal.getAttribute("url") != null  &&  principal.getAttribute("url").toString().indexOf("github") >= 0 
				&& principal.getAttribute("email") != null ) {
			
		
			
			String username = principal.getAttribute("login").toString();
			String email = username + "@github.auth"; // avoid real email
			String pwd = email + "1298Abz+-";

			Optional<UserInfo> tryUser = userRepository.findByEmail(email);

			if (!tryUser.isPresent()) {
				OAuthSignupRequest signup = new OAuthSignupRequest();
				signup.setAuthService("oauth");
				signup.setEmail(email);
				signup.setOriginalEmail(principal.getAttribute("email"));
				signup.setPassword(pwd);
				signup.setUsername(username);
				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_JSON);
				HttpEntity<OAuthSignupRequest> postUser = new HttpEntity<OAuthSignupRequest>(signup, headers);
				ResponseEntity<String> response = restTemplate.postForEntity(postUserServiceUri, postUser,
						String.class);
				if (response.getStatusCode().is2xxSuccessful() == false) {
					/*
					 * ObjectMapper mapper = new ObjectMapper();
					 * 
					 * ObjectNode info = mapper.createObjectNode();
					 * info.put("error","Github login failed"); return new
					 * ResponseEntity<ObjectNode>(info,HttpStatus.UNAUTHORIZED);
					 */
					RedirectView redirectV = new RedirectView(failedUrl);
					return redirectV;
				}

			}

			LoginRequest loginRequest = new LoginRequest();
			loginRequest.setAuthType("oauth");
			loginRequest.setPassword(pwd);
			loginRequest.setUsernameOrEmail(email);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<LoginRequest> postUserSignIn = new HttpEntity<LoginRequest>(loginRequest, headers);
			// System.out.println("before request");
			ResponseEntity<String> response = restTemplate.postForEntity(signInUserServiceUri, postUserSignIn,
					String.class);
			// System.out.println("after request");
			if (response.getStatusCode().is2xxSuccessful() == false) {
				// ObjectMapper mapper = new ObjectMapper();
				// ObjectNode info = mapper.createObjectNode();
				// info.put("error","Github login failed");
				// return new ResponseEntity<ObjectNode>(info,HttpStatus.UNAUTHORIZED);
				// System.out.println("code error");
				RedirectView redirectV = new RedirectView(failedUrl);
				return redirectV;
			}

			try {

				ObjectMapper mapper = new ObjectMapper();
				JsonNode info;
				info = mapper.readTree(response.getBody());

				// System.out.println("good");
				// System.out.println( info.get("accessToken"));
				String url = successUrl.concat("?").concat("AccessToken").concat("=")
						.concat(info.get("accessToken").asText());
				RedirectView redirectV = new RedirectView(url);
				return redirectV;
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
				// ObjectMapper mapper = new ObjectMapper();
				// ObjectNode info = mapper.createObjectNode();
				// info.put("error","Github login failed");
				// return new ResponseEntity<ObjectNode>(info,HttpStatus.UNAUTHORIZED);
				// System.out.println("exception1");
				RedirectView redirectV = new RedirectView(failedUrl);
				return redirectV;
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				// ObjectMapper mapper = new ObjectMapper();
				// ObjectNode info = mapper.createObjectNode();
				// info.put("error","Github login failed");
				// return new ResponseEntity<ObjectNode>(info,HttpStatus.UNAUTHORIZED);
				// e.printStackTrace();
				// System.out.println("exception2");
				RedirectView redirectV = new RedirectView(failedUrl);
				return redirectV;
			}

		}

		else if (principal.getAttribute("iss") != null && principal.getAttribute("iss").toString().contains("google") ) {
			
				
				String username = principal.getAttribute("email").toString().split("@")[0];
				//System.out.println(username);
				//String email = principal.getAttribute("email").toString();
				String email=username+"@google.auth"; //avoid real email
				String pwd = username + "1298Abz+-";

				Optional<UserInfo> tryUser = userRepository.findByEmail(email);

				if (!tryUser.isPresent()) {
					OAuthSignupRequest signup = new OAuthSignupRequest();
					signup.setAuthService("oauth");
					signup.setEmail(email);
					signup.setOriginalEmail(principal.getAttribute("email"));
					signup.setPassword(pwd);
					signup.setUsername(username);
					HttpHeaders headers = new HttpHeaders();
					headers.setContentType(MediaType.APPLICATION_JSON);
					HttpEntity<OAuthSignupRequest> postUser = new HttpEntity<OAuthSignupRequest>(signup, headers);
					ResponseEntity<String> response = restTemplate.postForEntity(postUserServiceUri, postUser,
							String.class);
					if (response.getStatusCode().is2xxSuccessful() == false) {
						/*
						 * ObjectMapper mapper = new ObjectMapper();
						 * 
						 * ObjectNode info = mapper.createObjectNode();
						 * info.put("error","Github login failed"); return new
						 * ResponseEntity<ObjectNode>(info,HttpStatus.UNAUTHORIZED);
						 */
						RedirectView redirectV = new RedirectView(failedUrl);
						return redirectV;
					}

				}

				LoginRequest loginRequest = new LoginRequest();
				loginRequest.setAuthType("oauth");
				loginRequest.setPassword(pwd);
				loginRequest.setUsernameOrEmail(email);

				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_JSON);
				HttpEntity<LoginRequest> postUserSignIn = new HttpEntity<LoginRequest>(loginRequest, headers);
				// System.out.println("before request");
				ResponseEntity<String> response = restTemplate.postForEntity(signInUserServiceUri, postUserSignIn,
						String.class);
				// System.out.println("after request");
				if (response.getStatusCode().is2xxSuccessful() == false) {
					// ObjectMapper mapper = new ObjectMapper();
					// ObjectNode info = mapper.createObjectNode();
					// info.put("error","Github login failed");
					// return new ResponseEntity<ObjectNode>(info,HttpStatus.UNAUTHORIZED);
					// System.out.println("code error");
					RedirectView redirectV = new RedirectView(failedUrl);
					return redirectV;
				}

				try {

					ObjectMapper mapper = new ObjectMapper();
					JsonNode info;
					info = mapper.readTree(response.getBody());

					// System.out.println("good");
					// System.out.println( info.get("accessToken"));
					String url = successUrl.concat("?").concat("AccessToken").concat("=")
							.concat(info.get("accessToken").asText());
					RedirectView redirectV = new RedirectView(url);
					return redirectV;
				} catch (JsonMappingException e) {
					// TODO Auto-generated catch block
					// e.printStackTrace();
					// ObjectMapper mapper = new ObjectMapper();
					// ObjectNode info = mapper.createObjectNode();
					// info.put("error","Github login failed");
					// return new ResponseEntity<ObjectNode>(info,HttpStatus.UNAUTHORIZED);
					// System.out.println("exception1");
					RedirectView redirectV = new RedirectView(failedUrl);
					return redirectV;
				} catch (JsonProcessingException e) {
					// TODO Auto-generated catch block
					// ObjectMapper mapper = new ObjectMapper();
					// ObjectNode info = mapper.createObjectNode();
					// info.put("error","Github login failed");
					// return new ResponseEntity<ObjectNode>(info,HttpStatus.UNAUTHORIZED);
					// e.printStackTrace();
					// System.out.println("exception2");
					RedirectView redirectV = new RedirectView(failedUrl);
					return redirectV;
				}

			
		}
		
		else if ( principal.getAttribute("picture") != null && principal.getAttribute("picture").toString().contains("fb"))
        {
			// avoid language problem
			String username = principal.getAttribute("email").toString().split("@")[0];
			//String email = principal.getAttribute("email").toString();
			String email=username+"@facebook.auth"; //avoid real email
			String pwd = username + "1298Abz+-";

			Optional<UserInfo> tryUser = userRepository.findByEmail(email);

			if (!tryUser.isPresent()) {
				OAuthSignupRequest signup = new OAuthSignupRequest();
				signup.setAuthService("oauth");
				signup.setEmail(email);
				signup.setOriginalEmail(principal.getAttribute("email"));
				signup.setPassword(pwd);
				signup.setUsername(username);
				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_JSON);
				HttpEntity<OAuthSignupRequest> postUser = new HttpEntity<OAuthSignupRequest>(signup, headers);
				ResponseEntity<String> response = restTemplate.postForEntity(postUserServiceUri, postUser,
						String.class);
				if (response.getStatusCode().is2xxSuccessful() == false) {
					/*
					* ObjectMapper mapper = new ObjectMapper();
					
					ObjectNode info = mapper.createObjectNode();
					info.put("error","Github login failed");
					return new ResponseEntity<ObjectNode>(info,HttpStatus.UNAUTHORIZED);
					*/
					RedirectView redirectV = new RedirectView(failedUrl);
					return redirectV;
				}

			}

			LoginRequest loginRequest = new LoginRequest();
			loginRequest.setAuthType("oauth");
			loginRequest.setPassword(pwd);
			loginRequest.setUsernameOrEmail(email);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<LoginRequest> postUserSignIn = new HttpEntity<LoginRequest>(loginRequest, headers);
			// System.out.println("before request");
			ResponseEntity<String> response = restTemplate.postForEntity(signInUserServiceUri, postUserSignIn,
					String.class);
			// System.out.println("after request");
			if (response.getStatusCode().is2xxSuccessful() == false) {
				// ObjectMapper mapper = new ObjectMapper();
				//ObjectNode info = mapper.createObjectNode();
				//info.put("error","Github login failed");
				//return new ResponseEntity<ObjectNode>(info,HttpStatus.UNAUTHORIZED);
				//System.out.println("code error");
				RedirectView redirectV = new RedirectView(failedUrl);
				return redirectV;
			}

			try {

				ObjectMapper mapper = new ObjectMapper();
				JsonNode info;
				info = mapper.readTree(response.getBody());

				// System.out.println("good");
				// System.out.println( info.get("accessToken"));
				String url = successUrl.concat("?").concat("AccessToken").concat("=")
						.concat(info.get("accessToken").asText());
				RedirectView redirectV = new RedirectView(url);
				return redirectV;
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				//	ObjectMapper mapper = new ObjectMapper();
				//  ObjectNode info = mapper.createObjectNode();
				//  info.put("error","Github login failed");
				//return new ResponseEntity<ObjectNode>(info,HttpStatus.UNAUTHORIZED);
				// System.out.println("exception1");
				RedirectView redirectV = new RedirectView(failedUrl);
				return redirectV;
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				//ObjectMapper mapper = new ObjectMapper();
				//   ObjectNode info = mapper.createObjectNode();
				//  info.put("error","Github login failed");
				// return new ResponseEntity<ObjectNode>(info,HttpStatus.UNAUTHORIZED);
				//e.printStackTrace();
				//System.out.println("exception2");
				RedirectView redirectV = new RedirectView(failedUrl);
				return redirectV;
			}

		}
		
		else if ( principal.getAttribute("iss") != null && principal.getAttribute("iss").toString().contains("oidc.scc.kit.edu"))
        {
			// avoid language problem
			String username = principal.getAttribute("preferred_username");
			//String email = principal.getAttribute("email").toString();
			String email=username+"@kit.auth"; //avoid real email
			String pwd = username + "1298Abz+-";

			Optional<UserInfo> tryUser = userRepository.findByEmail(email);

			if (!tryUser.isPresent()) {
				OAuthSignupRequest signup = new OAuthSignupRequest();
				signup.setAuthService("oauth");
				signup.setEmail(email);
				signup.setOriginalEmail(principal.getAttribute("email"));
				signup.setPassword(pwd);
				signup.setUsername(username);
				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_JSON);
				HttpEntity<OAuthSignupRequest> postUser = new HttpEntity<OAuthSignupRequest>(signup, headers);
				ResponseEntity<String> response = restTemplate.postForEntity(postUserServiceUri, postUser,
						String.class);
				if (response.getStatusCode().is2xxSuccessful() == false) {
					/*
					* ObjectMapper mapper = new ObjectMapper();
					
					ObjectNode info = mapper.createObjectNode();
					info.put("error","Github login failed");
					return new ResponseEntity<ObjectNode>(info,HttpStatus.UNAUTHORIZED);
					*/
					RedirectView redirectV = new RedirectView(failedUrl);
					return redirectV;
				}

			}

			LoginRequest loginRequest = new LoginRequest();
			loginRequest.setAuthType("oauth");
			loginRequest.setPassword(pwd);
			loginRequest.setUsernameOrEmail(email);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<LoginRequest> postUserSignIn = new HttpEntity<LoginRequest>(loginRequest, headers);
			// System.out.println("before request");
			ResponseEntity<String> response = restTemplate.postForEntity(signInUserServiceUri, postUserSignIn,
					String.class);
			// System.out.println("after request");
			if (response.getStatusCode().is2xxSuccessful() == false) {
				// ObjectMapper mapper = new ObjectMapper();
				//ObjectNode info = mapper.createObjectNode();
				//info.put("error","Github login failed");
				//return new ResponseEntity<ObjectNode>(info,HttpStatus.UNAUTHORIZED);
				//System.out.println("code error");
				RedirectView redirectV = new RedirectView(failedUrl);
				return redirectV;
			}

			try {

				ObjectMapper mapper = new ObjectMapper();
				JsonNode info;
				info = mapper.readTree(response.getBody());

				// System.out.println("good");
				// System.out.println( info.get("accessToken"));
				String url = successUrl.concat("?").concat("AccessToken").concat("=")
						.concat(info.get("accessToken").asText());
				RedirectView redirectV = new RedirectView(url);
				return redirectV;
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				//	ObjectMapper mapper = new ObjectMapper();
				//  ObjectNode info = mapper.createObjectNode();
				//  info.put("error","Github login failed");
				//return new ResponseEntity<ObjectNode>(info,HttpStatus.UNAUTHORIZED);
				// System.out.println("exception1");
				RedirectView redirectV = new RedirectView(failedUrl);
				return redirectV;
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				//ObjectMapper mapper = new ObjectMapper();
				//   ObjectNode info = mapper.createObjectNode();
				//  info.put("error","Github login failed");
				// return new ResponseEntity<ObjectNode>(info,HttpStatus.UNAUTHORIZED);
				//e.printStackTrace();
				//System.out.println("exception2");
				RedirectView redirectV = new RedirectView(failedUrl);
				return redirectV;
			}

		}

		//
		//ObjectMapper mapper = new ObjectMapper();
		//ObjectNode info = mapper.createObjectNode();
		//info.put("name", principal.getAttribute("login").toString());
		RedirectView redirectV = new RedirectView(failedUrl);
		return redirectV;

	}

	
	
}  
    

   
