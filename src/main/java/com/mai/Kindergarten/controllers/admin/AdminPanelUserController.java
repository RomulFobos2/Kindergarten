package com.mai.Kindergarten.controllers.admin;

import com.mai.Kindergarten.models.User;
import com.mai.Kindergarten.repo.UserRepository;
import com.mai.Kindergarten.service.OTPGenerator;
import com.mai.Kindergarten.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

//Класс для обработки страниц администратора для работы с пользователями(воспитателями)
@Controller
public class AdminPanelUserController {
    @Autowired
    UserRepository userRepository;
    @Autowired
    private UserService userService;

    @GetMapping("/admin/allUsers")
    public String userList(Model model) {
        //Получаем пользователя, под которым выполнен вход (страница доступна только админу, соответсвенно пользователь будет только с ролью админа.
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        //Получаем списко всех пользователей, для отображения на странице
        List<User> var_list = userService.allUsers();
        //Удаляем текущего пользователя из списка для отображения, чтобы он не мог удалить сам себя
        var_list.remove(userRepository.findByUsername(auth.getName()));
        //Оставляем только пользователей у которых имеется роль User.
        Iterable<User> users = var_list.stream().filter(x -> x.getRoles().stream().anyMatch(y -> y.getName().equals("ROLE_USER"))).collect(Collectors.toList());
        model.addAttribute("allUsers", users);
        return "admin/allUsers";
    }

    //Страниц для формы для добавления зам. по ВМР
    @GetMapping("/admin/addUser")
    public String addUser(Model model) {
        return "admin/addUser";
    }

    //Сохраняем менеджера
    @PostMapping("/admin/addUser")
    public String addUser(@RequestParam String userName, @RequestParam String inputLastName, @RequestParam String inputFirstName,
                             @RequestParam String inputPatronymicName, Model model) {
        if(userService.chekUserName(userName)){
            model.addAttribute("userNameError", "Пользователь с таким именем уже существует");
            return "admin/addUser";
        }
        String oneTimePassword = OTPGenerator.getOneTimePassword();
        User user = new User(userName, oneTimePassword, oneTimePassword, inputLastName, inputFirstName, inputPatronymicName);
        userService.sendOneTimePasswordMail(userName, oneTimePassword, user);
        userService.saveUser(user);
        return "redirect:/admin/allUsers";
    }

    //Формируем динамически страницу для каждого пользователя. Внутри страницы можно сделать операции над пользователем
    @GetMapping("/admin/allUsers/user-details/{id}")
    public String userDetails(@PathVariable(value = "id") long id, Model model) {
        if (!userRepository.existsById(id)) {
            return "redirect:/admin/allUsers";
        }
        Optional<User> user = userRepository.findById(id);
        ArrayList<User> res = new ArrayList<>();
        user.ifPresent(res::add);
        model.addAttribute("user", res);
        return "admin/user-details";
    }

    //Удаляем пользователя
    @PostMapping("/admin/allUsers/user-details/{id}/remove")
    public String delete_user(@PathVariable(value = "id") long id,Model model) {
        User user = userRepository.findById(id).orElseThrow();
        userRepository.delete(user);
        return "redirect:/admin/allUsers";
    }

}
