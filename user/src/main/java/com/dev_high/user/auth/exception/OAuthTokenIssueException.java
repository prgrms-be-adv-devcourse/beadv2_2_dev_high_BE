package com.dev_high.user.auth.exception;

import com.dev_high.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class OAuthTokenIssueException extends CustomException {

    public OAuthTokenIssueException() {
        super(
                HttpStatus.BAD_GATEWAY,
                "소셜 로그인 토큰 응답이 올바르지 않습니다."
        );
    }
}
