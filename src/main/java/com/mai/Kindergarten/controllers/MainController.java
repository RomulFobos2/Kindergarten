package com.mai.Kindergarten.controllers;

import com.mai.Kindergarten.models.Role;
import com.mai.Kindergarten.models.User;
import com.mai.Kindergarten.repo.AgeGroupRepository;
import com.mai.Kindergarten.repo.GroupRepository;
import com.mai.Kindergarten.repo.RoleRepository;
import com.mai.Kindergarten.repo.UserRepository;
import com.mai.Kindergarten.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


/**
 * Контроллер для отображения страниц доступных без авторизации и общих страниц
 */
@Controller
public class MainController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    UserService userService;
    @Autowired
    GroupRepository groupRepository;
    @Autowired
    AgeGroupRepository ageGroupRepository;

    @GetMapping("/")
    public String home(Model model) {
        User currentUser = userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        if(currentUser != null && currentUser.isNeedChangePass()){
            return "redirect:/change-password";
        }
        model.addAttribute("title", "Главная страница");
        return "general/home";
    }

    @GetMapping("/login")
    public String login(Model model) {
        initialSetting();
        model.addAttribute("title", "Вход");
        return "general/login";
    }

    @GetMapping("/profile")
    public String profile(Model model) {
        //На страницу загружаем объект "Пользователь" который эту страницу посещает
        User currentUser = userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        String roleName = currentUser.getRoles().stream().toList().get(0).getName();
        switch (roleName){
            case "ROLE_MANAGER" : roleName = "Зам. по УВР"; break;
            case "ROLE_USER" : roleName = "Воспитатель"; break;
            default: roleName = "Техническая учетная запись";
        }
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("roleName", roleName);
        return "general/profile";
    }

    @GetMapping("/admin")
    public String admin(Model model) {
        return "admin/admin";
    }

    @GetMapping("/manager")
    public String manager(Model model) {
        return "manager/manager";
    }

    @GetMapping("/user")
    public String user(Model model) {
        User currentUser = userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        model.addAttribute("currentUser", currentUser);
        return "user/user";
    }

    @GetMapping("/change-password")
    public String changePassword(Model model) {
        User currentUser = userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        model.addAttribute("currentUser", currentUser);
        return "general/change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam String passwordNew, @RequestParam String passwordConfirm, Model model) {
        //Получаем текущего пользователя, который инициировал смену пароля
        User currentUser = userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        //С помощью функции matches определяем, что пользователь ввел свой текущий пароль верно
        if (passwordNew.equals(passwordConfirm)) {
            currentUser.setPassword(bCryptPasswordEncoder.encode(passwordNew));
            currentUser.setNeedChangePass(false);
            userRepository.save(currentUser);
        }
        else {
            model.addAttribute("passwordError", "Пароли не совпадают. Потдвердите новый пароль верно");
            return "change-password";
        }
        return "redirect:/logout";
    }

    //Первоначальная настройка приложения. Создание ролей, админа.
    public void initialSetting(){
        if(roleRepository.count() == 0){
            Role roleUser = new Role(1L, "ROLE_USER");
            Role roleAdmin = new Role(2L, "ROLE_ADMIN");
            Role roleManager = new Role(3L, "ROLE_MANAGER");
            roleRepository.save(roleUser);
            roleRepository.save(roleAdmin);
            roleRepository.save(roleManager);
        }
        User user = userRepository.findByUsername("admin");
        if (user == null){
            User userAdmin = new User("admin", "admin", "admin", "Администратор", "", "");
            userService.saveAdmin(userAdmin);
        }
    }









}