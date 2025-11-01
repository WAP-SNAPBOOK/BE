package com.example.easybooking.shop.dto;

import lombok.Value;

@Value
public class LinkInfoResponse {
    String fullUrl; // ex) https://yourdomain.com/s/{slugOrCode}
    String canonicalUrl; // ex) /s/{slugOrCode}
    String slug;
    String publicCode;

    public static LinkInfoResponse of(String fullUrl, String canonicalUrl, String slug, String publicCode) {

        return new LinkInfoResponse(fullUrl, canonicalUrl, slug, publicCode);
    }
}
