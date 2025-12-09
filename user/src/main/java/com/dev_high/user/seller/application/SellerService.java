package com.dev_high.user.seller.application;

import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.user.seller.application.dto.CreateSellerCommand;
import com.dev_high.user.seller.application.dto.SellerInfo;
import com.dev_high.user.seller.domain.Seller;
import com.dev_high.user.seller.domain.SellerRepository;
import com.dev_high.user.seller.exception.SellerAlreadyExistsException;
import com.dev_high.user.user.application.UserService;
import com.dev_high.user.user.domain.User;
import com.dev_high.user.user.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SellerService {

    private final SellerRepository sellerRepository;
    private final UserService userService;

    @Transactional
    public ApiResponseDto<SellerInfo> create(CreateSellerCommand command) {
        User user = userService.findById(command.userId()).orElseThrow(() -> new UserNotFoundException());
        if(sellerRepository.existsByUserId(user.getId())) {
            throw new SellerAlreadyExistsException();
        }
        Seller seller = new Seller(
                user,
                command.bankName(),
                command.bankAccount()
        );
        Seller saved = sellerRepository.save(seller);
        return ApiResponseDto.success(SellerInfo.from(saved));
    }
}
