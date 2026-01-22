package com.dev_high.admin.applicaiton;

import com.dev_high.auction.application.dto.AuctionResponse;
import com.dev_high.auction.application.AuctionLifecycleService;
import com.dev_high.auction.domain.AuctionRepository;
import com.dev_high.auction.domain.AuctionStatus;
import com.dev_high.common.context.UserContext;
import com.dev_high.exception.AuctionNotFoundException;
import com.dev_high.exception.AuctionStatusInvalidException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final AuctionRepository auctionRepository;
    private final AuctionLifecycleService lifecycleService;

    @Transactional
    public AuctionResponse startAuctionNow(String auctionId) {
        String userId = resolveAdminUserId();
        var auction = auctionRepository.findById(auctionId)
                .orElseThrow(AuctionNotFoundException::new);
        if (auction.getStatus() != AuctionStatus.READY) {
            throw new AuctionStatusInvalidException();
        }
        return AuctionResponse.fromEntity(lifecycleService.startNow(auctionId, userId));
    }

    @Transactional
    public AuctionResponse endAuctionNow(String auctionId) {
        String userId = resolveAdminUserId();
        var auction = auctionRepository.findById(auctionId)
                .orElseThrow(AuctionNotFoundException::new);
        if (!List.of(AuctionStatus.READY, AuctionStatus.IN_PROGRESS).contains(auction.getStatus())) {
            throw new AuctionStatusInvalidException();
        }
        return AuctionResponse.fromEntity(lifecycleService.endNow(auctionId, userId));
    }


    private String resolveAdminUserId() {
        if (UserContext.get() == null || UserContext.get().userId() == null) {
            return "SYSTEM";
        }
        return UserContext.get().userId();
    }
}
