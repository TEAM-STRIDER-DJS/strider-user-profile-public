package com.strider.user_profile.repository;

import com.strider.user_profile.model.entity.UserProfile;
import com.strider.user_profile.model.entity.UserReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserReportRepository extends JpaRepository<UserReport, String> {
}