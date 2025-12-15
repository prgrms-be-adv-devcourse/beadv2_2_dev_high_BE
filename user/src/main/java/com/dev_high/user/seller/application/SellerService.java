package com.dev_high.user.seller.application;

import com.dev_high.common.context.UserContext;
import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.user.seller.application.dto.SellerCommand;
import com.dev_high.user.seller.application.dto.SellerResponse;
import com.dev_high.user.seller.domain.Seller;
import com.dev_high.user.seller.domain.SellerRepository;
import com.dev_high.user.seller.domain.SellerStatus;
import com.dev_high.user.seller.exception.SellerAlreadyExistsException;
import com.dev_high.user.seller.exception.SellerNotFoundException;
import com.dev_high.user.user.application.UserDomainService;
import com.dev_high.user.user.domain.User;
import com.dev_high.user.user.domain.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SellerService {

    private final SellerRepository sellerRepository;
    private final UserDomainService userDomainService;

    @Transactional
    public ApiResponseDto<SellerResponse> create(SellerCommand command) {
        User user = userDomainService.getUser();
        //일반 회원 seller로 변경
        userDomainService.updateUserRole(user, UserRole.SELLER);
        if(sellerRepository.existsByUserId(user.getId())) {
            throw new SellerAlreadyExistsException();
        }
        Seller seller = new Seller(
                user,
                command.bankName(),
                command.bankAccount()
        );
        Seller saved = sellerRepository.save(seller);
        return ApiResponseDto.success(SellerResponse.from(saved));
    }

    @Transactional(readOnly = true)
    public ApiResponseDto<SellerResponse> getProfile() {
        Seller seller = getSeller();
        return ApiResponseDto.success(SellerResponse.from(seller));
    }

    @Transactional
    public ApiResponseDto<SellerResponse> updateProfile(SellerCommand command) {
        Seller seller = getSeller();
        seller.updateSeller(command.bankName(), command.bankAccount());
        return ApiResponseDto.success(SellerResponse.from(seller));
    }

    @Transactional
    public ApiResponseDto<Void> delete() {
        deleteSeller(SellerStatus.INACTIVE);
        User user = userDomainService.getUser();
        userDomainService.updateUserRole(user, UserRole.USER);
        return ApiResponseDto.success(null);
    }

    @Transactional
    public void deleteSeller(SellerStatus status) {
        Seller seller = getSeller();
        seller.deleteSeller(status);
    }

    private Seller getSeller() {
        String userId = UserContext.get().userId();
        return Optional.ofNullable(sellerRepository.findByUserId(userId))
                .filter(s -> !"Y".equals(s.getDeletedYn()))
                .orElseThrow(SellerNotFoundException::new);
    }
}
