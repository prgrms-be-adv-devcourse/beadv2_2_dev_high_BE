package com.dev_high.settlement.application;

import com.dev_high.common.context.UserContext;
import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.settlement.domain.Settlement;
import com.dev_high.settlement.domain.SettlementRepository;
import com.dev_high.settlement.presentation.dto.SettlementModifyRequest;
import com.dev_high.settlement.presentation.dto.SettlementResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
  public ApiResponseDto<SettlementResponse> findById(String id) {
    Settlement result = settlementRepository.findById(id).orElse(null);
    if (result == null) {
      return ApiResponseDto.fail("NOT FOUND");
    }
    return ApiResponseDto.success("FOUND", result.toResponse());
  }

  /**
   * 판매자 ID로 모든 정산 정보를 조회합니다.
   *
   * @return 해당 판매자의 모든 정산 정보 목록
   */
  public ApiResponseDto<List<SettlementResponse>> findBySellerId() {
    String sellerId = UserContext.get().userId();
    List<Settlement> found = settlementRepository.findAllBySellerId(sellerId);
    List<SettlementResponse> settlementResponseList = found.stream().map(Settlement::toResponse)
        .toList();
    return ApiResponseDto.success(settlementResponseList);
  }

  /**
   * 정산 정보를 수정합니다.
   *
   * @param request 정산 수정 요청 정보
   * @return 수정된 정산 정보 또는 "NOT FOUND" 실패 메시지
   */
  public ApiResponseDto<SettlementResponse> update(SettlementModifyRequest request) {
    Settlement settlement = settlementRepository.findById(request.id()).orElse(null);
    if (settlement == null) {
      return ApiResponseDto.fail("NOT FOUND");
    }
    settlement.updateStatus(request.status());
    settlement = settlementRepository.save(settlement);
    return ApiResponseDto.success("FOUND", settlement.toResponse());
  }
}
