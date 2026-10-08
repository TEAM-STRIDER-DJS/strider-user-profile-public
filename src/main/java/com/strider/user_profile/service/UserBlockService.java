package com.strider.user_profile.service;

import com.strider.strider_common_lib.error.StriderErrorCodes;
import com.strider.strider_common_lib.exception.StriderException;
import com.strider.user_profile.model.entity.StriderUser;
import com.strider.user_profile.model.entity.UserBlock;
import com.strider.user_profile.model.request.UserBlockRequest;
import com.strider.user_profile.repository.UserBlockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserBlockService {
    private final UserBlockRepository userBlockRepository;

    @Transactional
    public void blockUser(String targetId, String userId) {
        UserBlock userBlock = userBlockRepository.findByBlockerIdAndTargetTypeAndTargetId(userId, "USER", targetId);

        if(userBlock == null){
            // UserBlock 엔티티 생성
            UserBlock block = UserBlock.builder()
                    .blockerId(userId)
                    .targetType("USER")
                    .targetId(targetId)
                    .status("ACTIVE")
                    .build();

            userBlockRepository.save(block);
        } else {
            // 이미 차단 중인지 확인
            if("ACTIVE".equals(userBlock.getStatus())){
                throw new StriderException(StriderErrorCodes.BAD_REQUEST);
            }

            userBlock.setStatus("ACTIVE");
            userBlock.setReleasedAt(null);

            userBlockRepository.save(userBlock);
        }
    }

    @Transactional
    public void releaseBlock(String targetId, String userId) {
        UserBlock userBlock = userBlockRepository.findByBlockerIdAndTargetTypeAndTargetIdAndStatus(userId,"USER", targetId,"ACTIVE");

        if(userBlock == null){
            throw new StriderException(StriderErrorCodes.NOT_FOUND);
        }

        userBlock.setStatus("RELEASED");
        userBlock.setReleasedAt(LocalDateTime.now());

        userBlockRepository.save(userBlock);
    }

}
