package com.example.jetty11h2c;

import java.util.Collections;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AppController {

  @GetMapping(path = "/", produces = MediaType.APPLICATION_JSON_VALUE)
  public Map<String, String> index() {
    return Collections.singletonMap("response", "Hello World");
  }
}
