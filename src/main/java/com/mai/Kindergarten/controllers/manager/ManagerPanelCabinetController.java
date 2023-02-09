package com.mai.Kindergarten.controllers.manager;

import com.mai.Kindergarten.models.Cabinet;
import com.mai.Kindergarten.models.Group;
import com.mai.Kindergarten.repo.CabinetRepository;
import com.mai.Kindergarten.repo.GroupRepository;
import com.mai.Kindergarten.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

//Класс для обработки страниц менеджера для работы с кабинетами
@Controller
public class ManagerPanelCabinetController {
    @Autowired
    UserRepository userRepository;
    @Autowired
    private CabinetRepository cabinetRepository;
    @Autowired
    private GroupRepository groupRepository;

    @GetMapping("/manager/allCabinets")
    public String cabinetList(Model model, HttpServletRequest request) {
        if(request.getSession().getAttribute("deleteError") != null && (Boolean)request.getSession().getAttribute("deleteError")){
            model.addAttribute("deleteError", true);
            request.getSession().setAttribute("deleteError", false);
        }
        List<Cabinet> cabinets = cabinetRepository.findAll();
        model.addAttribute("allCabinets", cabinets);
        return "manager/cabinet/allCabinets";
    }

    @GetMapping("/manager/addCabinet")
    public String addCabinet(Model model) {
        return "manager/cabinet/addCabinet";
    }

    @PostMapping("/manager/addCabinet")
    public String addCabinet(@RequestParam String inputCabinetNumber, Model model) {
        Cabinet cabinet = cabinetRepository.findByNumber(inputCabinetNumber);
        if(cabinet != null){
            model.addAttribute("cabinetNameError", "Кабинет уже существует");
            return "manager/cabinet/addCabinet";
        }
        Cabinet newCabinet = new Cabinet(inputCabinetNumber);
        cabinetRepository.save(newCabinet);
        return "redirect:/manager/allCabinets";
    }

    @GetMapping("/manager/allCabinets/cabinet-details/{id}")
    public String cabinetDetails(@PathVariable(value = "id") long id, Model model) {
        if (!cabinetRepository.existsById(id)) {
            return "redirect:/manager/allCabinets";
        }
        Optional<Cabinet> cabinet = cabinetRepository.findById(id);
        ArrayList<Cabinet> res = new ArrayList<>();
        cabinet.ifPresent(res::add);
        model.addAttribute("cabinet", res);
        List<Group> allGroups = groupRepository.findAll();
        for(Group group : allGroups){
            if(group.getCabinet().getId() == id){
                model.addAttribute("group", group);
            }
        }
        return "manager/cabinet/cabinet-details";
    }

    //Удаляем кабинет
    @PostMapping("/manager/allCabinets/cabinet-details/{id}/remove")
    public String delete_cabinet(@PathVariable(value = "id") long id,
                                 Model model, HttpServletRequest request) {
        Cabinet cabinet = cabinetRepository.findById(id).orElseThrow();
        try {
            cabinetRepository.delete(cabinet);
        }
        catch (Exception e){
            request.getSession().setAttribute("deleteError", true);
        }
        return "redirect:/manager/allCabinets";
    }

}
