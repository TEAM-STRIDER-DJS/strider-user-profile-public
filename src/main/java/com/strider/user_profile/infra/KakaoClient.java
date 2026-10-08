package com.strider.user_profile.infra;

import com.strider.user_profile.model.response.KakaoUserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "kakaoClient", url = "https://kapi.kakao.com")
public interface KakaoClient {

    @GetMapping("/v2/user/me")
    KakaoUserResponse getUserInfo(@RequestHeader("Authorization") String bearerToken);
}
