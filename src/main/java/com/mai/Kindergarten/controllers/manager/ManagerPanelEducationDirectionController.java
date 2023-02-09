package com.mai.Kindergarten.controllers.manager;

import com.mai.Kindergarten.models.AgeGroup;
import com.mai.Kindergarten.models.EducationArea;
import com.mai.Kindergarten.models.EducationDirection;
import com.mai.Kindergarten.models.EducationIndicator;
import com.mai.Kindergarten.repo.AgeGroupRepository;
import com.mai.Kindergarten.repo.EducationAreaRepository;
import com.mai.Kindergarten.repo.EducationDirectionRepository;
import com.mai.Kindergarten.repo.EducationIndicatorRepository;
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
import java.util.stream.Collectors;

//Класс для обработки страниц менеджера для работы с направлениями развития
@Controller
public class ManagerPanelEducationDirectionController {
    @Autowired
    EducationAreaRepository educationAreaRepository;
    @Autowired
    EducationIndicatorRepository educationIndicatorRepository;
    @Autowired
    EducationDirectionRepository educationDirectionRepository;
    @Autowired
    AgeGroupRepository ageGroupRepository;

    @GetMapping("/manager/mainAllEducationDirections")
    public String ageGroupList(Model model, HttpServletRequest request) {
        if (request.getSession().getAttribute("deleteError") != null && (Boolean) request.getSession().getAttribute("deleteError")) {
            model.addAttribute("deleteError", true);
            request.getSession().setAttribute("deleteError", false);
        }
        List<AgeGroup> allAgeGroups = ageGroupRepository.findAll();
        model.addAttribute("allAgeGroups", allAgeGroups);
        return "manager/educationDirection/mainAllEducationDirections";
    }

    @GetMapping("/manager/allEducationDirections/{ageGroup_id}")
    public String educationDirectionList(@PathVariable(value = "ageGroup_id") Long ageGroup_id,
                                         Model model, HttpServletRequest request) {
//        if (request.getSession().getAttribute("deleteError") != null && (Boolean) request.getSession().getAttribute("deleteError")) {
//            model.addAttribute("deleteError", true);
//            request.getSession().setAttribute("deleteError", false);
//        }
        List<EducationDirection> allEducationDirections = educationDirectionRepository.findAll().stream().filter(x -> x.getAgeGroup().getId() == ageGroup_id).collect(Collectors.toList());
        model.addAttribute("allEducationDirections", allEducationDirections);
        return "manager/educationDirection/allEducationDirections";
    }

    @GetMapping("/manager/addEducationDirection")
    public String addEducationDirection(Model model, HttpServletRequest request) {
        if (request.getSession().getAttribute("educationDirectionNameError") != null && (Boolean) request.getSession().getAttribute("educationDirectionNameError")) {
            model.addAttribute("educationDirectionNameError", "Не уникальная запись. Данные параметры уже имеются");
            request.getSession().setAttribute("educationAreaNameError", false);
        }
        List<EducationArea> allEducationAreas = educationAreaRepository.findAll();
        List<AgeGroup> allAgeGroups = ageGroupRepository.findAll();
        model.addAttribute("allEducationAreas", allEducationAreas);
        model.addAttribute("allAgeGroups", allAgeGroups);

        List<EducationDirection> allEducationDirections = educationDirectionRepository.findAll();
        model.addAttribute("allEducationDirections", allEducationDirections);
        return "manager/educationDirection/addEducationDirection";
    }


    @PostMapping("/manager/addEducationDirection")
    public String addEducationDirection(@RequestParam String inputEducationDirectionName,
                                        @RequestParam Long inputEducationArea,
                                        @RequestParam Long inputAgeGroup,
                                        @RequestParam("inputEducationIndicator") List<String> inputEducationIndicatorQuestion,
                                        @RequestParam(required = false) String inputHaveLesson,
                                        @RequestParam(required = false) String inputIsHard,
                                        @RequestParam(required = false) String inputIsActivity,
                                        @RequestParam(required = false) Integer inputCountLesson,
                                        @RequestParam(required = false) Long inputTotalCountLesson,
                                        Model model, HttpServletRequest request) {
        EducationDirection educationDirectionTotalCountLesson = null;
        boolean haveLesson = false;
        boolean isHard = false;
        boolean isActivity = false;
        int countLesson = 0;
        if (inputHaveLesson != null) {
            haveLesson = true;
            if (inputIsHard != null) {
                isHard = true;
            }
            if (inputIsActivity != null) {
                isActivity = true;
            }
            if (inputTotalCountLesson != null) {
                educationDirectionTotalCountLesson = educationDirectionRepository.findById(inputTotalCountLesson).orElseThrow();
            }
            countLesson = inputCountLesson;
        }

        if (educationDirectionRepository.findDateByUniqueId(inputEducationDirectionName, inputAgeGroup, inputEducationArea) != null) {
            request.getSession().setAttribute("educationDirectionNameError", true);
            return "redirect:/manager/addEducationDirection";
        }
        List<EducationIndicator> educationIndicatorList = new ArrayList<>();
        for (String s : inputEducationIndicatorQuestion) {
            EducationIndicator educationIndicator = new EducationIndicator(s);
            educationIndicatorList.add(educationIndicator);
            educationIndicatorRepository.save(educationIndicator);
        }
        EducationArea educationArea = educationAreaRepository.findById(inputEducationArea).orElseThrow();
        AgeGroup ageGroup = ageGroupRepository.findById(inputAgeGroup).orElseThrow();
        EducationDirection educationDirection = new EducationDirection(inputEducationDirectionName, educationArea, educationIndicatorList, ageGroup, haveLesson, isHard, isActivity, countLesson, educationDirectionTotalCountLesson);
        //Если у обоих направлений развития, уроки проходят под одной темой и облим кол0вом уроков, то добаляем у них ссылки друг на друга
        //чтобы при формировании уроков, учеть это и для двух таких направлений сформировать один урок, а не два
        educationDirectionRepository.save(educationDirection);
        if (educationDirectionTotalCountLesson != null) {
            educationDirectionTotalCountLesson.setEducationDirectionTotalCountLesson(educationDirection);
            educationDirectionRepository.save(educationDirectionTotalCountLesson);
        }
        return "redirect:/manager/mainAllEducationDirections";
    }

    @GetMapping("/manager/allEducationDirections/educationDirection-details/{id}")
    public String educationDirectionDetails(@PathVariable(value = "id") long id, Model model) {
        if (!educationDirectionRepository.existsById(id)) {
            return "redirect:/manager/allEducationDirections";
        }
        Optional<EducationDirection> educationDirection = educationDirectionRepository.findById(id);
        ArrayList<EducationDirection> res = new ArrayList<>();
        educationDirection.ifPresent(res::add);
        model.addAttribute("educationDirection", res);
        return "manager/educationDirection/educationDirection-details";
    }

    @GetMapping("/manager/editEducationDirection/{id}")
    public String educationDirectionEdit(@PathVariable(value = "id") long id,
                                         Model model, HttpServletRequest request) {
        if (!educationDirectionRepository.existsById(id)) {
            return "redirect:/manager/allEducationDirections";
        }
        if (request.getSession().getAttribute("educationDirectionNameError") != null && (Boolean) request.getSession().getAttribute("educationDirectionNameError")) {
            model.addAttribute("educationDirectionNameError", "Данное направление развития уже существует");
            request.getSession().setAttribute("educationAreaNameError", false);
        }
        EducationDirection educationDirection = educationDirectionRepository.findById(id).orElseThrow();
        model.addAttribute("educationDirection", educationDirection);
        List<EducationArea> allEducationAreas = educationAreaRepository.findAll().stream().filter(x -> x.getId() != educationDirection.getEducationArea().getId()).collect(Collectors.toList());
        List<AgeGroup> allAgeGroups = ageGroupRepository.findAll().stream().filter(x -> x.getId() != educationDirection.getAgeGroup().getId()).collect(Collectors.toList());
        ;
        model.addAttribute("allEducationAreas", allEducationAreas);
        model.addAttribute("allAgeGroups", allAgeGroups);
        return "manager/educationDirection/editEducationDirection";
    }


    @PostMapping("/manager/editEducationDirection/{id}")
    public String editEducationDirection(@PathVariable(value = "id") long id,
                                         @RequestParam String inputEducationDirectionName,
                                         @RequestParam Long inputEducationArea,
                                         @RequestParam Long inputAgeGroup,
                                         @RequestParam("inputEducationIndicator") List<String> inputEducationIndicatorQuestion,
                                         Model model, HttpServletRequest request) {
        EducationDirection educationDirection = educationDirectionRepository.findById(id).orElseThrow();
        if (educationDirectionRepository.findDateByUniqueId(inputEducationDirectionName, inputAgeGroup, inputEducationArea) != null
                && educationDirectionRepository.findDateByUniqueId(inputEducationDirectionName, inputAgeGroup, inputEducationArea).getId() != id) {
            System.out.println("Надена похожая запись в БД по направлению развития - " + inputEducationDirectionName);
            System.out.println("ID похожей запись - " + educationDirectionRepository.findDateByUniqueId(inputEducationDirectionName, inputAgeGroup, inputEducationArea).getId());
            request.getSession().setAttribute("educationDirectionNameError", true);
            return "redirect:/manager/editEducationDirection/{id}";
        }
        //Полностью удаляем вопросы и загружаем их по новой
        for (EducationIndicator var_educationIndicator : educationDirection.getEducationIndicators()) {
            educationIndicatorRepository.delete(var_educationIndicator);
        }


        List<EducationIndicator> new_educationIndicatorList = new ArrayList<>();
        for (String s : inputEducationIndicatorQuestion) {
            EducationIndicator educationIndicator = new EducationIndicator(s);
            new_educationIndicatorList.add(educationIndicator);
            educationIndicatorRepository.save(educationIndicator);
        }
        EducationArea educationArea = educationAreaRepository.findById(inputEducationArea).orElseThrow();
        AgeGroup ageGroup = ageGroupRepository.findById(inputAgeGroup).orElseThrow();
        educationDirection.setEducationArea(educationArea);
        educationDirection.setAgeGroup(ageGroup);
        educationDirection.setEducationIndicators(new_educationIndicatorList);
        educationDirection.setName(inputEducationDirectionName);
        educationDirectionRepository.save(educationDirection);
        return "redirect:/manager/allEducationDirections/educationDirection-details/"+id;
    }

    @PostMapping("/manager/allEducationDirections/educationDirection-details/{id}/remove")
    public String delete_educationDirection(@PathVariable(value = "id") long id,
                                            Model model, HttpServletRequest request) {

        EducationDirection educationDirection = educationDirectionRepository.findById(id).orElseThrow();
        try {
            //Тут можно было бы использовать rollback
            for (EducationIndicator var_educationIndicator : educationDirection.getEducationIndicators()) {
                educationIndicatorRepository.delete(var_educationIndicator);
            }
            educationDirectionRepository.delete(educationDirection);
        } catch (Exception e) {
            request.getSession().setAttribute("deleteError", true);
        }
        return "redirect:/manager/mainAllEducationDirections";
    }

}
