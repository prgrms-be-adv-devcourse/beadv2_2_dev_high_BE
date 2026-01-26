package com.dev_high.user.auth.exception;

import com.dev_high.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class OAuthProfileInvalidException extends CustomException {

    public OAuthProfileInvalidException() {
        super(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "소셜 로그인 사용자 정보 응답이 올바르지 않습니다."
        );
    }
}
