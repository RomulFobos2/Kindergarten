package com.mai.Kindergarten.controllers.manager;

import com.mai.Kindergarten.models.Diploma;
import com.mai.Kindergarten.models.Group;
import com.mai.Kindergarten.models.User;
import com.mai.Kindergarten.repo.DiplomaRepository;
import com.mai.Kindergarten.repo.GroupRepository;
import com.mai.Kindergarten.repo.UserRepository;
import com.mai.Kindergarten.service.OTPGenerator;
import com.mai.Kindergarten.service.Transcriptor;
import com.mai.Kindergarten.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

//Класс для обработки страниц менеджера для работы с пользователями(воспитателями)
@Controller
public class ManagerPanelUserController {
    @Autowired
    UserRepository userRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private DiplomaRepository diplomaRepository;

    @GetMapping("/manager/allUsers")
    public String userList(Model model) {
        //Получаем пользователя, под которым выполнен вход (страница доступна только админу, соответсвенно пользователь будет только с ролью админа.
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        //Получаем список всех пользователей, для отображения на странице
        List<User> var_list = userService.allUsers();
        //Удаляем текущего пользователя из списка для отображения, чтобы он не мог удалить сам себя
        var_list.remove(userRepository.findByUsername(auth.getName()));
        //Оставляем только пользователей у которых имеется роль User.
        Iterable<User> users = var_list.stream().filter(x -> x.getRoles().stream().anyMatch(y -> y.getName().equals("ROLE_USER"))).collect(Collectors.toList());
        model.addAttribute("allUsers", users);
        return "manager/user/allUsers";
    }

    //Страниц для формы для добавления воспитателя
    @GetMapping("/manager/addUser")
    public String addUser(Model model) {
        return "manager/user/addUser";
    }

    //Сохраняем воспитателя
    @PostMapping("/manager/addUser")
    public String addUser(@RequestParam String userName, @RequestParam String inputLastName, @RequestParam String inputFirstName,
                             @RequestParam String inputPatronymicName, Model model) {
        if(userService.chekUserName(userName)){
            model.addAttribute("userNameError", "Пользователь с таким именем уже существует");
            return "manager/user/addUser";
        }
        String oneTimePassword = OTPGenerator.getOneTimePassword();
        User user = new User(userName, oneTimePassword, oneTimePassword, inputLastName, inputFirstName, inputPatronymicName);
        userService.sendOneTimePasswordMail(userName, oneTimePassword, user);
        userService.saveUser(user);
        return "redirect:/manager/allUsers";
    }

    //Формируем динамически страницу для каждого пользователя. Внутри страницы можно сделать операции над пользователем
    @GetMapping("/manager/allUsers/user-details/{id}")
    public String userDetails(@PathVariable(value = "id") long id, Model model) {
        if (!userRepository.existsById(id)) {
            return "redirect:/manager/allUsers";
        }
        Optional<User> user = userRepository.findById(id);
        ArrayList<User> res = new ArrayList<>();
        user.ifPresent(res::add);
        model.addAttribute("user", res);
        return "manager/user/user-details";
    }

    //Удаляем пользователя
    @PostMapping("/manager/allUsers/user-details/{id}/remove")
    public String delete_user(@PathVariable(value = "id") long id,Model model) {
        User user = userRepository.findById(id).orElseThrow();
        userRepository.delete(user);
        return "redirect:/manager/allUsers";
    }

    @GetMapping("/manager/editUser/{id}")
    public String userEdit(@PathVariable(value = "id") long id, Model model) {
        if (!userRepository.existsById(id) || !userService.haveRole(id, "ROLE_USER")) {
            return "redirect:/manager/allUsers";
        }
        User user = userRepository.findById(id).orElseThrow();
        List<Group> allGroups;
        if (user.getGroup() != null){
            allGroups = groupRepository.findAll().stream().filter(x -> x.getId() != user.getGroup().getId()).filter(x -> x.getUsers().size() < 2).collect(Collectors.toList());
        }
        else {
            allGroups = groupRepository.findAll().stream().filter(x -> x.getUsers().size() < 2).collect(Collectors.toList());
        }
        model.addAttribute("user", user);
        model.addAttribute("allGroups", allGroups);
        return "manager/user/editUser";
    }

    @PostMapping("/manager/editUser/{id}")
    public String editUser(@PathVariable(value = "id") long id, @RequestParam String inputLastName, @RequestParam String inputFirstName,
                              @RequestParam String inputPatronymicName, @RequestParam Long inputGroup, Model model) {
        User user = userRepository.findById(id).orElseThrow();
        user.setLastName(inputLastName);
        user.setFirstName(inputFirstName);
        user.setPatronymicName(inputPatronymicName);
        if(inputGroup != -1){
            user.setGroup(groupRepository.findById(inputGroup).orElseThrow());
        }
        userRepository.save(user);
        return "redirect:/manager/allUsers/user-details/{id}";
    }

    @GetMapping("/manager/allUsers/user-details/{id}/allDiplomas")
    public String diplomasList(@PathVariable(value = "id") long id, Model model) {
        if (!userRepository.existsById(id)) {
            return "redirect:/manager/allUsers";
        }
        User currentUser = userRepository.findById(id).orElseThrow();
        Iterable<Diploma> allDiplomas = diplomaRepository.findAll().stream().filter(x -> x.getUser().getId().equals(currentUser.getId())).collect(Collectors.toList());
        model.addAttribute("allDiplomas", allDiplomas);
        model.addAttribute("user", currentUser);
        return "manager/user/allDiplomas";
    }

    @GetMapping("/manager/allUsers/user-details/{id_user}/allDiplomas/diploma-details/{id_diploma}")
    public String diplomaDetails(@PathVariable(value = "id_user") long id_user,
                                 @PathVariable(value = "id_diploma") long id_diploma,
                                 Model model) throws UnsupportedEncodingException {
        if (!userRepository.existsById(id_user)) {
            return "redirect:/manager/allUsers";
        }
        if (!diplomaRepository.existsById(id_diploma)) {
            return "redirect:/manager/allUsers/user-details/{id_user}/allDiplomas";
        }
        User currentUser = userRepository.findById(id_user).orElseThrow();
        Diploma diploma = diplomaRepository.findById(id_diploma).orElseThrow();
        model.addAttribute("diploma", diploma);
        //Добавляем картинку в виде объекта на страницу
        byte[] bytes = diplomaRepository.findById(id_diploma).orElseThrow().getFile();
        byte[] encodeBase64 = Base64.encode(bytes);
        String base64Encoded = new String(encodeBase64, "UTF-8");
        model.addAttribute("image", base64Encoded);
        model.addAttribute("user", currentUser);
        return "manager/user/diploma-details";
    }

    //Скачивание диплома
    @GetMapping("manager/allUsers/user-details/{id_user}/allDiplomas/diploma-details/{id_diploma}/download")
    public ResponseEntity<Object> downloadFile(@PathVariable(value = "id_diploma") Long id_diploma, Model model) throws Exception {
        Diploma diploma = diplomaRepository.findById(id_diploma).orElseThrow();
        InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(diploma.getFile()));
        HttpHeaders headers = new HttpHeaders();
        String nameFileDownload = Transcriptor.transliterate(diploma.getFileName());
        headers.add("Content-Disposition", String.format("attachment; filename=\"%s\"", nameFileDownload));
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");
        ResponseEntity<Object>
                responseEntity = ResponseEntity.ok().headers(headers).contentLength(diploma.getFile().length).contentType(
                MediaType.parseMediaType("application/txt")).body(resource);
        return responseEntity;
    }

}
