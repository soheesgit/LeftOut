package com.example.demo.TEST_001.controller;

import com.example.demo.TEST_001.dto.BoardDTO;
import com.example.demo.TEST_001.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class BoardController {
    private final BoardService boardService;

    @GetMapping("/list")
    public String getList(Model model) {
        List<BoardDTO> boardDTOList = boardService.getList();

//        for (int i = 0; i < boardDTOList.size(); i++) {
//            if (boardDTOList.get(i) == null) {
//                System.out.println("null값 발견 " + i);
//            } else {
//                System.out.println(boardDTOList.get(i));
//            }
//        }
// 디버깅 법! 밑에 로그 뜬다
        model.addAttribute("bookList", boardDTOList);
        return "bookList";
    }

    @GetMapping("/addbook")
    public String addBook() {
        return "addBook";
    }

    @PostMapping("/addbook")
    public String save(BoardDTO boardDTO) {
        boardService.save(boardDTO);
        return "redirect:/list";
    }

    /* 도서 정보 상세 보기 */
    @GetMapping("/bookid/{id}")
    public String detail(@PathVariable() Integer id, Model model) {
        BoardDTO boardDTO = boardService.detail(id);
        model.addAttribute("bookDetail", boardDTO);
        return "detailBook";
    }

    @GetMapping("/goDelete/{id}")
    public String goDelete(@PathVariable("id") Integer id) {
        boardService.goDelete(id);
        return "redirect:/list";
    }

    //도서정보 수정하기
    @GetMapping("/goUpdate/{id}")
    public String goUpdate(@PathVariable("id") Integer id, Model model) {
        BoardDTO boardDTO = boardService.detail(id);
        model.addAttribute("bookDetail", boardDTO);
        return "updateBook";
    }

    //도서정보 수정 및 저장 !!
    @PostMapping("/goUpdate/{id}")
    public String goUpdate(BoardDTO boardDTO, Model model) {
        //  (1) 도서정보 수정
        boardService.goUpdate(boardDTO);

        // (2) 도서정보 수정 후, 수정된 내용을 다시 조회.
        BoardDTO dto = boardService.detail(boardDTO.getBookid());
        model.addAttribute("bookDetail", dto);
        return "detailBook";
    }
}
