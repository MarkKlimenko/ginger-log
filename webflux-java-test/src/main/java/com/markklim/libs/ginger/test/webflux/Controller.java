package com.markklim.libs.ginger.test.webflux;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1")
public class Controller {

    @PostMapping("test1")
    public String test() {
        return "OK";
    }
}
