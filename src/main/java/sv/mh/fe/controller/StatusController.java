package sv.mh.fe.controller;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StatusController {

    @GetMapping({"/status", "/firma/status"})
    public ResponseEntity<Map<String, Object>> status() {
        Map<String, Object> body = new HashMap<>();
        body.put("ok", true);
        body.put("service", "api-firma");
        body.put("time", Instant.now().toString());
        return ResponseEntity.ok(body);
    }
}
