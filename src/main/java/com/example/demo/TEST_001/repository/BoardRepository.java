package com.example.demo.TEST_001.repository;

import com.example.demo.TEST_001.dto.BoardDTO;
import lombok.RequiredArgsConstructor;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class BoardRepository {
    private final SqlSessionTemplate sql;

    public List<BoardDTO> getList() {
        return sql.selectList("board.getList");
    }

    public void save(BoardDTO boardDTO) {
        sql.insert("board.save", boardDTO);
    }

    public BoardDTO detail(Integer id) {
        return sql.selectOne("board.detail", id);
    }

    // 도서정보 삭제하기
    public void goDelete(Integer id) {
        sql.delete("board.goDelete", id);
    }

    public void goUpdate(BoardDTO boardDTO) {
        sql.update("board.goUpdate", boardDTO);
    }
}
