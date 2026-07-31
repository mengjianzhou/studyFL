package com.learnfl.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.learnfl.common.BizException;
import com.learnfl.common.UserContext;
import com.learnfl.dto.auth.LoginRequest;
import com.learnfl.dto.auth.LoginResponse;
import com.learnfl.dto.auth.RegisterRequest;
import com.learnfl.dto.auth.UpdateUserRequest;
import com.learnfl.dto.auth.UserVO;
import com.learnfl.entity.User;
import com.learnfl.mapper.UserMapper;
import com.learnfl.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public LoginResponse register(RegisterRequest req) {
        Long exists = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUsername, req.getUsername()));
        if (exists > 0) {
            throw new BizException("用户名已存在");
        }
        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(encoder.encode(req.getPassword()));
        user.setNickname(req.getNickname() != null ? req.getNickname() : req.getUsername());
        userMapper.insert(user);
        return buildLoginResponse(user);
    }

    public LoginResponse login(LoginRequest req) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, req.getUsername()));
        if (user == null || !encoder.matches(req.getPassword(), user.getPassword())) {
            throw new BizException("用户名或密码错误");
        }
        return buildLoginResponse(user);
    }

    public UserVO me() {
        User user = userMapper.selectById(UserContext.userId());
        return toVO(user);
    }

    public UserVO updateMe(UpdateUserRequest req) {
        User user = userMapper.selectById(UserContext.userId());
        if (req.getNickname() != null && !req.getNickname().isBlank()) {
            user.setNickname(req.getNickname());
        }
        if (req.getActiveLanguageId() != null) {
            user.setActiveLanguageId(req.getActiveLanguageId());
        }
        userMapper.updateById(user);
        return toVO(user);
    }

    private LoginResponse buildLoginResponse(User user) {
        String token = jwtUtil.generate(user.getId(), user.getUsername());
        return new LoginResponse(token, toVO(user));
    }

    private UserVO toVO(User user) {
        return new UserVO(user.getId(), user.getUsername(), user.getNickname(), user.getAvatar(), user.getActiveLanguageId());
    }
}
