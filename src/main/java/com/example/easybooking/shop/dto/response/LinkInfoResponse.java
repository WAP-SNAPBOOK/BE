package com.example.easybooking.shop.dto.response;

import lombok.Value;

@Value
public class LinkInfoResponse {
    Long shopId;
    String fullUrl; // ex) https://yourdomain.com/s/{slugOrCode}
    String canonicalUrl; // ex) /s/{slugOrCode}
    String slug;
    String publicCode;

    public static LinkInfoResponse of(Long shopId, String fullUrl, String canonicalUrl, String slug, String publicCode) {

        return new LinkInfoResponse(shopId, fullUrl, canonicalUrl, slug, publicCode);
    }
}
