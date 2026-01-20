package com.dev_high.user.seller.application;

import com.dev_high.common.context.UserContext;
import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.common.kafka.event.user.SellerApprovedEvent;
import com.dev_high.user.admin.presentation.dto.AdminSellerListRequest;
import com.dev_high.user.seller.application.dto.SellerApproveResult;
import com.dev_high.user.seller.application.dto.SellerCommand;
import com.dev_high.user.seller.application.dto.SellerResponse;
import com.dev_high.user.seller.domain.Seller;
import com.dev_high.user.seller.domain.SellerRepository;
import com.dev_high.user.seller.domain.SellerSpecification;
import com.dev_high.user.seller.domain.SellerStatus;
import com.dev_high.user.seller.exception.SellerAlreadyExistsException;
import com.dev_high.user.seller.exception.SellerNotFoundException;
import com.dev_high.user.user.application.UserDomainService;
import com.dev_high.user.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SellerService {

    private final SellerRepository sellerRepository;
    private final UserDomainService userDomainService;
    private final ApplicationEventPublisher publisher;

    @Transactional
    public ApiResponseDto<SellerResponse> request(SellerCommand command) {
        User user = userDomainService.getUser();

        Seller seller = sellerRepository.findById(user.getId())
                .map(existing -> {
                    if ("N".equals(existing.getDeletedYn())) {
                        throw new SellerAlreadyExistsException();
                    }
                    existing.revive(command.bankName(), command.bankAccount());
                    return existing;
                })
                .orElseGet(() -> new Seller(user, command.bankName(), command.bankAccount()));

        Seller saved = sellerRepository.save(seller);

        return ApiResponseDto.success(
                "판매자 등록 요청이 정상적으로 처리되었습니다.",
                SellerResponse.from(saved)
        );
    }

    @Transactional(readOnly = true)
    public ApiResponseDto<SellerResponse> getProfile() {
        Seller seller = getSeller();

        return ApiResponseDto.success(
                "판매자 정보가 정상적으로 조회되었습니다.",
                SellerResponse.from(seller)
        );
    }

    @Transactional
    public ApiResponseDto<SellerResponse> updateProfile(SellerCommand command) {
        Seller seller = getSeller();
        seller.update(command.bankName(), command.bankAccount());

        return ApiResponseDto.success(
                "판매자 정보가 정상적으로 변경되었습니다.",
                SellerResponse.from(seller)
        );
    }

    @Transactional
    public ApiResponseDto<Void> removeSeller() {
        Seller seller = getSeller();
        seller.remove();

        User user = seller.getUser();
        userDomainService.revokeRoleFromUser(user, "SELLER");

        return ApiResponseDto.success(
                "판매자 등록 철회가 정상적으로 처리되었습니다.",
                null
        );
    }

    private Seller getSeller() {
        String userId = UserContext.get().userId();

        return sellerRepository.findByIdAndDeletedYn(userId, "N")
                .orElseThrow(SellerNotFoundException::new);
    }

    @Transactional
    public SellerApproveResult approveSellers(List<Seller> targets, String approvedBy) {
        if (targets == null || targets.isEmpty()) {
            return SellerApproveResult.empty();
        }

        int approved = 0;
        int roleInserted = 0;
        int skipped = 0;

        List<String> userIds = new ArrayList<>();

        for (Seller seller : targets) {
            if (seller.getSellerStatus() != SellerStatus.PENDING) {
                skipped++;
                continue;
            }

            User user = userDomainService.getUser(seller.getId());

            seller.markActive(approvedBy);

            if (!userDomainService.getUserRoles(user).contains("SELLER")) {
                userDomainService.assignRoleToUser(user, "SELLER");
                roleInserted++;
                userIds.add(seller.getId());
            }

            approved++;
        }

        sellerRepository.saveAll(targets);

        publisher.publishEvent(
                new SellerApprovedEvent(userIds)
        );
        return new SellerApproveResult(approved, roleInserted, skipped, targets.size());
    }

    public Page<SellerResponse> getAdminSellerList(AdminSellerListRequest request, Pageable pageable) {
        Page<Seller> page = sellerRepository.findAll(
                SellerSpecification.from(request),
                pageable
        );

        return page.map(SellerResponse::from);
    }
}
