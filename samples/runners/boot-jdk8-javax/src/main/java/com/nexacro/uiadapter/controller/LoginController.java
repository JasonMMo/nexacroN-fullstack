package com.nexacro.uiadapter.controller;

import com.nexacro.uiadapter.domain.User;
import com.nexacro.uiadapter.spring.core.annotation.ParamVariable;
import com.nexacro.uiadapter.spring.core.data.NexacroResult;
import com.nexacro.uiadapter.service.UserService;
import javax.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Collections;

/**
 * Login / logout endpoints.
 *
 * <p>Both routes are mapped <b>without</b> the {@code /uiadapter} prefix; the
 * servlet context-path supplies it. On a successful login the user id is
 * stashed in the HTTP session (legacy parity).
 */
@Controller
@RequiredArgsConstructor
public class LoginController {

    private final UserService userService;

    @RequestMapping("/login.do")
    public NexacroResult login(
            @ParamVariable(name = "userId") String userId,
            @ParamVariable(name = "password") String password,
            HttpSession session) {
        NexacroResult result = new NexacroResult();
        User user = userService.login(userId, password);
        if (user == null) {
            result.setErrorCode(-1);
            result.setErrorMsg("invalid credentials");
            result.addDataSet("ds_user", Collections.emptyList());
            return result;
        }
        session.setAttribute("USER_ID", user.getUserId());
        result.addDataSet("ds_user", Collections.singletonList(user));
        return result;
    }

    @RequestMapping("/logout.do")
    public NexacroResult logout(HttpSession session) {
        session.invalidate();
        return new NexacroResult();
    }
}
