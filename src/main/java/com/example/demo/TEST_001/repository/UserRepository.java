package com.example.demo.TEST_001.repository;

import com.example.demo.TEST_001.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class UserRepository {
    private final SqlSessionTemplate sql;

    // 회원가입
    public void save(UserDTO userDTO) {
        sql.insert("user.save", userDTO);
    }

    // 아이디로 사용자 조회
    public UserDTO findByUsername(String username) {
        return sql.selectOne("user.findByUsername", username);
    }

    // 아이디 중복 체크
    public boolean existsByUsername(String username) {
        Integer count = sql.selectOne("user.existsByUsername", username);
        return count != null && count > 0;
    }

    // ID로 사용자 조회
    public UserDTO findById(Long userId) {
        return sql.selectOne("user.findById", userId);
    }

    // 이메일로 사용자 조회
    public UserDTO findByEmail(String email) {
        return sql.selectOne("user.findByEmail", email);
    }

    // 이메일 설정 업데이트
    public void updateEmailSettings(Long userId, String email, Boolean emailNotificationEnabled) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("email", email);
        params.put("emailNotificationEnabled", emailNotificationEnabled);
        sql.update("user.updateEmailSettings", params);
    }
}
