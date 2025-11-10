package com.example.demo.TEST_001.repository;

import com.example.demo.TEST_001.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

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
}
