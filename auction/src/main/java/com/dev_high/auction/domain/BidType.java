package com.dev_high.auction.domain;

public enum BidType {
  BID_SUCCESS,           // 입찰 성공
  BID_FAIL_LOW_PRICE,    // 입찰가가 현재가보다 낮음
  BID_FAIL_TIME,         // 경매 시간 범위 벗어난 입찰
  BID_FAIL_LOCK,         // 동시 입찰 실패
  BID_WITHDRAW ,      // 사용자가 입찰 포기
  WITHDRAW_COMPLETE
}
