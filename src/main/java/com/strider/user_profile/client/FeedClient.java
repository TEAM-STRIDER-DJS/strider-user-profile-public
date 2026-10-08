package com.strider.user_profile.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "feedClient", url = "http://strider-feed:8004")
// @FeignClient(name = "feedClient", url = "http://localhost:8004")
public interface FeedClient {
    // 유저의 게시물 수 조회
    @GetMapping(value = "/api/v1/feed/user/{feedUserId}/count")
    Envelope<Long> getUserFeedCount(@PathVariable("feedUserId") String feedUserId);
}
