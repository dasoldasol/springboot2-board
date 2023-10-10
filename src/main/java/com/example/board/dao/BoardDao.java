package com.example.board.dao;

import com.example.board.dto.Board;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.core.simple.SimpleJdbcInsertOperations;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public class BoardDao {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private final SimpleJdbcInsertOperations insertBoard;

    // 생성자 주입. 스프링이 자동으로 HikariCP Bean을 주입한다.
    public BoardDao(DataSource dataSource){
        jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        insertBoard = new SimpleJdbcInsert(dataSource)
                .withTableName("board")
                .usingGeneratedKeyColumns("board_id");//자동으로 증가되는 id 설정
    }

    // 글 등록
    @Transactional
    public void addBoard(int userId, String title, String content) {
        Board board = new Board();
        board.setUserId(userId);
        board.setTitle(title);
        board.setContent(content);
        board.setRegdate(LocalDateTime.now());
        SqlParameterSource params = new BeanPropertySqlParameterSource(board);
        insertBoard.execute(params); // 자동 생성 id 받을 필요 없으므로 executeReturnKey아니고 그냥 execute
    }

    // 페이징 : 글 갯수 조회
    @Transactional(readOnly = true)
    public int getTotalCount() {
        String sql = "select count(*) as total_count from board"; // 1건 데이터 -> queryForObject
        Integer totalCount = jdbcTemplate.queryForObject(sql, Map.of(), Integer.class);
        return totalCount.intValue();
    }

    // 글 목록 조회
    @Transactional(readOnly = true)
    public List<Board> getBoards(int page) {
        // start = 0, 10, 20, 30 는 1page, 2page, 3page, 4page
        int start = (page - 1) * 10;
        String sql = "select b.user_id, b.board_id, b.title, b.regdate, b.view_cnt, u.name from board b, user u where b.user_id = u.user_id order by board_id desc limit :start, 10";
        RowMapper<Board> rowMapper = BeanPropertyRowMapper.newInstance(Board.class);
        List<Board> list = jdbcTemplate.query(sql, Map.of("start", start), rowMapper);
        return list;
    }

    // 글 상세보기
    @Transactional(readOnly = true)
    public Board getBoard(int boardId) {
        // 1건 또는 0건 -> queryForObject
        String sql = "select b.user_id, b.board_id, b.title, b.regdate, b.view_cnt, u.name, b.content from board b, user u where b.user_id = u.user_id and b.board_id=:boardId";
        RowMapper<Board> rowMapper = BeanPropertyRowMapper.newInstance(Board.class);
        Board board = jdbcTemplate.queryForObject(sql, Map.of("boardId", boardId), rowMapper);
        return board;
    }

    // 글 상세보기 - 조회수 증가
    @Transactional
    public void updateViewCnt(int boardId) {
        String sql = "update board set view_cnt = view_cnt + 1 where board_id=:boardId";
        jdbcTemplate.update(sql, Map.of("boardId", boardId));
    }

    // 글 삭제
    @Transactional
    public void deleteBoard(int boardId) {
        String sql = "delete from board where board_id = :boardId";
        jdbcTemplate.update(sql, Map.of("boardId", boardId));
    }

    @Transactional
    public void updateBoard(int boardId, String title, String content) {
        String sql = "update board\n" +
                "set title=:title, content=:content\n" +
                "where board_id = :boardId";
        Board board = new Board();
        board.setBoardId(boardId);
        board.setTitle(title);
        board.setContent(content);
        SqlParameterSource params = new BeanPropertySqlParameterSource(board);
        jdbcTemplate.update(sql, params);

    }
}
