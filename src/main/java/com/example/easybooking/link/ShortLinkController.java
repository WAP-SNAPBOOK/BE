package com.example.easybooking.link;

import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/s")
public class ShortLinkController {
    @Value("${frontend-base-url}")
    private String frontendBaseUrl;

    @GetMapping("/{slugOrCode}")
    public ResponseEntity<Void> redirectToFrontend(@PathVariable String slugOrCode) {
        URI to = URI.create(frontendBaseUrl + "/s/" + slugOrCode);
        return ResponseEntity.status(302).location(to).build();
    }
}
