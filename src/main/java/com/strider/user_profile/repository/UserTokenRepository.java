package com.strider.user_profile.repository;

import com.strider.user_profile.model.entity.UserToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserTokenRepository extends JpaRepository<UserToken, String> {
    Optional<UserToken> findByUserId(String userId);
    void deleteByUserIdAndRefreshToken(String userId, String refreshToken);
}
