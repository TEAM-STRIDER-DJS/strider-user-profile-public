package com.strider.user_profile.repository;

import com.strider.user_profile.model.entity.UserBlock;
import com.strider.user_profile.model.entity.UserReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserBlockRepository extends JpaRepository<UserBlock, String> {
    UserBlock findByBlockerIdAndTargetTypeAndTargetId(String blockerId, String targetType, String targetId);
    UserBlock findByBlockerIdAndTargetTypeAndTargetIdAndStatus(String blockerId, String targetType, String targetId, String status);
    boolean existsByBlockerIdAndTargetTypeAndTargetIdAndStatus(String blockerId, String targetType, String targetId, String status);
}