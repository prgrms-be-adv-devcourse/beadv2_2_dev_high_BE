package com.dev_high.admin.applicaiton;

import org.springframework.stereotype.Service;

@Service
public class AdminService {



    public void createAuction(){
    // 셀러아이디 상관없이 등록가능
    // 해당상품에 라스트옥션에 해당 데이터 넣어줘야함.
    }

    public void modifyAuction(){
        // 셀러아이디 상관없이 수정가능 (경매상태, 시간 ,가격등)
        // 시작중일때 중지가능 > 이후로직 처리해야함 참여자들 보증금 환불처리 등, 최종낙찰자 처리 등?
        // 대기상태 바로 시작가능 > 상태값 진행으로 바꿀때 시작시간 현재시간으로 박아서 사용
        // 바로 종료처리> 상태값 종료로 바꿀때 종료시간 현재시간으로 바로 적용
    }

    public void removeAuction(){
        // 셀러아이디 상관없이 삭제가능
        // 해당 상품에 라스트옥션 제거
    }
}
