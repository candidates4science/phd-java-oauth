package com.kit.phd.oauthserver.repos;







import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kit.phd.oauthserver.model.UserInfo;




@Repository
public interface UserInfoJpaRepository extends JpaRepository<UserInfo, Long> {
        public Optional<UserInfo> findByUsername(String username);
        public Optional<UserInfo> findByEmail(String email);
        public Optional<UserInfo> findByUsernameOrEmail(String username, String email);
        public Optional<UserInfo> findById(long id);
        public void deleteById(long id);
}