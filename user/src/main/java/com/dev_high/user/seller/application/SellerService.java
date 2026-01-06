package com.dev_high.user.seller.application;

import com.dev_high.common.context.UserContext;
import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.user.seller.application.dto.SellerCommand;
import com.dev_high.user.seller.application.dto.SellerResponse;
import com.dev_high.user.seller.domain.Seller;
import com.dev_high.user.seller.domain.SellerRepository;
import com.dev_high.user.seller.exception.SellerAlreadyExistsException;
import com.dev_high.user.seller.exception.SellerNotFoundException;
import com.dev_high.user.user.application.UserDomainService;
import com.dev_high.user.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SellerService {

    private final SellerRepository sellerRepository;
    private final UserDomainService userDomainService;

    @Transactional
    public ApiResponseDto<SellerResponse> create(SellerCommand command) {
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
        userDomainService.assignRoleToUser(user, "SELLER");

        return ApiResponseDto.success(
                "판매자 등록이 정상적으로 처리되었습니다.",
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
}
