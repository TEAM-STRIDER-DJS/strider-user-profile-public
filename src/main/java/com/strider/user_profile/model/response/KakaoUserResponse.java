package com.strider.user_profile.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record KakaoUserResponse (
    Long id,
    @JsonProperty("kakao_account")
    KakaoAccount kakaoAccount
){}
