package com.dev_high.user.user.application;

import com.dev_high.user.seller.application.SellerService;
import com.dev_high.user.user.application.dto.*;
import com.dev_high.user.user.domain.Address;
import com.dev_high.user.user.domain.AddressRepository;
import com.dev_high.user.user.domain.User;
import com.dev_high.user.user.domain.UserRepository;
import com.dev_high.user.user.exception.AddressNotFoundException;
import com.dev_high.user.user.exception.AddressNotOwnedException;
import com.dev_high.user.user.exception.UserAlreadyExistsException;
import com.dev_high.user.user.util.EmailMasker;
import lombok.RequiredArgsConstructor;
import com.dev_high.common.dto.ApiResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SellerService sellerService;
    private final UserDomainService userDomainService;
    private final ApplicationEventPublisher publisher;
    private final AddressRepository addressRepository;

    @Transactional
    public ApiResponseDto<UserResponse> create(CreateUserCommand command){
        if(userRepository.existsByEmailAndDeletedYn(command.email(), "N")) {
            throw new UserAlreadyExistsException();
        }

        User user = new User(
                command.email(),
                passwordEncoder.encode(command.password()),
                command.name(),
                command.nickname(),
                command.phone_number()
        );

        User saved = userRepository.save(user);
        userDomainService.assignRoleToUser(saved, "USER");

        if (saved != null) {
            try {
                publisher.publishEvent(saved.getId());
            } catch (Exception e) {
                log.error(">>{ }", e);
            }
        }

        return ApiResponseDto.success(
                "회원 가입이 정상적으로 처리되었습니다.",
                UserResponse.from(saved)
        );
    }

    @Transactional(readOnly = true)
    public ApiResponseDto<UserResponse> getProfile() {
        User user = userDomainService.getUser();
        return ApiResponseDto.success(
                "회원 정보가 정상적으로 조회되었습니다.",
                UserResponse.from(user)
        );
    }

    @Transactional
    public ApiResponseDto<UserResponse> updateProfile(UpdateUserCommand command) {
        User user = userDomainService.getUser();
        user.updateUser(command);
        return ApiResponseDto.success(
                "회원 정보가 정상적으로 변경되었습니다.",
                UserResponse.from(user)
        );
    }

    @Transactional
    public ApiResponseDto<Void> updatePassword(UpdatePasswordCommand command) {
        User user = userDomainService.getUser();
        user.updatePassWord(passwordEncoder.encode(command.password()));
        return ApiResponseDto.success(
                "비밀번호가 정상적으로 변경되었습니다.",
                null
        );
    }

    @Transactional
    public ApiResponseDto<Void> delete() {
        User user = userDomainService.getUser();
        Set<String> roles = userDomainService.getUserRoles(user);

        if (roles.contains("SELLER")) {
            sellerService.removeSeller();
        }

        roles.stream()
                .filter(roleName -> !roleName.equals("SELLER"))
                .forEach(roleName -> userDomainService.revokeRoleFromUser(user, roleName));

        user.remove();
        return ApiResponseDto.success(
                "회원 탈퇴가 정상적으로 처리되었습니다.",
                null
        );
    }

    public ApiResponseDto<List<UserNicknameEmailResponse>> getUserNicknameAndEmail(UserNicknameEmailCommand command) {
        if (command.userIds() == null || command.userIds().isEmpty()) {
            return ApiResponseDto.success(List.of());
        }

        List<User> users = userRepository.findByUserIds(command.userIds());

        Map<String, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        List<UserNicknameEmailResponse> result = new ArrayList<>(command.userIds().size());

        for (String userId : command.userIds()) {
            User user = userMap.get(userId);

            if (user == null || "Y".equals(user.getDeletedYn())) {
                result.add(new UserNicknameEmailResponse(userId, null, null));
                continue;
            }

            result.add(
                    new UserNicknameEmailResponse(
                            user.getId(),
                            EmailMasker.mask(user.getEmail()),
                            user.getNickname()
                    )
            );
        }

        return ApiResponseDto.success(result);
    }

    public ApiResponseDto<List<UserAddressResponse>> getAddressList() {
        String userId = userDomainService.getUser().getId();
        List<Address> addresses = addressRepository.findByUserIdOrderByCreatedAtDesc(userId);
        List<UserAddressResponse> responseList = addresses.stream()
                .map(UserAddressResponse::of)
                .toList();
        return ApiResponseDto.success("주소지가 정상적으로 조회되었습니다.", responseList);
    }

    @Transactional
    public ApiResponseDto<UserAddressResponse> registerAddress(UserAddressCommand command) {
        String userId = userDomainService.getUser().getId();

        if(command.isDefault()) {
            Optional<Address> currentDefaultAddress = getCurrentDefaultAddress(userId);
            currentDefaultAddress.ifPresent(Address::clearDefault);
        }

        Address address = new Address(command.zipcode(), command.state(), command.city(), command.detail(),  command.isDefault(), userId);

        Address saved = addressRepository.save(address);

        return ApiResponseDto.success("주소지가 정상적으로 저장되었습니다.", UserAddressResponse.of(saved));
    }

    @Transactional
    public ApiResponseDto<UserAddressResponse> updateAddress(String addressId, UserAddressCommand command) {
        String userId = userDomainService.getUser().getId();
        Address address = getAddress(addressId);

        if(!userId.equals(address.getUserId())) {
            throw new AddressNotOwnedException();
        }

        Optional<Address> currentDefaultAddress = getCurrentDefaultAddress(userId);

        String msg = "주소지가 정상적으로 변경되었습니다.";

        if(command.isDefault()) {
            currentDefaultAddress.ifPresent(Address::clearDefault);
            address.makeDefault();
        } else {
            if(currentDefaultAddress.isPresent() && !currentDefaultAddress.get().equals(address)) {
                address.clearDefault();
            } else {
                msg = "기본 배송지는 해제할 수 없습니다.";
            }
        }
        address.update(command.zipcode(), command.state(), command.city(), command.detail(), userId);
        return ApiResponseDto.success(msg, UserAddressResponse.of(address));
    }


    public ApiResponseDto<Void> deleteAddress(String addressId) {
        String userId = userDomainService.getUser().getId();
        Address address = getAddress(addressId);

        if(!userId.equals(address.getUserId())) {
            throw new AddressNotOwnedException();
        }

        if(address.getIsDefault()) {
            addressRepository
                    .findFirstByUserIdAndIsDefaultFalseOrderByUpdatedAtDesc(userId)
                    .ifPresent(Address::makeDefault);
        }

        addressRepository.delete(address);

        return ApiResponseDto.success("주소지가 정상적으로 삭제되었습니다.", null);
    }

    private Address getAddress(String addressId) {
        return addressRepository.findById(addressId)
                .orElseThrow(AddressNotFoundException::new);
    }

    private Optional<Address> getCurrentDefaultAddress(String userId) {
        return addressRepository.findCurrentDefaultAddress(userId);
    }
}
