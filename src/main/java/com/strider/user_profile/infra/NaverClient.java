package com.strider.user_profile.infra;

import com.strider.user_profile.model.response.NaverUserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "naverClient", url = "https://openapi.naver.com")
public interface NaverClient {
    @GetMapping("/v1/nid/me")
    NaverUserResponse getUserInfo(@RequestHeader("Authorization") String bearerToken);
}
