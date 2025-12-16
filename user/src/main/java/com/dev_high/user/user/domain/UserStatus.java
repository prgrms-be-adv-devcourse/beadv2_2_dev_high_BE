package com.dev_high.user.user.domain;

public enum UserStatus {
    ACTIVE, // 정상활동 가능한 회원
    BLACKLISTED, // 블랙리스트에 등록된 서비스 이용 제한 회원
    WITHDRAWN // 탈퇴 회원
}
