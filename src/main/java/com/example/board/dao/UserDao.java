package com.example.board.dao;

import com.example.board.dto.User;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
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

// Spring이 관리하는 Bean. Spring JDBC를 이용한 코드
@Repository
public class UserDao {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private final SimpleJdbcInsertOperations insertUser;

    //hikari가 JDBC 정보 넣어준다 위에서 생성자주입하면서
    public UserDao(DataSource dataSource){
        jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        insertUser = new SimpleJdbcInsert(dataSource)
                .withTableName("user")
                .usingGeneratedKeyColumns("user_id"); // 자동으로 증가되는 id 설정
    }

    // 회원가입 : insert
    @Transactional
    public User addUser(String email, String name, String password){
        //insert into user (email, name, password, regdate) values (:email, :name, :password, :regdate) (user_id auto gen)
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(password);
        user.setRegdate(LocalDateTime.now());
        SqlParameterSource params = new BeanPropertySqlParameterSource(user); // dto 넣어준다
        Number number = insertUser.executeAndReturnKey(params); // insert를 실행하고 자동생성한 id를 가져온다.
        //last_insert_id (권한부여를 위해서)
        int userId = number.intValue();
        user.setUserId(userId);
        return user;
    }

    //회원가입 : user role 추가
    @Transactional
    public void mappingUserRole(int userId){
        // inset into user_role(user_id, role_id) values (?, 1)
        String sql = "insert into user_role (user_id, role_id) values (:userId, 1)";
        SqlParameterSource params = new MapSqlParameterSource("userId", userId); // 1 : ROLE_USER
        jdbcTemplate.update(sql, params);
    }

    // 로그인
    @Transactional
    public User getUser(String email) {
        try {
            String sql = "select user_id, email, name, password, regdate from user where email = :email";
            SqlParameterSource params = new MapSqlParameterSource("email", email);
            //row mapper가 dto와 동일 규칙을 갖고 있다면 BeanPropertyRowMapper 이용해 매핑 => user_id -> setUserId..
            RowMapper<User> rowMapper = BeanPropertyRowMapper.newInstance(User.class);
            User user = jdbcTemplate.queryForObject(sql, params, rowMapper);
            return user;
        }catch (Exception ex){
            return null;
        }
    }

    @Transactional(readOnly = true)
    public List<String> getRoles(int userId) {
        String sql = "select r.name from user_role ur, role r where ur.role_id = r.role_id and ur.user_id = :userId";
        List<String> roles = jdbcTemplate.query(sql, Map.of("userId", userId), (rs, rowNum) -> {
            return rs.getString(1);
        });
        return roles;
    }
}
