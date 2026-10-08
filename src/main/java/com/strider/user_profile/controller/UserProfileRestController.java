package com.strider.user_profile.controller;

import com.strider.strider_common_lib.response.StriderResponse;
import com.strider.strider_common_lib.utils.TokenUtils;
import com.strider.user_profile.model.enums.AuthProvider;
import com.strider.user_profile.model.entity.UserReport;
import com.strider.user_profile.model.request.*;
import com.strider.user_profile.model.response.*;
import com.strider.user_profile.service.UserBlockService;
import com.strider.user_profile.service.UserFollowService;
import com.strider.user_profile.service.UserProfileService;
import com.strider.user_profile.service.UserSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/user/profile")
public class UserProfileRestController {
    private final UserProfileService userProfileService;
    private final UserBlockService userBlockService;
    private final UserFollowService userFollowService;
    private final UserSettingService userSettingService;
    private final TokenUtils tokenUtils;

    @PostMapping(value = "/phone/send-code")
    public ResponseEntity<?> sendCode(@RequestHeader HttpHeaders headers, @RequestBody SendCodeRequest request){
        userProfileService.sendCode(headers, request);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(StriderResponse.responseBuilder(null, Void.class));
    }

    @PostMapping(value = "/phone/verify-code")
    public ResponseEntity<?> verifyCode(@RequestHeader HttpHeaders headers, @RequestBody VerifyCodeRequest request){
        userProfileService.verifyCode(headers, request);
        return ResponseEntity.status(HttpStatus.OK)
                .body(StriderResponse.responseBuilder(null, Void.class));
    }

    @PostMapping(value = "/find/id")
    public ResponseEntity<?> findMyId(@RequestHeader HttpHeaders headers, @RequestBody VerifyCodeRequest request){
        FindMyIdResponseDto response = userProfileService.findMyId(headers, request);
        return ResponseEntity.status(HttpStatus.OK)
                .body(StriderResponse.responseBuilder(response, FindMyIdResponseDto.class));
    }

    @PostMapping(value = "/password/reset")
    public ResponseEntity<?> resetPassword(@RequestHeader HttpHeaders headers, @RequestBody ResetPasswordRequest request){
        userProfileService.resetPassword(headers, request);
        return ResponseEntity.status(HttpStatus.OK)
                .body(StriderResponse.responseBuilder(null, Void.class));
    }

    @PostMapping(value = "/signup")
    public ResponseEntity<StriderResponse<StriderSignupResponse>> signup(@RequestHeader HttpHeaders headers, @RequestBody StriderSignupRequest request){
        StriderSignupResponse response = userProfileService.signup(headers, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(StriderResponse.responseBuilder(response, StriderSignupResponse.class));
    }

    @PostMapping(value = "/signup/kakao")
    public ResponseEntity<StriderResponse<SocialSignupResponse>> kakaoSignup(@RequestBody KakaoSignUpRequest request){
        SocialSignupResponse response = userProfileService.kakaoSignUp(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(StriderResponse.responseBuilder(response, SocialSignupResponse.class));
    }

    @PostMapping(value = "/signup/naver")
    public ResponseEntity<StriderResponse<SocialSignupResponse>> naverSignup(@RequestBody NaverSignUpRequest request){
        SocialSignupResponse response = userProfileService.naverSignUp(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(StriderResponse.responseBuilder(response, SocialSignupResponse.class));
    }

    @PostMapping(value = "/signup/apple")
    public ResponseEntity<StriderResponse<SocialSignupResponse>> appleSignup(@RequestBody AppleSignUpRequest request){
        SocialSignupResponse response = userProfileService.appleSignUp(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(StriderResponse.responseBuilder(response, SocialSignupResponse.class));
    }

    @PostMapping(value = "/login")
    public ResponseEntity<StriderResponse<StriderLoginResponse>> login(@RequestHeader HttpHeaders headers, @RequestBody StriderLoginRequest request){
        StriderLoginResponse response = userProfileService.login(headers, request);
        return ResponseEntity.status(HttpStatus.OK)
                .body(StriderResponse.responseBuilder(response, StriderLoginResponse.class));
    }

    @PostMapping(value = "/login/kakao")
    public ResponseEntity<StriderResponse<AuthResponse>> kakaoLogin(@RequestBody KakaoLogInRequest request){
        AuthResponse response = userProfileService.kakaoLogIn(request);
        return ResponseEntity.status(HttpStatus.OK)
                .body(StriderResponse.responseBuilder(response, AuthResponse.class));
    }

    @PostMapping(value = "/login/naver")
    public ResponseEntity<StriderResponse<AuthResponse>> naverLogin(@RequestBody NaverLogInRequest request){
        AuthResponse response = userProfileService.naverLogin(request);
        return ResponseEntity.status(HttpStatus.OK)
                .body(StriderResponse.responseBuilder(response, AuthResponse.class));
    }

    @PostMapping(value = "/login/apple")
    public ResponseEntity<StriderResponse<AuthResponse>> appleLogin(@RequestBody AppleLogInRequest request) {
        AuthResponse response = userProfileService.appleLogin(request);
        return ResponseEntity.status(HttpStatus.OK)
                .body(StriderResponse.responseBuilder(response, AuthResponse.class));
    }

    @PostMapping("/logout")
    public ResponseEntity<StriderResponse<Void>> logout(@RequestBody StriderLogoutRequest request) {
        userProfileService.logout(request);
        return ResponseEntity.status(HttpStatus.OK)
                .body(StriderResponse.responseBuilder(null, Void.class));
    }

    @GetMapping(value = "/strider-user/{id}")
    public ResponseEntity<StriderResponse<Void>> checkIsAvailableId(@RequestHeader HttpHeaders headers, @PathVariable String id){
        userProfileService.checkIsAvailableId(AuthProvider.LOCAL, id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(StriderResponse.responseBuilder(null, Void.class));
    }

    @GetMapping(value = "/strider-user/phone/{phoneNum}")
    public ResponseEntity<StriderResponse<Void>> checkIsAvailablePhoneNumber(@RequestHeader HttpHeaders headers, @PathVariable String phoneNum){
        userProfileService.checkIsAvailablePhoneNumber(phoneNum);
        return ResponseEntity.status(HttpStatus.OK)
                .body(StriderResponse.responseBuilder(null, Void.class));
    }

    @GetMapping(value = "/auth/token")
    public ResponseEntity<StriderResponse<?>> checkAuthorization(
            @RequestHeader HttpHeaders headers, @RequestHeader("Authorization") String auth){
        userProfileService.checkAuthorization(headers);
        return ResponseEntity.status(HttpStatus.OK)
                .body(StriderResponse.responseBuilder(null, Void.class));
    }

    @PostMapping(value = "/auth/token")
    public ResponseEntity<StriderResponse<?>> refreshAccessToken(
            @RequestHeader HttpHeaders headers, @RequestBody RefreshTokenRequest request){
        RefreshTokenResponseDto response = userProfileService.refreshAccessToken(headers, request);
        return ResponseEntity.status(HttpStatus.OK)
                .body(StriderResponse.responseBuilder(response, RefreshTokenResponseDto.class));
    }

    @GetMapping(value = "/{userId}/visibility")
    public ResponseEntity<StriderResponse<?>> isUserPublic(@RequestHeader HttpHeaders headers, @PathVariable("userId") String userId){
        Boolean response = userProfileService.isUserPublic(userId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(StriderResponse.responseBuilder(response, Boolean.class));
    }

    @PatchMapping(value = "/{userId}/visibility")
    public ResponseEntity<StriderResponse<?>> updateUserVisibility(
            @RequestHeader HttpHeaders headers,
            @PathVariable("userId") String userId,
            @RequestBody UpdateVisibilityRequest request
    ) {
        String currentUserId = tokenUtils.getUidFrom(headers);
        userProfileService.updateUserVisibility(currentUserId, userId, request.isPublic());

        return ResponseEntity.status(HttpStatus.OK)
                .body(StriderResponse.responseBuilder(null, Void.class));
    }

    @GetMapping(value = "/{userId}")
    public ResponseEntity<StriderResponse<Boolean>> isExistUser(@RequestHeader HttpHeaders headers, @PathVariable("userId") String userId){
        Boolean response = userProfileService.isExistUser(userId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(StriderResponse.responseBuilder(response, Boolean.class));
    }

    @PostMapping(value = "/find")
    public ResponseEntity<StriderResponse<UserProfileResponse>> findUserProfile(@RequestHeader HttpHeaders headers, @RequestBody FindUserProfileRequest request){
        String userId = tokenUtils.getUidFrom(headers);
        UserProfileResponse response = userProfileService.findUserProfile(request, userId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(StriderResponse.responseBuilder(response, UserProfileResponse.class));
    }

    @PostMapping(value = "/find/batch")
    public ResponseEntity<StriderResponse<?>> findSimpleUserProfileList(@RequestHeader HttpHeaders headers, @RequestBody FindUserProfileListRequest request){
        List<SimpleUserProfileResponse> response = userProfileService.findUserProfileInFeedList(request);

        return ResponseEntity.status(HttpStatus.OK)
                .body(StriderResponse.responseBuilder(response, List.class));
    }

    @PostMapping(value = "/delete")
    public ResponseEntity<StriderResponse<Void>> deleteUser(@RequestHeader HttpHeaders headers, @RequestBody StriderUserDeleteRequest request) {
        userProfileService.deleteUser(request);
        return ResponseEntity.status(HttpStatus.OK)
                .body(StriderResponse.responseBuilder(null, Void.class));
    }

    @PostMapping(value = "/update")
    public ResponseEntity<?> updateUserProfile(@RequestHeader HttpHeaders headers, @RequestBody UserProfileUpdateRequest request) {
        UserProfileResponse response = userProfileService.updateUserProfile(request);
        return ResponseEntity.status(HttpStatus.OK)
                .body(StriderResponse.responseBuilder(response, Object.class));
    }

    @PostMapping("/image")
    public ResponseEntity<StriderResponse<?>> saveUserProfileImage(@RequestHeader HttpHeaders headers, @RequestBody UserProfileImageRequest request) {
        userProfileService.saveProfileImage(request);

        // 성공 여부만 반환
        return ResponseEntity.status(HttpStatus.OK)
                .body(StriderResponse.responseBuilder(null, Void.class));
    }

    @PostMapping(value = "/image/delete")
    public ResponseEntity<?> deleteProfileImage(@RequestHeader HttpHeaders headers, @RequestBody UserProfileImageRequest request) {
        userProfileService.deleteProfileImage(request);

        return ResponseEntity.status(HttpStatus.OK)
                .body(StriderResponse.responseBuilder(null, Void.class));
    }

    @PostMapping(value = "/report")
    public ResponseEntity<StriderResponse<Object>> createReport(@RequestHeader HttpHeaders headers, @RequestBody UserReport userReport) {
        String userId = tokenUtils.getUidFrom(headers);
        Object response = userProfileService.createReport(userReport, userId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(StriderResponse.responseBuilder(response, Object.class));
    }

    @PostMapping("/block/{id}")
    public ResponseEntity<StriderResponse<Void>> blockUser(
            @RequestHeader HttpHeaders headers,
            @PathVariable String id) {
        String userId = tokenUtils.getUidFrom(headers);
        userBlockService.blockUser(id, userId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(StriderResponse.responseBuilder(null, Void.class));
    }

    @PostMapping("/release/{id}")
    public ResponseEntity<StriderResponse<Void>> releaseBlock(
            @RequestHeader HttpHeaders headers,
            @PathVariable String id) {
        String userId = tokenUtils.getUidFrom(headers);
        userBlockService.releaseBlock(id, userId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(StriderResponse.responseBuilder(null, Void.class));
    }

    @GetMapping("/{id}/following")
    public ResponseEntity<StriderResponse<?>> getFollowing(
            @RequestHeader HttpHeaders headers,
            @PathVariable String id
    ) {
        String currentUserId = tokenUtils.getUidFrom(headers);
        List<UserFollowResponse> followingList = userFollowService.getFollowingList(id, currentUserId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(StriderResponse.responseBuilder(followingList, List.class));
    }

    @GetMapping("/{id}/followers")
    public ResponseEntity<StriderResponse<?>> getFollowers(
            @RequestHeader HttpHeaders headers,
            @PathVariable String id
    ) {
        String currentUserId = tokenUtils.getUidFrom(headers);
        List<UserFollowResponse> followerList = userFollowService.getFollowerList(id, currentUserId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(StriderResponse.responseBuilder(followerList, List.class));
    }

    @GetMapping("/{id}/follow-relations")
    public ResponseEntity<StriderResponse<?>> getFollowRelations(
            @RequestHeader HttpHeaders headers,
            @PathVariable String id
    ) {
        String currentUserId = tokenUtils.getUidFrom(headers);
        List<UserFollowResponse> followerList = userFollowService.getFollowRelation(id, currentUserId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(StriderResponse.responseBuilder(followerList, List.class));
    }

    @PostMapping("/{followerId}/follow/{followingId}")
    public ResponseEntity<StriderResponse<?>> followUser(
            @RequestHeader HttpHeaders headers,
            @PathVariable String followerId,
            @PathVariable String followingId
    ) {
        userFollowService.follow(followerId, followingId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(StriderResponse.responseBuilder(null, Void.class));
    }

    @DeleteMapping("/{followerId}/unfollow/{followingId}")
    public ResponseEntity<StriderResponse<?>> unfollowUser(
            @RequestHeader HttpHeaders headers,
            @PathVariable String followerId,
            @PathVariable String followingId
    ) {
        userFollowService.unfollow(followerId, followingId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(StriderResponse.responseBuilder(null, Void.class));
    }

    // 팔로우 요청 수락 (userId = 요청 받은 사람 = 나, followerId = 요청한 사람)
    @PostMapping("/{userId}/follow-requests/{followerId}/accept")
    public ResponseEntity<StriderResponse<?>> acceptFollowRequest(
            @RequestHeader HttpHeaders headers,
            @PathVariable String userId,
            @PathVariable String followerId
    ) {
        userFollowService.acceptFollowRequest(userId, followerId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(StriderResponse.responseBuilder(null, Void.class));
    }

    // 팔로우 요청 거절
    @DeleteMapping("/{userId}/follow-requests/{followerId}")
    public ResponseEntity<StriderResponse<?>> rejectFollowRequest(
            @RequestHeader HttpHeaders headers,
            @PathVariable String userId,
            @PathVariable String followerId
    ) {
        userFollowService.rejectFollowRequest(userId, followerId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(StriderResponse.responseBuilder(null, Void.class));
    }

    @GetMapping("/settings")
    public ResponseEntity<StriderResponse<?>> getUserSettings(@RequestHeader HttpHeaders headers) {
        String userId = tokenUtils.getUidFrom(headers);

        UserSettingResponse response = userSettingService.getUserSettings(userId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(StriderResponse.responseBuilder(response, UserSettingResponse.class));
    }

    @PostMapping("/update/settings")
    public ResponseEntity<StriderResponse<?>> updateUserSettings(
            @RequestHeader HttpHeaders headers,
            @RequestBody UserSettingUpdateRequest request) {
        String userId = tokenUtils.getUidFrom(headers);

        userSettingService.updateUserSettings(userId, request);
        return ResponseEntity.status(HttpStatus.OK)
                .body(StriderResponse.responseBuilder(null, Void.class));
    }

    @PostMapping(value = "/update/password")
    public ResponseEntity<StriderResponse<?>> updateUserPassword(
            @RequestHeader HttpHeaders headers, @RequestBody UpdatePasswordRequest request){
        userProfileService.updateUserPassword(headers, request);
        return ResponseEntity.status(HttpStatus.OK)
                .body(StriderResponse.responseBuilder(null, Void.class));
    }
}
