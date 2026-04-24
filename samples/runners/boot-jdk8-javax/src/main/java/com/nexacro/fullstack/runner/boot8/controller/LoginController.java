package com.nexacro.fullstack.runner.boot8.controller;

import com.nexacro.fullstack.business.domain.user.UserService;
import com.nexacro.fullstack.business.uiadapter.NexacroController;
import com.nexacro.fullstack.business.uiadapter.NexacroResponseBuilder;
import com.nexacro.fullstack.business.xapi.NexacroDataset;
import com.nexacro.fullstack.business.xapi.NexacroEnvelope;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

@RestController
public class LoginController extends NexacroController {

    private final UserService userService;

    public LoginController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login.do")
    public NexacroEnvelope login(@RequestBody NexacroEnvelope req, HttpSession session) {
        String userId   = asString(parameterById(req, "userId"));
        String password = asString(parameterById(req, "password"));
        NexacroDataset output = userService.login(userId, password);
        if (output.getRows() != null && !output.getRows().isEmpty()) {
            session.setAttribute("USER_ID", userId);
        }
        return NexacroResponseBuilder.ok(output);
    }

    @PostMapping("/logout.do")
    public NexacroEnvelope logout(HttpSession session) {
        session.invalidate();
        return NexacroResponseBuilder.ok();
    }

    private static String asString(Object v) {
        return v == null ? null : v.toString();
    }
}
