package com.dev_high.user.seller.domain;

public enum SellerStatus {
    PENDING, // 판매자 등록 신청한 판매자
    ACTIVE, // 정상활동 가능한 판매자
    INACTIVE, // 판매 활동을 비활성환 판매자
    BLACKLISTED, // 블랙리스트에 등록된 판매 제한 판매자
    WITHDRAWN // 탈퇴 판매자
}
