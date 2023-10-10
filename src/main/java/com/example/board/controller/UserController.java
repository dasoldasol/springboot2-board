package com.example.board.controller;

import com.example.board.dto.LoginInfo;
import com.example.board.dto.User;
import com.example.board.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class UserController{

    private final UserService userService;

    @GetMapping("/userRegForm")
    public String userRegForm(){
        return "userRegForm";
    }

    @PostMapping("/userRegForm")
    public String userReg(
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam("password") String password
    ){
        // user 정보 저장한다.
        userService.addUser(name, email, password);

        return "redirect:/welcome"; // 브라우저에게 자동으로 http://localhost:8080/welcome 으로 GET 요청
    }

    @GetMapping("/welcome")
    public String welcome(){
        return "welcome";
    }

    @GetMapping("/loginform")
    public String loginform(){
        return "loginform";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            HttpSession httpSession // Spring이 자동으로 session 처리하는 HttpSession 객체를 넣어준다.
    ){
        // email에 해당하는 회원정보 불러온 후
        try{
            User user = userService.getUser(email);
            if(user.getPassword().equals(password)){
                // 아이디 암호 맞다면 세션에 회원정보 저장
                LoginInfo loginInfo = new LoginInfo(user.getUserId(), user.getEmail(), user.getName());
                // 권한 정보 읽어와서 loginInfo에 추가한다.
                List<String> roles = userService.getRoles(user.getUserId());
                loginInfo.setRoles(roles);
                httpSession.setAttribute("loginInfo", loginInfo); // 1파라미터 키, 2파라미터 값
            }else{
                throw new RuntimeException("암호가 일치하지 않음");
            }
        }catch(Exception ex){
            return "redirect:/loginform?error=true";
        }
        return "redirect:/";
    }

    @GetMapping("/logout")
    public String logout(HttpSession httpSession){
        // 세션에서 회원정보 삭제한다
        httpSession.removeAttribute("loginInfo");
        return "redirect:/";
    }


}
