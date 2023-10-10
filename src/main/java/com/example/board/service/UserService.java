package com.example.board.service;

import com.example.board.dao.UserDao;
import com.example.board.dto.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// 트랜잭션 단위로 실행될 메소드를 선언하고 있는 클래스
// 스프링이 관리하는 Bean
@Service
@RequiredArgsConstructor // 생성자 주입 : Spring이 UserService를 Bean으로 생성할 때 생성자를 이용해 생성하는데, 이때 UserDao Bean이 있는지 보고 그 빈을 주입한다. 생성자 주입 public UserService(UserDao userDao){this.userDao = userDao;}
public class UserService {
    private final UserDao userDao;
    // 보통 서비스에서는 @Transactional을 붙여서 하나의 트랜잭션으로 처리하게 한다. 스프링부트는 트랜잭션을 처리해주는 트랜잭션 관리자를 가지고 있다.

    //회원가입
    @Transactional
    public User addUser(String name, String email, String password){
        // 이메일 중복 검사
        User user1 = userDao.getUser(email);
        if (user1 != null) {
            throw new RuntimeException("이미 가입된 이메일입니다.");
        }
        User user = userDao.addUser(email, name, password); // insert 후 자동생성한 id 가져오기
        userDao.mappingUserRole(user.getUserId()); // 권한을 부여한다.
        return user;
    }

    // 로그인
    @Transactional
    public User getUser(String email){
        return userDao.getUser(email);
    }

    @Transactional(readOnly = true)
    public List<String> getRoles(int userId) {
        return userDao.getRoles(userId);
    }
}
