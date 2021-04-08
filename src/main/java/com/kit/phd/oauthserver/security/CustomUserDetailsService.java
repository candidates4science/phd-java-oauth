package com.kit.phd.oauthserver.security;





import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kit.phd.oauthserver.model.UserInfo;
import com.kit.phd.oauthserver.repos.UserInfoJpaRepository;




@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    UserInfoJpaRepository userRepository;

    @Override
    //@Transactional
    public UserDetails loadUserByUsername(String usernameOrEmail)
            throws UsernameNotFoundException {
        // Let people login with either username or email
        Optional<UserInfo> user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail);
        if(! user.isPresent() ) {
        	 throw new UsernameNotFoundException(
                     "Opps! user not found with username or email: " + usernameOrEmail);
        } 
       
        
        return UserPrincipal.create(user.get());
    }

    //@Transactional
    public UserDetails loadUserById(Long id) {
        Optional<UserInfo> user = userRepository.findById(id);
        if( user.isPresent() == false) {
        	throw new UsernameNotFoundException(
                    "Opps! user not found with id: " + id);
        }
    
        return UserPrincipal.create(user.get());
    }
}