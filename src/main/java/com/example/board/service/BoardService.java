package com.example.board.service;

import com.example.board.dao.BoardDao;
import com.example.board.dto.Board;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor// 생성자 주입 : Spring이 UserService를 Bean으로 생성할 때 생성자를 이용해 생성하는데, 이때 UserDao Bean이 있는지 보고 그 빈을 주입한다. 생성자 주입 public UserService(UserDao userDao){this.userDao = userDao;}
public class BoardService {
    private final BoardDao boardDao;

    // 글 등록
    @Transactional
    public void addBoard(int userId, String title, String content) {
        boardDao.addBoard(userId, title, content);
    }

    //페이징 : 글 갯수 조회
    @Transactional(readOnly = true)//조회만 할 때는 readOnly=true
    public int getTotalCount() {
        return boardDao.getTotalCount();
    }

    //글 목록 조회
    @Transactional(readOnly = true)
    public List<Board> getBoards(int page) {
        return boardDao.getBoards(page);
    }

    // 글 상세보기
    @Transactional
    public Board getBoard(int boardId) {
        return getBoard(boardId, true);
    }

    // 글 수정 시 글 상세보기
    @Transactional
    public Board getBoard(int boardId, boolean updateViewCnt){
        // updateViewCnt true면 글의 조회수 증가, false면 글의 조회수를 증가하지 않도록 한다.
        Board board = boardDao.getBoard(boardId);
        // id에 해당하는 게시물의 조회수도 1 증가한다.
        if(updateViewCnt) {
            boardDao.updateViewCnt(boardId);
        }
        return board;
    }

    // 글 삭제
    @Transactional
    public void deleteBoard(int userId, int boardId) {
        //boardId에 해당하는 글을 읽어온다.
        Board board = boardDao.getBoard(boardId);
        if(board.getUserId() == userId){
            boardDao.deleteBoard(boardId);
        }
    }

    // 관리자 권한일 경우 삭제
    @Transactional
    public void deleteBoard(int boardId){
        boardDao.deleteBoard(boardId);
    }

    @Transactional
    public void updateBoard(int boardId, String title, String content) {
        boardDao.updateBoard(boardId, title, content);
    }
}
