package com.example.board.controller;

import com.example.board.dto.Board;
import com.example.board.dto.LoginInfo;
import com.example.board.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.List;

// http요청 받아 응답하는 컴포넌트. 스프링부트가 자동으로 Bean으로 생성한다.
@Controller
@RequiredArgsConstructor // service 생성자 주입
public class BoardController {
    private final BoardService boardService;

    // 컨트롤러의 메소드가 리턴하는 문자열은 템플릿 이름이다.
    // Model : 템플릿에 값을 전달하기 위한 객체

    // 게시물 목록
    @GetMapping("/")
    public String list(@RequestParam(name = "page", defaultValue = "1") int page, HttpSession session, Model model) { // HttpSession, Model은 Spring이 자동으로 넣어준다
        //게시물 목록 읽어온다.
        LoginInfo loginInfo = (LoginInfo) session.getAttribute("loginInfo");
        model.addAttribute("loginInfo", loginInfo);

        //페이징 처리한다.
        int totalCount = boardService.getTotalCount();
        List<Board> list = boardService.getBoards(page);
        // 전체 페이지 수
        int pageCount = totalCount / 10;
        if (totalCount % 10 > 0) {
            pageCount++;
        }
        // 현재 페이지
        int currentPage = page;
        model.addAttribute("list", list);
        model.addAttribute("pageCount", pageCount);
        model.addAttribute("currentPage", currentPage);
        return "list"; // classpath:/templates/list.html
    }

    // 글 상세보기
    @GetMapping("/board")
    public String board(@RequestParam("boardId") int boardId, Model model) {
        // id에 해당하는 게시물을 읽어온다
        // id에 해당하는 게시물의 조회수도 1 증가한다.
        Board board = boardService.getBoard(boardId);
        model.addAttribute("board", board);
        return "board";
    }

    // 글쓰기 열기
    @GetMapping("/writeForm")
    public String writeForm(HttpSession session, Model model) {
        // 로그인한 사용자만 글을 써야한다.
        // 세션에서 로그인한 정보를 읽어들인다. 로그인 하지 않았다면 리스트보기로 자동 이동 시킨다.
        LoginInfo loginInfo = (LoginInfo) session.getAttribute("loginInfo");
        if (loginInfo == null) {
            return "redirect:/loginform";
        }
        model.addAttribute("loginInfo", loginInfo);

        return "writeForm";
    }

    // 글쓰기 등록
    @PostMapping("/write")
    public String write(
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            HttpSession session
    ) {
        // 로그인한 사용자만 글을 써야함
        // 세션에서 로그인한 정보를 읽어들인다. 로그인 하지 않았다면 리스트보기로 자동 이동 시킨다.
        LoginInfo loginInfo = (LoginInfo) session.getAttribute("loginInfo");
        if (loginInfo == null) {
            return "redirect:/loginform";
        }
        // 로그인한 회원 정보 + 제목, 내용을 저장한다.
        boardService.addBoard(loginInfo.getUserId(), title, content);

        return "redirect:/";
    }

    //글 삭제
    @GetMapping("/delete")
    public String delete(
            @RequestParam("boardId") int boardId,
            HttpSession session
    ) {
        // 세션에 로그인 정보가 없으면 /loginform으로 redirect
        LoginInfo loginInfo = (LoginInfo) session.getAttribute("loginInfo");
        if (loginInfo == null) {
            return "redirect:/loginform";
        }

        List<String> roles = loginInfo.getRoles();
        if(roles.contains("ROLE_ADMIN")){
            // 관리자일 경우 삭제한다.
            boardService.deleteBoard(boardId);
        }else {
            // 이 글의 주인만 삭제한다.
            boardService.deleteBoard(loginInfo.getUserId(), boardId);
        }

        return "redirect:/";
    }

    //글 수정
    @GetMapping("/updateform")
    public String updateform(@RequestParam("boardId") int boardId, Model model, HttpSession session){
        // 세션에 로그인 정보가 없으면 /loginform으로 redirect
        LoginInfo loginInfo = (LoginInfo) session.getAttribute("loginInfo");
        if (loginInfo == null) {
            return "redirect:/loginform";
        }
        //boardId에 해당하는 정보를 읽어와서 updateform 템플릿에 전달한다.
        Board board = boardService.getBoard(boardId, false); // 글읽어오면서 + 조회수 증가함
        model.addAttribute("board", board);
        model.addAttribute("loginInfo", loginInfo);
        return "updateform";
    }

    @PostMapping("/update")
    public String update(@RequestParam("boardId") int boardId,
                         @RequestParam("title") String title,
                         @RequestParam("content") String content,
                         HttpSession session){
        // 세션에 로그인 정보가 없으면 /loginform으로 redirect
        LoginInfo loginInfo = (LoginInfo) session.getAttribute("loginInfo");
        if (loginInfo == null) {
            return "redirect:/loginform";
        }
        // 글쓴이만 수정 가능
        Board board = boardService.getBoard(boardId, false);
        if(board.getUserId() != loginInfo.getUserId()){
            return "redirect: /board?boardId=" + boardId; // 글 상세로 이동
        }
        // boardId에 해당하는 글을 수정한다
        boardService.updateBoard(boardId, title, content);

        return "redirect:/board?boardId=" + boardId; // 수정된 글 보기로 리다이렉트
    }
}
