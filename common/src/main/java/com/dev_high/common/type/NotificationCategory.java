package com.dev_high.common.type;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public enum NotificationCategory {
    AUCTION(List.of(Type.AUCTION_NO_BID, Type.AUCTION_CLOSED)),
    DEPOSIT(List.of(Type.DEPOSIT)),
    ORDER(List.of(Type.ORDER_CREATED, Type.ORDER_STARTED, Type.ORDER_COMPLETED, Type.ORDER_CANCELED)),
    PRODUCT(List.of(Type.PRODUCT)),
    SEARCH(List.of(Type.SEARCH)),
    SETTLEMENT(List.of(Type.SETTLEMENT_SUCCESS, Type.SETTLEMENT_FAILED)),
    USER(List.of(Type.USER)),
    WISHLIST(List.of(Type.WISHLIST)),
    GENERAL(List.of(Type.GENERAL));

    private final List<Type> types;

    NotificationCategory(List<Type> types) { this.types = types; }

    public static NotificationCategory fromType(Type type) {
        return Arrays.stream(values())
                .filter(category -> category.types.contains(type))
                .findFirst()
                .orElse(GENERAL);
    }

    @Getter
    public enum Type {
        AUCTION_NO_BID("경매 유찰"),
        AUCTION_CLOSED("경매 종료"),
        DEPOSIT("예치금"),
        ORDER_CREATED("주문 생성"),
        ORDER_STARTED("배송중"),
        ORDER_COMPLETED("배송 완료"),
        ORDER_CANCELED("주문 취소"),
        PRODUCT("상품"),
        SEARCH("검색"),
        SETTLEMENT_SUCCESS("정산 완료"),
        SETTLEMENT_FAILED("정산 오류"),
        USER("사용자"),
        WISHLIST("찜하기"),
        GENERAL("새로운 알림");

        private final String defaultTitle;

        Type(String defaultTitle) { this.defaultTitle = defaultTitle; }
    }
}
