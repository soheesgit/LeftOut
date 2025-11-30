package com.example.demo.TEST_001.service;

import com.example.demo.TEST_001.dto.UserDTO;
import com.example.demo.TEST_001.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    // 회원가입
    @Transactional
    public void signup(UserDTO userDTO) {
        // 아이디 중복 체크
        if (userRepository.existsByUsername(userDTO.getUsername())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        // 비밀번호 암호화
        String encryptedPassword = passwordEncoder.encode(userDTO.getPassword());
        userDTO.setPassword(encryptedPassword);

        // 사용자 저장
        userRepository.save(userDTO);
    }

    // 로그인 검증
    @Transactional(readOnly = true)
    public UserDTO login(String username, String password) {
        // 사용자 조회
        UserDTO user = userRepository.findByUsername(username);

        if (user == null) {
            log.warn("존재하지 않는 아이디로 로그인 시도: {}", username);
            throw new IllegalArgumentException("존재하지 않는 아이디입니다.");
        }

        // 비밀번호 확인
        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("비밀번호 불일치 - 사용자: {}", username);
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        log.info("로그인 성공: {}", username);
        return user;
    }

    // 아이디 중복 체크
    @Transactional(readOnly = true)
    public boolean checkUsername(String username) {
        return userRepository.existsByUsername(username);
    }
}
