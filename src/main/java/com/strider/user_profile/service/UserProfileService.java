package com.strider.user_profile.service;

import com.nimbusds.jwt.JWTClaimsSet;
import com.strider.strider_common_lib.error.StriderErrorCodes;
import com.strider.strider_common_lib.exception.StriderException;
import com.strider.user_profile.config.AppleJwtVerifier;
import com.strider.user_profile.config.JwtTokenProvider;
import com.strider.user_profile.config.SignupValidator;
import com.strider.user_profile.infra.KakaoClient;
import com.strider.user_profile.infra.NaverClient;
import com.strider.user_profile.model.entity.*;
import com.strider.user_profile.model.enums.AuthProvider;
import com.strider.user_profile.model.request.*;
import com.strider.user_profile.model.response.*;
import com.strider.user_profile.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
@Slf4j
@RequiredArgsConstructor
public class UserProfileService {
    private final StriderUserRepository striderUserRepository;
    private final UserTokenRepository userTokenRepository;
    private final UserFollowRepository userFollowRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserReportRepository userReportRepository;
    private final UserBlockRepository userBlockRepository;

    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, String> redisTemplate;
    private final SmsService smsService;
    private final FileStorageService fileStorageService;

    private final KakaoClient kakaoClient;
    private final NaverClient naverClient;
    private final AppleJwtVerifier appleJwtVerifier;
    private final com.strider.user_profile.client.FeedClient feedClient;

    private final RestTemplate restTemplate;

    @Value("${services.gateway.base-url}")
    private String gatewayBaseUrl;

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Transactional
    public StriderSignupResponse signup(HttpHeaders headers, StriderSignupRequest request) {
        SignupValidator.validate(request.id(), request.password());

        checkIsAvailableId(AuthProvider.LOCAL, request.id());
        checkIsAvailablePhoneNumber(request.phoneNumber());

        StriderUser striderUser = StriderUser.builder()
                .providerId(request.id())
                .provider(AuthProvider.LOCAL)
                .password(passwordEncoder.encode(request.password()))
                .phoneNumber(request.phoneNumber())
                .isAgreedMarketing(request.marketing())
                .isAgreedMarketingPush(request.marketing_push())
                .isAgreedMarketingEmail(request.marketing_email())
                .isAgreedMarketingSMS(request.marketing_sms())
                .build();

        StriderUser newStriderUser = striderUserRepository.save(striderUser);

        UserProfile userProfile = UserProfile.builder()
                .userId(newStriderUser.getUserId())
                .nickname("User" + newStriderUser.getUserId().substring(0, 6)) // 기본 닉네임 설정
                .build();

        userProfileRepository.save(userProfile);

        return StriderSignupResponse.builder()
                .userId(newStriderUser.getUserId())
                .id(newStriderUser.getProviderId())
                .userStatus(newStriderUser.getUserStatus()).build();
    }

    public void checkIsAvailablePhoneNumber(String phoneNum){
        if (striderUserRepository.existsByPhoneNumber(phoneNum)){
            log.info("This phoneNumber already exists: {}", phoneNum);
            throw new StriderException(StriderErrorCodes.BAD_REQUEST);
        }
    }

    public void checkIsAvailableId(AuthProvider provider, String id){
        if (striderUserRepository.findByProviderAndProviderId(provider, id).isPresent()){
            log.info("This user already exists: {}", id);
            throw new StriderException(StriderErrorCodes.BAD_REQUEST);
        }
    }

    public StriderLoginResponse login(HttpHeaders headers, StriderLoginRequest request) {
        log.info("[Login] Login attempt for id: {}", request.id());
        StriderUser user = striderUserRepository.findByProviderAndProviderId(AuthProvider.LOCAL, request.id())
                .orElseThrow(() -> {
                    log.error("[Login][BAD REQUEST] User not found for id: {}", request.id());
                    return new StriderException(StriderErrorCodes.BAD_REQUEST);
                });
        log.info("[Login] found login for userId: {}", user.getUserId());
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            log.error("[Login][BAD REQUEST] Password mismatch for userId: {}", user.getUserId());
            throw new StriderException(StriderErrorCodes.BAD_REQUEST);
        }

        AuthResponse authResponse = issueTokensFor(user.getUserId());
        log.info("[Login][SUCCESS] User logged in successfully: {}", user.getUserId());
        return StriderLoginResponse.builder()
                .userId(user.getUserId())
                .token(authResponse.accessToken())
                .refreshToken(authResponse.refreshToken())
                .build();
    }

    @Transactional
    public AuthResponse issueTokensFor(String userId){
        String refreshToken = jwtTokenProvider.createRefreshToken(userId);
        String token = jwtTokenProvider.createAccessToken(userId);

        UserToken userToken = userTokenRepository.findByUserId(userId)
                .map(existingToken -> {
                    existingToken.updateRefreshToken(refreshToken);
                    return existingToken;
                })
                .orElse(UserToken.builder()
                        .userId(userId)
                        .refreshToken(refreshToken)
                        .build());

        userTokenRepository.save(userToken);

        return AuthResponse.builder()
                .accessToken(token)
                .refreshToken(refreshToken)
                .build();
    }

    @Transactional
    public SocialSignupResponse kakaoSignUp(KakaoSignUpRequest request){
        KakaoUserResponse myInfo = kakaoClient.getUserInfo("Bearer " + request.accessToken());
        String socialId = myInfo.id().toString();

        checkIsAvailableId(AuthProvider.KAKAO, socialId);
        checkIsAvailablePhoneNumber(request.phoneNumber());

        StriderUser striderUser = StriderUser.builder()
                .providerId(socialId)
                .provider(AuthProvider.KAKAO)
                .phoneNumber(request.phoneNumber())
                .isAgreedMarketing(request.marketing())
                .isAgreedMarketingPush(request.marketing_push())
                .isAgreedMarketingEmail(request.marketing_email())
                .isAgreedMarketingSMS(request.marketing_sms())
                .build();

        StriderUser newStriderUser = striderUserRepository.save(striderUser);
        AuthResponse auth = issueTokensFor(newStriderUser.getUserId());

        return SocialSignupResponse.builder()
                .userId(newStriderUser.getUserId())
                .id(newStriderUser.getProviderId())
                .userStatus(newStriderUser.getUserStatus())
                .refreshToken(auth.refreshToken())
                .accessToken(auth.accessToken())
                .build();
    }

    @Transactional
    public SocialSignupResponse naverSignUp(NaverSignUpRequest request){
        NaverUserResponse myInfo = naverClient.getUserInfo("Bearer " + request.accessToken());
        String socialId = myInfo.naverUserInfo().id();

        checkIsAvailableId(AuthProvider.NAVER, socialId);
        checkIsAvailablePhoneNumber(request.phoneNumber());

        StriderUser striderUser = StriderUser.builder()
                .providerId(socialId)
                .provider(AuthProvider.NAVER)
                .phoneNumber(request.phoneNumber())
                .isAgreedMarketing(request.marketing())
                .isAgreedMarketingPush(request.marketing_push())
                .isAgreedMarketingEmail(request.marketing_email())
                .isAgreedMarketingSMS(request.marketing_sms())
                .build();

        StriderUser newStriderUser = striderUserRepository.save(striderUser);
        AuthResponse auth = issueTokensFor(newStriderUser.getUserId());

        return SocialSignupResponse.builder()
                .userId(newStriderUser.getUserId())
                .id(newStriderUser.getProviderId())
                .userStatus(newStriderUser.getUserStatus())
                .refreshToken(auth.refreshToken())
                .accessToken(auth.accessToken())
                .build();
    }

    @Transactional
    public SocialSignupResponse appleSignUp(AppleSignUpRequest request){
        JWTClaimsSet claims = appleJwtVerifier.verify(request.identityToken());
        String socialId = claims.getSubject();

        checkIsAvailableId(AuthProvider.APPLE, socialId);
        checkIsAvailablePhoneNumber(request.phoneNumber());

        StriderUser striderUser = StriderUser.builder()
                .providerId(socialId)
                .provider(AuthProvider.APPLE)
                .phoneNumber(request.phoneNumber())
                .isAgreedMarketing(request.marketing())
                .isAgreedMarketingPush(request.marketing_push())
                .isAgreedMarketingEmail(request.marketing_email())
                .isAgreedMarketingSMS(request.marketing_sms())
                .build();

        StriderUser newStriderUser = striderUserRepository.save(striderUser);
        AuthResponse auth = issueTokensFor(newStriderUser.getUserId());

        return SocialSignupResponse.builder()
                .userId(newStriderUser.getUserId())
                .id(newStriderUser.getProviderId())
                .userStatus(newStriderUser.getUserStatus())
                .refreshToken(auth.refreshToken())
                .accessToken(auth.accessToken())
                .build();
    }


    public AuthResponse kakaoLogIn(KakaoLogInRequest request) {
        KakaoUserResponse myInfo = kakaoClient.getUserInfo("Bearer " + request.accessToken());
        String socialId = myInfo.id().toString();

        return striderUserRepository
                .findByProviderAndProviderId(AuthProvider.KAKAO, socialId)
                .map(user -> issueTokensFor(user.getUserId()))
                .orElseThrow(() -> new StriderException(StriderErrorCodes.NOT_FOUND));
    }

    public AuthResponse naverLogin(NaverLogInRequest request){
        NaverUserResponse myInfo = naverClient.getUserInfo("Bearer " + request.accessToken());
        String socialId = myInfo.naverUserInfo().id();

        return striderUserRepository
                .findByProviderAndProviderId(AuthProvider.NAVER, socialId)
                .map(user -> issueTokensFor(user.getUserId()))
                .orElseThrow(() -> new StriderException(StriderErrorCodes.NOT_FOUND));
    }

    public AuthResponse appleLogin(AppleLogInRequest request){
        JWTClaimsSet claims = appleJwtVerifier.verify(request.identityToken());
        String socialId = claims.getSubject();
//        String email = claims.getClaim("email").toString();

        return striderUserRepository
                .findByProviderAndProviderId(AuthProvider.APPLE, socialId)
                .map(user -> issueTokensFor(user.getUserId()))
                .orElseThrow(() -> new StriderException(StriderErrorCodes.NOT_FOUND));
    }

    public void checkAuthorization(HttpHeaders headers){
        if (headers.containsKey(HttpHeaders.AUTHORIZATION)){
            String token = headers.getFirst(HttpHeaders.AUTHORIZATION);
            if (jwtTokenProvider.validateAccessToken(token)) return;
        }
        throw new StriderException(StriderErrorCodes.UNAUTHORIZED);
    }

//    public void checkAuthorization(String auth){
//        if(auth == null || auth.isBlank()){
//            throw new StriderException(StriderErrorCodes.UNAUTHORIZED);
//        }
//
//        if (auth.startsWith("Bearer ")) {
//            auth = auth.substring(7);
//        }
//
//        if(!jwtTokenProvider.validateAccessToken(auth)){
//            throw new StriderException(StriderErrorCodes.UNAUTHORIZED);
//        }
//    }

    public RefreshTokenResponseDto refreshAccessToken(HttpHeaders headers, RefreshTokenRequest request){
        String refreshToken = request.refreshToken();

        if (jwtTokenProvider.validateRefreshToken(refreshToken) == false){
            throw new StriderException(StriderErrorCodes.UNAUTHORIZED);
        }

        String userId = jwtTokenProvider.getUserIdFromRefreshToken(refreshToken);

        UserToken savedUserToken = userTokenRepository.findByUserId(userId)
                        .orElseThrow(() -> new StriderException(StriderErrorCodes.UNAUTHORIZED));

        if (savedUserToken.equals(refreshToken) == false) {
            throw new StriderException(StriderErrorCodes.UNAUTHORIZED);
        }

        String newAccessToken = jwtTokenProvider.createAccessToken(userId);

        return RefreshTokenResponseDto.builder().accessToken(newAccessToken).build();
    }

    @Transactional
    public void logout(StriderLogoutRequest request) {
        var userToken = userTokenRepository.findByUserId(request.userId())
                .orElseThrow(() -> new StriderException(StriderErrorCodes.NOT_FOUND));

        if (!userToken.getRefreshToken().equals(request.refreshToken())) {
            throw new StriderException(StriderErrorCodes.BAD_REQUEST);
        }

        userTokenRepository.deleteByUserIdAndRefreshToken(request.userId(), request.refreshToken());
    }

    public void sendCode(HttpHeaders headers, SendCodeRequest request) {
        String generatedCode = generate6DigitCode();
        log.info("생성한 인증번호: {}, 전화번호: {}", generatedCode, request.phoneNumber());
        try {
            redisTemplate.opsForValue().set("PHONE_CODE:" + request.phoneNumber(), generatedCode, Duration.ofMinutes(30));
        } catch (Exception exception){
            log.error("[SendCode][INTERNAL SERVER ERROR] Error occurred saving code to Redis");
            log.error("[SendCode] " + exception.getMessage());
            throw new StriderException(StriderErrorCodes.INTERNAL_SERVER_ERROR);
        }
        try {
            String message = "[Strider] 인증번호는 " + generatedCode + "입니다.";
            smsService.sendSms(request.phoneNumber(), message);
            log.info("[SendCode] 전송된 인증번호: {}", generatedCode);
        } catch (Exception exception){
            log.error("[SendCode][INTERNAL SERVER ERROR] Error occurred sending Solapi");
            throw new StriderException(StriderErrorCodes.INTERNAL_SERVER_ERROR);
        }
    }

    private String generate6DigitCode() {
        return String.format("%06d", new Random().nextInt(999999));
    }

    public void verifyCode(HttpHeaders headers, VerifyCodeRequest request) {
        verifyCodeWithPhoneNumber(request.phoneNumber(), request.inputCode());
    }

    public FindMyIdResponseDto findMyId(HttpHeaders headers, VerifyCodeRequest request) {
        verifyCodeWithPhoneNumber(request.phoneNumber(), request.inputCode());

        StriderUser user = striderUserRepository.findByPhoneNumber(request.phoneNumber())
                .orElseThrow(() -> new StriderException(StriderErrorCodes.NOT_FOUND));

        return FindMyIdResponseDto.builder()
                .provider(user.getProvider().name())
                .maskedProviderId(maskingId(user.getProviderId()))
                .build();
    }

    private void verifyCodeWithPhoneNumber(String phoneNumber, String inputCode){
        String key = "PHONE_CODE:" + phoneNumber;
        String savedCode = redisTemplate.opsForValue().get(key);
        if (savedCode != null && savedCode.equals(inputCode)) {
            redisTemplate.delete(key); // 1회용
            log.info("[VerifyingCode][SUCCESS] Successfully verified phone number: {}", phoneNumber);
            return;
        }
        log.error("[VerifyingCode][BAD REQUEST] savedCode: {}, inputCode: {}", savedCode, inputCode);
        throw new StriderException(StriderErrorCodes.BAD_REQUEST);
    }

    private String maskingId(String id){
        String firstTwo = id.substring(0,2);
        String lastTwo = id.substring(id.length() - 2);
        String maskedMiddle = "*".repeat(id.length() - 4);
        return firstTwo + maskedMiddle + lastTwo;
    }

    @Transactional
    public void resetPassword(HttpHeaders headers, ResetPasswordRequest request) {
        StriderUser user = striderUserRepository.findByPhoneNumber(request.phoneNumber())
                .orElseThrow(() -> new StriderException(StriderErrorCodes.NOT_FOUND));

        user.setPassword(passwordEncoder.encode(request.password()));

        striderUserRepository.save(user);
    }

    public UserProfileResponse findUserProfile(FindUserProfileRequest request, String currentUserId) {
        UserProfile user = userProfileRepository.findByUserId(request.userId())
                .orElseThrow(() -> new StriderException(StriderErrorCodes.NOT_FOUND));

        StriderUser striderUser = striderUserRepository.findByUserId(request.userId())
                .orElseThrow(() -> new StriderException(StriderErrorCodes.NOT_FOUND));

        UserBlock userBlock = userBlockRepository.findByBlockerIdAndTargetTypeAndTargetIdAndStatus(currentUserId,"USER",request.userId(),"ACTIVE");
        boolean isBlock = (userBlock != null);

        int postCount = 0;
        try {
            var feedCountEnvelope = feedClient.getUserFeedCount(request.userId());
            if (feedCountEnvelope != null && feedCountEnvelope.data() != null) {
                postCount = feedCountEnvelope.data().intValue();
            }
        } catch (Exception e) {
            log.warn("피드 게시물 수 조회 실패 (postCount=0 처리): {}", e.getMessage());
        }

        int followerCount = userFollowRepository.countFollowerExcludeCurrent(request.userId(), currentUserId);
        int followingCount = userFollowRepository.countFollowingExcludeCurrent(request.userId(),currentUserId);

        String profileImage = null;
        if(user.getProfileImage() != null) {
            profileImage = user.getProfileImage();
        }

        String profileThumbImage = null;
        if(user.getProfileThumbImage() != null) {
            profileThumbImage = user.getProfileThumbImage();
        }

        List<String> targetUserIds = new ArrayList<>();
        targetUserIds.add(request.userId());

        Map<String, FollowRelationResponse> relationMap =
                userFollowRepository.findFollowRelations(currentUserId, targetUserIds)
                        .stream()
                        .collect(Collectors.toMap(
                                FollowRelationResponse::userId,
                                Function.identity()
                        ));

        FollowRelationResponse relation = relationMap.get(request.userId());

        return UserProfileResponse.builder()
                .userId(user.getUserId())
                .profileId(user.getProfileId())
                .nickname(user.getNickname())
                .profileImage(profileImage)
                .profileThumbImage(profileThumbImage)
                .profileDesc(user.getProfileDesc())
                .rankId(user.getRankId())
                .postCount(postCount)
                .followingCount(followingCount)
                .followerCount(followerCount)
                .followStatus(relation != null ? relation.followStatus() : "NOT_FOLLOWING")  // 상대방이 나를 팔로우
                .isFollower(relation != null && relation.isFollower())  // 내가 상대방 팔로우
                .isPublic(striderUser.getIsPublic())
                .isBlock(isBlock)
                .build();
    }

    public List<SimpleUserProfileResponse> findUserProfileInFeedList(FindUserProfileListRequest request){
        List<UserProfile> userProfileList = userProfileRepository.findByUserIdIn(request.userIds());

        return userProfileList.stream()
                .map(userProfile ->
                {
                    return SimpleUserProfileResponse.builder()
                            .userId(userProfile.getUserId())
                            .profileId(userProfile.getProfileId())
                            .nickname(userProfile.getNickname())
                            .profileImage(userProfile.getProfileImage())
                            .profileThumbImage(userProfile.getProfileThumbImage())
                            .rankId(userProfile.getRankId())
                            .build();
                }).toList();
    }

    @Transactional
    public UserProfileResponse updateUserProfile(UserProfileUpdateRequest request) {
        // 프로필 존재 여부 확인 (userId)
        UserProfile existingProfile = userProfileRepository.findByUserId(request.userId())
                .orElseThrow(() -> new StriderException(StriderErrorCodes.NOT_FOUND));

        // nickname 중복 검사 (자기 자신 제외)
        userProfileRepository.findByNicknameAndUserIdNot(request.nickname(), request.userId())
                .ifPresent(profile -> {
                    throw new StriderException(StriderErrorCodes.BAD_REQUEST);
                });

        // 업데이트 수행
        existingProfile.setNickname(request.nickname());
        existingProfile.setProfileDesc(request.profileDesc());

        userProfileRepository.save(existingProfile);

        return UserProfileResponse.builder()
                .userId(existingProfile.getUserId())
                .nickname(existingProfile.getNickname())
                .profileDesc(existingProfile.getProfileDesc())
                .build();
    }

    @Transactional
    public void saveProfileImage(UserProfileImageRequest request) {
        UserProfile profile = userProfileRepository.findByUserId(request.userId())
                .orElseThrow(() -> new StriderException(StriderErrorCodes.NOT_FOUND));

        // 1. 기존 이미지 URL 확인
        String oldProfileImageS3Key = profile.getProfileImageS3Key();
        String oldProfileThumbS3Key = profile.getProfileThumbS3Key();

        // 2. DB에 새 이미지 URL 저장
        profile.setProfileImage(request.profileImage());
        profile.setProfileThumbImage(request.profileThumbImage());
        profile.setProfileImageS3Key(request.profileImageS3Key());
        profile.setProfileThumbS3Key(request.profileThumbS3Key());
        profile.setUpdatedAt(LocalDateTime.now());
        userProfileRepository.save(profile);

        // 3. Media 삭제 요청 (추후 이미지 신고 이력을 위해 S3 이미지 유지)
//        try {
//            if (oldProfileImageS3Key != null && !oldProfileImageS3Key.isBlank()) {
//                deleteMediaViaGateway(oldProfileImageS3Key);
//            }
//
//            if (oldProfileThumbS3Key != null && !oldProfileThumbS3Key.isBlank()) {
//                deleteMediaViaGateway(oldProfileThumbS3Key);
//            }
//        } catch (Exception e) {
//            log.error("Failed to request old media deletion via gateway", e);
//        }
    }

    private void deleteMediaViaGateway(String s3Url) {
        try {
            String encodedKey = Base64.getUrlEncoder().encodeToString(s3Url.getBytes(StandardCharsets.UTF_8));
            URI uri = URI.create(gatewayBaseUrl + "/api/v1/strider/media/delete?key=" + encodedKey);
            restTemplate.postForEntity(uri, null, Void.class);

            log.info("Requested deletion of S3 object via gateway: {}", s3Url);
        } catch (Exception e) {
            log.error("Media deletion via gateway failed for URL: {}", s3Url, e);
        }
    }

    public void deleteProfileImage(UserProfileImageRequest request) {
        UserProfile profile = userProfileRepository.findByUserId(request.userId())
                .orElseThrow(() -> new StriderException(StriderErrorCodes.NOT_FOUND));

        // 이미지 경로가 현재 프로필과 일치하는지 확인
        String dbFileName = Paths.get(profile.getProfileImage()).getFileName().toString();
        String requestFileName = Paths.get(request.profileImage()).getFileName().toString();

        if (!dbFileName.equals(requestFileName)) {
            throw new StriderException(StriderErrorCodes.BAD_REQUEST);
        }

        // 파일 삭제 (로컬 기준)
        Path imagePath = Paths.get(uploadDir).resolve(Paths.get(request.profileImage()).getFileName().toString());

        try {
            Files.deleteIfExists(imagePath);
        } catch (IOException e) {
            throw new StriderException(StriderErrorCodes.INTERNAL_SERVER_ERROR);
        }

        // DB 업데이트: 프로필 이미지 비우기
        profile.setProfileImage(null);
        profile.setProfileThumbImage(null);
        profile.setProfileImageS3Key(null);
        profile.setProfileThumbS3Key(null);

        userProfileRepository.save(profile);
    }

    @Transactional
    public void deleteUser(StriderUserDeleteRequest request) {
        // 사용자 정보 조회
        UserProfile profile = userProfileRepository.findByUserId(request.userId())
                .orElseThrow(() -> new StriderException(StriderErrorCodes.NOT_FOUND));

        // RefreshToken 검증
        userTokenRepository.findByUserId(request.userId())
                .filter(token -> token.getRefreshToken().equals(request.refreshToken()))
                .orElseThrow(() -> new StriderException(StriderErrorCodes.BAD_REQUEST));

        // 토큰 삭제
        userTokenRepository.deleteByUserIdAndRefreshToken(request.userId(), request.refreshToken());

        // 사용자 삭제
        userProfileRepository.delete(profile);
    }

    @Transactional
    public UserReport createReport(UserReport userReport, String userId) {
        userReport.setUserId(userId);
        userReport.setTargetType("USER");

        if (userReport.getStatus() == null || userReport.getStatus().isEmpty()) {
            userReport.setStatus("PENDING"); // 기본 상태
        }
        return userReportRepository.save(userReport);
    }

    @Transactional
    public void updateUserPassword(HttpHeaders headers, UpdatePasswordRequest request) {
        StriderUser user = striderUserRepository.findById(request.userId())
                .orElseThrow(() -> new StriderException(StriderErrorCodes.NOT_FOUND));

        // 현재 비밀번호 검증
        if (request.currentPassword() != null && user.getPassword() != null) {
            if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
                throw new StriderException(StriderErrorCodes.BAD_REQUEST);
            }
        }

        // 새 비밀번호 암호화 후 저장
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        striderUserRepository.save(user);
    }

    public Boolean isUserPublic(String userId) {
        StriderUser user = striderUserRepository.findByUserId(userId)
                .orElseThrow(() -> new StriderException(StriderErrorCodes.NOT_FOUND));

        return user.getIsPublic();
    }

    @Transactional
    public void updateUserVisibility(String currentUserId, String userId, Boolean isPublic) {
        if (!currentUserId.equals(userId)) {
            throw new StriderException(StriderErrorCodes.FORBIDDEN);
        }

        StriderUser user = striderUserRepository.findByUserId(userId)
                .orElseThrow(() -> new StriderException(StriderErrorCodes.NOT_FOUND));

        user.setIsPublic(isPublic);
        striderUserRepository.save(user);
    }

    public Boolean isExistUser(String userId) {
        return striderUserRepository.existsById(userId);
    }
}
