package com.mai.Kindergarten.controllers.manager;

import com.mai.Kindergarten.models.AgeGroup;
import com.mai.Kindergarten.repo.AgeGroupRepository;
import com.mai.Kindergarten.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

//Класс для обработки страниц менеджера для работы с возрастными группами
@Controller
public class ManagerPanelAgeGroupController {
    @Autowired
    UserRepository userRepository;
    @Autowired
    private AgeGroupRepository ageGroupRepository;

    @GetMapping("/manager/allAgeGroups")
    public String ageGroupList(Model model, HttpServletRequest request) {
        if (request.getSession().getAttribute("deleteError") != null && (Boolean) request.getSession().getAttribute("deleteError")) {
            model.addAttribute("deleteError", true);
            request.getSession().setAttribute("deleteError", false);
        }
        List<AgeGroup> ageGroups = ageGroupRepository.findAll();
        model.addAttribute("allAgeGroups", ageGroups);
        return "manager/ageGroup/allAgeGroups";
    }

    @GetMapping("/manager/addAgeGroup")
    public String addAgeGroup(Model model) {
        return "manager/ageGroup/addAgeGroup";
    }

    @PostMapping("/manager/addAgeGroup")
    public String addAgeGroup(@RequestParam String inputAgeGroupName,
                              @RequestParam String inputAgeGroup,
                              @RequestParam int inputDurationLesson, @RequestParam int inputMaxDurationLessons,
                              Model model) {
        AgeGroup ageGroup = ageGroupRepository.findByName(inputAgeGroupName);
        if (ageGroup != null) {
            model.addAttribute("ageGroupNameError", "Возрастная группа c таким именем уже существует");
            return "manager/ageGroup/addAgeGroup";
        }
        int valueInputAgeGroup = Integer.parseInt(inputAgeGroup);
        Long childrenMaxАge = 0l;
        Long childrenMinАge = 0l;
        switch (valueInputAgeGroup) {
            case 0:
                childrenMinАge = 1l;
                childrenMaxАge = 2l;
                break;
            case 1:
                childrenMinАge = 2l;
                childrenMaxАge = 3l;
                break;
            case 2:
                childrenMinАge = 3l;
                childrenMaxАge = 4l;
                break;
            case 3:
                childrenMinАge = 4l;
                childrenMaxАge = 5l;
                break;
            case 4:
                childrenMinАge = 5l;
                childrenMaxАge = 6l;
                break;
        }
        AgeGroup newAgeGroup = new AgeGroup(inputAgeGroupName, childrenMinАge.toString(), childrenMaxАge.toString(), inputDurationLesson, inputMaxDurationLessons);
        ageGroupRepository.save(newAgeGroup);
        return "redirect:/manager/allAgeGroups";
    }

    @GetMapping("/manager/allAgeGroups/ageGroup-details/{id}")
    public String ageGroupDetails(@PathVariable(value = "id") long id, Model model) {
        if (!ageGroupRepository.existsById(id)) {
            return "redirect:/manager/allAgeGroups";
        }
        Optional<AgeGroup> ageGroup = ageGroupRepository.findById(id);
        ArrayList<AgeGroup> res = new ArrayList<>();
        ageGroup.ifPresent(res::add);
        model.addAttribute("ageGroup", res);
        return "manager/ageGroup/ageGroup-details";
    }

    @PostMapping("/manager/allAgeGroups/ageGroup-details/{id}/remove")
    public String delete_ageGroup(@PathVariable(value = "id") long id,
                                  Model model, HttpServletRequest request) {
        AgeGroup ageGroup = ageGroupRepository.findById(id).orElseThrow();
        try {
            ageGroupRepository.delete(ageGroup);
        } catch (Exception e) {
            request.getSession().setAttribute("deleteError", true);
        }
        return "redirect:/manager/allAgeGroups";
    }

    @GetMapping("/manager/editAgeGroup/{id}")
    public String ageGroupEdit(@PathVariable(value = "id") long id, Model model) {
        if (!ageGroupRepository.existsById(id)) {
            return "redirect:/manual/allAgeGroups";
        }
        AgeGroup ageGroup = ageGroupRepository.findById(id).orElseThrow();
        model.addAttribute("ageGroup", ageGroup);
        return "manager/ageGroup/editAgeGroup";
    }

    @PostMapping("/manager/editAgeGroup/{id}")
    public String ageGroupEdit(@PathVariable(value = "id") long id,
//                               @RequestParam String inputAgeGroupName,
//                               @RequestParam String inputAgeGroup,
                               @RequestParam int inputDurationLesson, @RequestParam int inputMaxDurationLessons,
                               Model model) throws IOException {
        AgeGroup ageGroup = ageGroupRepository.findById(id).orElseThrow();
        ageGroup.setDurationLesson(inputDurationLesson);
        ageGroup.setMaxDurationLessons(inputMaxDurationLessons);
        ageGroupRepository.save(ageGroup);
        return "redirect:/manager/allAgeGroups/ageGroup-details/{id}";
    }

}
