package com.ruoyi.framework;

import static org.assertj.core.api.Assertions.assertThat;
import com.ruoyi.common.exception.user.UserException;
import com.ruoyi.framework.web.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import java.nio.charset.StandardCharsets;

class GlobalLoginErrorTest {
    @RestController static class Probe {
        @GetMapping("/qa-user-error") public void userError(){throw new UserException("test",null){@Override public String getMessage(){return "验证码已失效";}};}
        @GetMapping("/qa-unknown-error") public void unknownError(){throw new IllegalStateException("internal database secret");}
    }
    @Test void localizedUserFailureIsVisible() throws Exception {
        String body=MockMvcBuilders.standaloneSetup(new Probe()).setControllerAdvice(new GlobalExceptionHandler()).build()
                .perform(MockMvcRequestBuilders.get("/qa-user-error")).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(body).contains("验证码已失效").doesNotContain("系统内部错误");
    }
    @Test void unknownFailureStillHidesInternalDetails() throws Exception {
        String body=MockMvcBuilders.standaloneSetup(new Probe()).setControllerAdvice(new GlobalExceptionHandler()).build()
                .perform(MockMvcRequestBuilders.get("/qa-unknown-error")).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(body).contains("系统内部错误").doesNotContain("internal database secret");
    }
}
