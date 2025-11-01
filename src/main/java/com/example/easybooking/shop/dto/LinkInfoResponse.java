package com.example.easybooking.shop.dto;

import lombok.Value;

@Value
public class LinkInfoResponse {
    String canonicalUrl; // ex) /s/{slugOrCode}
    String slug;
    String publicCode;

    public static LinkInfoResponse of(String canonicalUrl, String slug, String publicCode) {
        return new LinkInfoResponse(canonicalUrl, slug, publicCode);
    }
}
