package com.dev_high.product.ai.dto;

public enum DraftTonePreset {
    BALANCED("친절하고 담백한 판매글 톤", "요약 1~2문장 + 핵심 불릿", "장점 1~2개 + 상태/구성품을 균형 있게"),
    FRIENDLY("친절하고 자연스러운 중고거래 말투", "자연스러운 문장형", "사용감/상태/구성품을 우선"),
    INFO("깔끔한 정보형(군더더기 없이)", "요약 1문장 + 불릿", "하자/주의사항을 먼저"),
    PREMIUM("단정하고 신뢰감 있는 톤", "핵심 장점→상태→구성 순", "스펙/특징을 우선");

    public final String tone;
    public final String format;
    public final String focus;

    DraftTonePreset(String tone, String format, String focus) {
        this.tone = tone;
        this.format = format;
        this.focus = focus;
    }

    public static String buildExtraContext(Integer regenCount) {
        int rc = (regenCount == null) ? 0 : regenCount;

        DraftTonePreset[] presets = DraftTonePreset.values();
        DraftTonePreset p = presets[Math.floorMod(rc, presets.length)];

        return String.join("\n",
                "tone=" + p.tone,
                "format=" + p.format,
                "focus=" + p.focus,
                "variationKey=regen-" + regenCount
        );
    }
}

