package com.dev_high.user.auth.exception;

import com.dev_high.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class OAuthAccessTokenNotFoundException  extends CustomException {
    public OAuthAccessTokenNotFoundException() {
        super(
                HttpStatus.BAD_REQUEST,
                "소셜 로그인 인증 정보가 유효하지 않습니다."
        );
    }
}
