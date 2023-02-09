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

//Класс для обработки страниц администратора для работы с менеджером(методистом)
@Controller
public class AdminPanelManagerController {
    @Autowired
    UserRepository userRepository;
    @Autowired
    private UserService userService;

    @GetMapping("/admin/allManagers")
    public String managerList(Model model) {
        //Получаем пользователя, под которым выполнен вход (страница доступна только админу, соответсвенно пользователь будет только с ролью админа.
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        //Получаем списко всех пользователей, для отображения на странице
        List<User> var_list = userService.allUsers();
        //Удаляем текущего пользователя из списка для отображения, чтобы он не мог удалить сам себя
        var_list.remove(userRepository.findByUsername(auth.getName()));
        //Оставляем только пользователей у которых имеется роль Manager.
        Iterable<User> managers = var_list.stream().filter(x -> x.getRoles().stream().anyMatch(y -> y.getName().equals("ROLE_MANAGER"))).collect(Collectors.toList());
        model.addAttribute("allManagers", managers);
        return "admin/allManagers";
    }

    //Страниц для формы для добавления зам. по УВР
    @GetMapping("/admin/addManager")
    public String addManager(Model model) {
        return "admin/addManager";
    }

    //Сохраняем менеджера
    @PostMapping("/admin/addManager")
    public String addManager(@RequestParam String managerName, @RequestParam String inputLastName, @RequestParam String inputFirstName,
                             @RequestParam String inputPatronymicName, Model model) {
        if(userService.chekUserName(managerName)){
            model.addAttribute("managerNameError", "Пользователь с таким именем уже существует");
            return "admin/addManager";
        }
        String oneTimePassword = OTPGenerator.getOneTimePassword();
        User manager = new User(managerName, oneTimePassword, oneTimePassword, inputLastName, inputFirstName, inputPatronymicName);
        userService.sendOneTimePasswordMail(managerName, oneTimePassword, manager);
        userService.saveManager(manager);
        return "redirect:/admin/allManagers";
    }

    //Формируем динамически страницу для каждого пользователя. Внутри страницы можно сделать операции над пользователем
    @GetMapping("/admin/allManagers/manager-details/{id}")
    public String managerDetails(@PathVariable(value = "id") long id, Model model) {
        if (!userRepository.existsById(id)) {
            return "redirect:/admin/allManagers";
        }
        Optional<User> manager = userRepository.findById(id);
        ArrayList<User> res = new ArrayList<>();
        manager.ifPresent(res::add);
        model.addAttribute("manager", res);
        return "admin/manager-details";
    }

    //Удаляем пользователя
    @PostMapping("/admin/allManagers/manager-details/{id}/remove")
    public String delete_manager(@PathVariable(value = "id") long id,Model model) {
        User manager = userRepository.findById(id).orElseThrow();
        userRepository.delete(manager);
        return "redirect:/admin/allManagers";
    }

}
