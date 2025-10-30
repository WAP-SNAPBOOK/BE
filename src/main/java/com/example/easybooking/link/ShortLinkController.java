package com.example.easybooking.link;

import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/s")
public class ShortLinkController {
    @Value("${app.frontend-base-url:https://ssnapbook.netlify.app}")
    private String frontendBaseUrl;

    @GetMapping("/{slugOrCode}")
    public ResponseEntity<Void> redirectToFrontend(@PathVariable String slugOrCode) {
        URI to = URI.create(frontendBaseUrl + "/s/" + slugOrCode);
        return ResponseEntity.status(302).location(to).build();
    }
}
