package com.mai.Kindergarten.controllers.manager;

import com.mai.Kindergarten.models.*;
import com.mai.Kindergarten.repo.EducationAreaRepository;
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

//Класс для обработки страниц менеджера для работы с группами
@Controller
public class ManagerPanelEducationAreaController {
    @Autowired
    EducationAreaRepository educationAreaRepository;

    @GetMapping("/manager/allEducationAreas")
    public String educationAreaList(Model model, HttpServletRequest request) {
        if (request.getSession().getAttribute("deleteError") != null && (Boolean) request.getSession().getAttribute("deleteError")) {
            model.addAttribute("deleteError", true);
            request.getSession().setAttribute("deleteError", false);
        }
        List<EducationArea> allEducationAreas = educationAreaRepository.findAll();
        model.addAttribute("allEducationAreas", allEducationAreas);
        return "manager/educationArea/allEducationAreas";
    }

    @GetMapping("/manager/addEducationArea")
    public String addEducationArea(Model model, HttpServletRequest request) {
        if (request.getSession().getAttribute("educationAreaNameError") != null && (Boolean) request.getSession().getAttribute("educationAreaNameError")) {
            model.addAttribute("educationAreaNameError", "Образовательная область с таким именем уже существует");
            request.getSession().setAttribute("educationAreaNameError", false);
        }
        return "manager/educationArea/addEducationArea";
    }


    @PostMapping("/manager/addEducationArea")
    public String addEducationArea(@RequestParam String inputEducationAreaName,
                                   Model model, HttpServletRequest request) {
        if (educationAreaRepository.findByName(inputEducationAreaName) != null) {
            request.getSession().setAttribute("educationAreaNameError", true);
            return "redirect:/manager/addEducationArea";
        }
        EducationArea educationArea = new EducationArea(inputEducationAreaName);
        educationAreaRepository.save(educationArea);
        return "redirect:/manager/allEducationAreas";
    }

    //Формируем динамически страницу для каждого пользователя. Внутри страницы можно сделать операции над пользователем
    @GetMapping("/manager/allEducationAreas/educationArea-details/{id}")
    public String educationAreaDetails(@PathVariable(value = "id") long id, Model model) {
        if (!educationAreaRepository.existsById(id)) {
            return "redirect:/manager/allEducationAreas";
        }
        Optional<EducationArea> educationArea = educationAreaRepository.findById(id);
        ArrayList<EducationArea> res = new ArrayList<>();
        educationArea.ifPresent(res::add);
        model.addAttribute("educationArea", res);
        return "manager/educationArea/educationArea-details";
    }

    @GetMapping("/manager/editEducationArea/{id}")
    public String educationAreaEdit(@PathVariable(value = "id") long id,
                                    Model model, HttpServletRequest request) {
        if (!educationAreaRepository.existsById(id)) {
            return "redirect:/manager/allEducationAreas";
        }
        if (request.getSession().getAttribute("educationAreaNameError") != null && (Boolean) request.getSession().getAttribute("educationAreaNameError")) {
            model.addAttribute("educationAreaNameError", "Образовательная область с таким именем уже существует");
            request.getSession().setAttribute("educationAreaNameError", false);
        }
        EducationArea educationArea = educationAreaRepository.findById(id).orElseThrow();
        model.addAttribute("educationArea", educationArea);
        return "manager/educationArea/editEducationArea";
    }

    @PostMapping("/manager/editEducationArea/{id}")
    public String editEducationArea(@PathVariable(value = "id") long id, @RequestParam String inputEducationAreaName,
                                    Model model, HttpServletRequest request) {
        EducationArea educationArea = educationAreaRepository.findById(id).orElseThrow();
        if (educationAreaRepository.findByName(inputEducationAreaName) != null & !educationArea.getName().equals(inputEducationAreaName)) {
            request.getSession().setAttribute("educationAreaNameError", true);
            return "redirect:/manager/editEducationArea/{id}";
        }
        educationArea.setName(inputEducationAreaName);
        educationAreaRepository.save(educationArea);
        return "redirect:/manager/allEducationAreas";
    }

    @PostMapping("/manager/allEducationAreas/educationArea-details/{id}/remove")
    public String delete_educationArea(@PathVariable(value = "id") long id,
                               Model model, HttpServletRequest request) {
        EducationArea educationArea = educationAreaRepository.findById(id).orElseThrow();
        try {
            educationAreaRepository.delete(educationArea);
        } catch (Exception e) {
            request.getSession().setAttribute("deleteError", true);
        }
        return "redirect:/manager/allEducationAreas";
    }

}
