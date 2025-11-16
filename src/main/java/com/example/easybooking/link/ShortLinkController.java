package com.example.easybooking.link;

import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/s")
public class ShortLinkController {
    private final LinkService linkService;

    @Value("${frontend-base-url}")
    private String frontendBaseUrl;

    @GetMapping("/{slugOrCode}")
    public ResponseEntity<Void> redirectToFrontend(@PathVariable String slugOrCode) {
        linkService.resolveShop(slugOrCode);
        URI to = URI.create(frontendBaseUrl + "/s/" + slugOrCode);
        return ResponseEntity.status(302).location(to).build();
    }
}
