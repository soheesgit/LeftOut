package com.example.demo.TEST_001.service;

import com.example.demo.TEST_001.dto.BoardDTO;
import com.example.demo.TEST_001.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardService {
    private final BoardRepository boardRepository;

    //도서목록 불러오기
    public List<BoardDTO> getList() {
        return boardRepository.getList();
    }

    //도서정보 추가하기
    public void save(BoardDTO boardDTO) {
        boardRepository.save(boardDTO);
    }

    public BoardDTO detail(Integer id) {
        return boardRepository.detail(id);
    }

    public void goDelete(Integer id) {
        boardRepository.goDelete(id);
    }


    public void goUpdate(BoardDTO boardDTO) {
        boardRepository.goUpdate(boardDTO);
    }
}
