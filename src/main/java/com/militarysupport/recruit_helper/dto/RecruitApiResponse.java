package com.militarysupport.recruit_helper.dto;

import java.util.List;

// 응답 루트
public record RecruitApiResponse(Header header, Body body) {
    public record Header(String resultCode, String resultMsg) {}
    public record Body(Items items, String numOfRows, String pageNo, String totalCount) {}
    public record Items(List<Item> item) {}
}

