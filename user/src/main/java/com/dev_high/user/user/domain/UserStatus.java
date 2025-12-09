package com.dev_high.user.user.domain;

public enum UserStatus {
    PENDING_VERIFICATION, // 회원은 생성되었지만, 이메일 인증을 완료하지 않음
    ACTIVE, // 정상활동 가능한 회원
    BLACKLISTED, // 블랙리스트에 등록된 서비스 이용 제한 회원
    WITHDRAWN // 탈퇴 회원
}
