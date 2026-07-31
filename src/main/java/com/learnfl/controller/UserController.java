package com.learnfl.controller;

import com.learnfl.common.Result;
import com.learnfl.dto.auth.UpdateUserRequest;
import com.learnfl.dto.auth.UserVO;
import com.learnfl.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;

    @PutMapping("/me")
    public Result<UserVO> updateMe(@RequestBody UpdateUserRequest req) {
        return Result.ok(authService.updateMe(req));
    }
}
