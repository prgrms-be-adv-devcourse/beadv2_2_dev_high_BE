package com.dev_high.settle.application;

import com.dev_high.common.context.UserContext;
import com.dev_high.common.exception.CustomException;
import com.dev_high.settle.domain.settle.Settlement;
import com.dev_high.settle.domain.settle.SettlementRepository;
import com.dev_high.settle.presentation.dto.SettlementResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 정산 관련 비즈니스 로직을 처리하는 서비스 클래스
 */
@Service
@Transactional
@RequiredArgsConstructor
public class SettlementService {

    private final SettlementRepository settlementRepository;

    /**
     * ID로 정산 정보를 조회합니다.
     *
     * @param id 정산 ID
     * @return 조회된 정산 정보 또는 "NOT FOUND" 실패 메시지
     */
    public SettlementResponse findById(String id) {
        Settlement result = settlementRepository.findById(id).orElse(null);
        if (result == null) {
            throw new CustomException(HttpStatus.NOT_FOUND,"정산이 존재하지 않습니다.");
        }
        return result.toResponse();
    }

    /**
     * 판매자 ID로 모든 정산 정보를 조회합니다.
     *
     * @return 해당 판매자의 모든 정산 정보 목록
     */
    public Page<SettlementResponse> findBySellerId(Pageable pageable) {
        String sellerId = UserContext.get().userId();
        Page<Settlement> found = settlementRepository.findAllBySellerIdOrderByCompleteDateDesc(sellerId, pageable);
        Page<SettlementResponse> settlementResponseList = found.map(Settlement::toResponse);


        return settlementResponseList;
    }

    public Page<SettlementDailySummary> findSettlementSummary(Pageable pageable) {
        String sellerId = UserContext.get().userId();

        return settlementRepository.findDailySummaryBySellerId(sellerId, pageable);
    }


}
