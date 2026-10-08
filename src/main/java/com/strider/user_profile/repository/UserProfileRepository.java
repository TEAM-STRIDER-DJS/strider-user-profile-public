package com.strider.user_profile.repository;

import com.strider.user_profile.model.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface UserProfileRepository extends JpaRepository<UserProfile, String> {
    Optional<UserProfile> findByUserId(String userId);
    Optional<UserProfile> findByNicknameAndUserIdNot(String nickname, String userId);
    List<UserProfile> findByUserIdIn(List<String> userIdList);
}