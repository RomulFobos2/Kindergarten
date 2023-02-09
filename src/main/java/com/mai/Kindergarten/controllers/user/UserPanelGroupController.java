package com.mai.Kindergarten.controllers.user;

import com.mai.Kindergarten.models.*;
import com.mai.Kindergarten.repo.*;
import com.mai.Kindergarten.service.TemplateCreator;
import com.mai.Kindergarten.service.UserService;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

//Класс для обработки страниц воспитателя для работы с группами
@Controller
public class UserPanelGroupController {
    @Autowired
    UserRepository userRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private AgeGroupRepository ageGroupRepository;
    @Autowired
    private CabinetRepository cabinetRepository;
    @Autowired
    private ChildRepository childRepository;
    @Autowired
    private EducationDirectionRepository educationDirectionRepository;
    @Autowired
    private GroupResultMonitoringRepository groupResultMonitoringRepository;

    //Формируем динамически страницу для каждого пользователя. Внутри страницы можно сделать операции над пользователем
    @GetMapping("/user/group-details")
    public String groupDetails(Model model, HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userRepository.findByUsername(auth.getName());
        Group group = currentUser.getGroup();
        if(group == null){
            return "redirect:/user";
        }
        int intSorted = request.getSession().getAttribute("sorted") == null ? 0 : Integer.parseInt((String) request.getSession().getAttribute("sorted"));
        model.addAttribute("group", group);
        List<Child> allChildren = group.getChildren();
        if (intSorted == 0) {
            Collections.sort(allChildren, new Comparator<Child>() {
                @Override
                public int compare(Child o1, Child o2) {
                    return o1.getFullName().compareTo(o2.getFullName());
                }
            });
        } else {
            Collections.sort(allChildren, new Comparator<Child>() {
                @Override
                public int compare(Child o1, Child o2) {
                    return o1.getDateOfBirth().compareTo(o2.getDateOfBirth());
                }
            });
        }
        List<GroupResultMonitoring> allGroupResultMonitorings = group.getGroupResultMonitorings();
        model.addAttribute("allGroupResultMonitorings", allGroupResultMonitorings);
        model.addAttribute("allChildren", allChildren);
        return "user/group/group-details";
    }

    //Формируем динамически страницу для каждого пользователя. Внутри страницы можно сделать операции над пользователем
    @PostMapping("/user/group-details")
    public String groupDetailsSorted(@RequestParam String sorted,
                                     Model model, HttpServletRequest request) {
        request.getSession().setAttribute("sorted", sorted);
        return "redirect:/user/group-details";
    }


    @GetMapping("/user/group-details/report/{group_id}/{isStarEducationYear}")
    public ResponseEntity<Object> report(@PathVariable(value = "group_id") long group_id,
                                         @PathVariable(value = "isStarEducationYear") String isStarEducationYear,
                                         Model model, HttpServletRequest request) throws IOException, Docx4JException, JAXBException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userRepository.findByUsername(auth.getName());
        Group group = groupRepository.findById(group_id).orElseThrow();
        AgeGroup ageGroup = group.getAgeGroup();
        List<Child> allChildren = group.getChildren();

        boolean boolStarEducationYear = Boolean.parseBoolean(isStarEducationYear);
        Date date = new Date();
        List<EducationDirection> allEducationDirections = educationDirectionRepository.findByAgeGroup(ageGroup);
        DecimalFormat f = new DecimalFormat("###.#");

        //Перед формирование отчета, необходимо обновить данные мониторингов, если вдруг были пересталены оценки.
        //Для этого не забываем удалить старые результаты мониторингов группы
        Iterator<GroupResultMonitoring> iterator = group.getGroupResultMonitorings().iterator();
        while (iterator.hasNext()) {
            GroupResultMonitoring resultMonitoring = iterator.next();
            if (resultMonitoring.isStarEducationYear() == boolStarEducationYear) {
                iterator.remove();
                groupResultMonitoringRepository.delete(resultMonitoring);
            }
        }

        int countAvailableChildFORReport = allChildren.size();
        for (EducationDirection educationDirection : allEducationDirections) {
            int countAvailableChild = allChildren.size();
            int countQuestion = educationDirection.getEducationIndicators().size();
            System.out.println("countQuestion Кол-во вопросов у направления - " + countQuestion);
            float countHighLevel = 0;
            float countMidLevel = 0;
            float countLowLevel = 0;
            for (Child child : allChildren) {
                //Берем у ребенка все вопросы текущего мониторинга и считаме по нему среднею оценку.
                List<ResultMonitoring> currentResultMonitorings = child.getResultMonitors().stream().filter(x -> x.getEducationDirection().getId() == educationDirection.getId()).filter(x -> x.isStarEducationYear() == boolStarEducationYear).collect(Collectors.toList());
                System.out.println("Кол-во ответов у ребенка по этому направления (должно совпадать с countQuestion) - " + countQuestion);
                float midRating = 0;
                for (ResultMonitoring resultMonitoring : currentResultMonitorings) {
                    midRating += resultMonitoring.getRating();
                }
                if (midRating == 0) {
                    countAvailableChild--;
                    continue;
                }
                midRating = midRating / currentResultMonitorings.size();
                if (midRating >= 2.5) {
                    countHighLevel++;
                } else {
                    if (midRating > 1.5) {
                        countMidLevel++;
                    } else {
                        countLowLevel++;
                    }
                }
            }
            float onePercent = (float) (100.0 / countAvailableChild);
            countHighLevel *= onePercent;
            countMidLevel *= onePercent;
            countLowLevel *= onePercent;
            System.out.println("onePercent - " + onePercent);
            System.out.println("countHighLevel - " + countHighLevel);
            System.out.println("countMidLevel - " + countMidLevel);
            System.out.println("countLowLevel - " + countLowLevel);

            //Если все дети отсутствовали на мониторинге
            if (Float.isNaN(countHighLevel)) {
                countHighLevel = 0;
            }
            if (Float.isNaN(countMidLevel)) {
                countMidLevel = 0;
            }
            if (Float.isNaN(countLowLevel)) {
                countLowLevel = 0;
            }

            GroupResultMonitoring
                    resultMonitoring = new GroupResultMonitoring(educationDirection, boolStarEducationYear, f.format(countHighLevel), f.format(countMidLevel), f.format(countLowLevel), String.valueOf(countAvailableChild), String.valueOf(allChildren.size()));
            groupResultMonitoringRepository.save(resultMonitoring);
            group.getGroupResultMonitorings().add(resultMonitoring);
            groupRepository.save(group);
            if (countAvailableChild != allChildren.size()) {
                countAvailableChildFORReport = countAvailableChild;
            }
        }


        //Формируем документ
        WordprocessingMLPackage wordDocument = TemplateCreator.getTemplate("Templates reports\\Report_4.docx");
        SimpleDateFormat simpleDateFormat_out = new SimpleDateFormat("«dd» MMMM yyyy");
        String strDate = simpleDateFormat_out.format(date);
        String groupInfo = group.getGroupName() + " (" + group.getAgeGroup().getName() + ")";
        TemplateCreator.replacePlaceholder(wordDocument, strDate, "date");
        TemplateCreator.replacePlaceholder(wordDocument, currentUser.getFullName(), "userFullName");
        TemplateCreator.replacePlaceholder(wordDocument, groupInfo, "groupInfo");
        TemplateCreator.replacePlaceholder(wordDocument, String.valueOf(allChildren.size()), "countFull");
        TemplateCreator.replacePlaceholder(wordDocument, String.valueOf(countAvailableChildFORReport), "allSize");
        List<GroupResultMonitoring> groupResultMonitorings = group.getGroupResultMonitorings().stream().filter(x -> x.isStarEducationYear() == boolStarEducationYear).collect(Collectors.toList());
        TemplateCreator.addGroupResultRow(wordDocument, groupResultMonitorings);
        return TemplateCreator.downloadReport(wordDocument, "Report");
    }

}
