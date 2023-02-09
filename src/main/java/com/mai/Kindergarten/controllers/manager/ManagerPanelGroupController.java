package com.mai.Kindergarten.controllers.manager;


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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

//Класс для обработки страниц менеджера для работы с группами
@Controller
public class ManagerPanelGroupController {
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
    @Autowired
    private EducationAreaRepository educationAreaRepository;
    @Autowired
    private LessonRepository lessonRepository;
    @Autowired
    private IndividualSessionRepository individualSessionRepository;
    @Autowired
    private ResultMonitoringRepository resultMonitoringRepository;
    @Autowired
    private DiplomaRepository diplomaRepository;

    @GetMapping("/manager/allGroups")
    public String groupList(Model model, HttpServletRequest request) {
        if (request.getSession().getAttribute("deleteError") != null && (Boolean) request.getSession().getAttribute("deleteError")) {
            model.addAttribute("deleteError", true);
            request.getSession().setAttribute("deleteError", false);
        }
        List<Group> allGroups = groupRepository.findAll();
        model.addAttribute("allGroups", allGroups);

        //Когда посещаем страницу, смотрим все группы, для тех у которых коллекция lessons пустая,
        // создаем новые сущности Lesson на основе направления развития (EducationDirection) в зависимости от возрасной группы (AgeGroup) - группы
        for(Group group : allGroups){
            if(group.getLessons().size() == 0){
                List<EducationDirection> educationDirectionsForCurrentGroup = educationDirectionRepository.findAll()
                        .stream().filter(x -> x.getAgeGroup().getId() == group.getAgeGroup().getId()).filter(x -> x.isHaveLesson()).collect(Collectors.toList());
                //В коллекции храним ID направлений, для которых процесс формирования уроков нужно пропустить
                //т.к. у этого направление было общее кол-во уроков с направление, по которому формирование уроков уже прошло
                List<Long> breakID = new ArrayList<>();
                for(EducationDirection educationDirection : educationDirectionsForCurrentGroup){
                    String lessonName = educationDirection.getName();
                    if(breakID.contains(educationDirection.getId())){
                        continue;
                    }
                    if(educationDirection.getEducationDirectionTotalCountLesson() != null){
                        breakID.add(educationDirection.getEducationDirectionTotalCountLesson().getId());
                        lessonName += " / " + educationDirection.getEducationDirectionTotalCountLesson().getName();
                    }
                    for(int i = 0; i < educationDirection.getCountLesson(); i++){
                        Lesson lesson = new Lesson(educationDirection, group, lessonName);
                        group.getLessons().add(lesson);
                        lessonRepository.save(lesson);
                    }
                }
//                //Отдельно добавляем Музыку
//                for(int i = 0; i < 2; i++){
//                    Lesson lesson = new Lesson("Музыка", group);
//                    group.getLessons().add(lesson);
//                    lessonRepository.save(lesson);
//                }
                groupRepository.save(group);
            }
        }
        return "manager/group/allGroups";
    }

    @GetMapping("/manager/addGroup")
    public String addGroup(Model model, HttpServletRequest request) {
        if (request.getSession().getAttribute("groupNameError") != null && (Boolean) request.getSession().getAttribute("groupNameError")) {
            model.addAttribute("groupNameError", "Группа с таким именем уже существует");
            request.getSession().setAttribute("groupNameError", false);
        }
        Iterable<AgeGroup> ageGroups = ageGroupRepository.findAll();
        model.addAttribute("allAgeGroups", ageGroups);
        List<Cabinet> cabinets = cabinetRepository.findAll().stream().filter(x -> !x.isBusy()).collect(Collectors.toList());
        if (cabinets.size() == 0) {
            model.addAttribute("cabinetSizeError", "Нет свободных кабинетов");
        }
        model.addAttribute("allCabinets", cabinets);
        List<User> users = userRepository.findAll().stream().filter(x -> x.getRoles().stream().allMatch(y -> y.getName().equals("ROLE_USER"))).filter(z -> z.getGroup() == null).collect(Collectors.toList());
        long maxID = -1; //грузим id последнего элемента на страницу сразу, чтобы не вызывать его в java скрипте, т.к. если список воспитателй пуст, тимлиф не прогружает нормально код скрипта и он крашится.
        try {
            maxID = users.get(users.size() - 1).getId();
        } catch (Exception e) {
            model.addAttribute("groupSizeError", "Нет свободных воспитателей");
        }
        model.addAttribute("allUsers", users);
        model.addAttribute("maxID", maxID);
        return "manager/group/addGroup";
    }


    @PostMapping("/manager/addGroup")
    public String addGroup(@RequestParam String inputGroupName, @RequestParam Long inputAgeGroup,
                           @RequestParam List<Long> inputUser, @RequestParam Long inputCabinet,
                           Model model, HttpServletRequest request) {
        AgeGroup ageGroup = ageGroupRepository.findById(inputAgeGroup).orElseThrow();
        Cabinet cabinet = cabinetRepository.findById(inputCabinet).orElseThrow();
        if (groupRepository.findByGroupName(inputGroupName) != null) {
            request.getSession().setAttribute("groupNameError", true);
//            return "manager/group/addGroup";
            return "redirect:/manager/addGroup";
        }
        List<User> users = new ArrayList<>();
        for (Long var_id : inputUser) {
            if (var_id != -1) {
                users.add(userService.findUserById(var_id));
            }
        }
        cabinet.setBusy(true);
        cabinetRepository.save(cabinet);
        Group group = new Group(inputGroupName, users, ageGroup, cabinet);
        groupRepository.save(group);
        for (User user : users) {
            user.setGroup(group);
            userRepository.save(user);
        }
        return "redirect:/manager/allGroups";
    }

    //Формируем динамически страницу для каждого пользователя. Внутри страницы можно сделать операции над пользователем
    @GetMapping("/manager/allGroups/group-details/{id}")
    public String groupDetails(@PathVariable(value = "id") long id,
                               Model model, HttpServletRequest request) {
        if (!groupRepository.existsById(id)) {
            return "redirect:/manager/allGroups";
        }
        int intSorted = request.getSession().getAttribute("sorted") == null ? 0 : Integer.parseInt((String) request.getSession().getAttribute("sorted"));
        Group group = groupRepository.findById(id).orElseThrow();
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
        List<GroupResultMonitoring> allGroupResultMonitorings = groupRepository.findById(id).orElseThrow().getGroupResultMonitorings();
        model.addAttribute("allGroupResultMonitorings", allGroupResultMonitorings);
        model.addAttribute("allChildren", allChildren);
        return "manager/group/group-details";
    }

    //Формируем динамически страницу для каждого пользователя. Внутри страницы можно сделать операции над пользователем
    @PostMapping("/manager/allGroups/group-details/{id}")
    public String groupDetailsSorted(@PathVariable(value = "id") long id, @RequestParam String sorted,
                                     Model model, HttpServletRequest request) {
        request.getSession().setAttribute("sorted", sorted);
        return "redirect:/manager/allGroups/group-details/{id}";
    }

    @GetMapping("/manager/editGroup/{id}")
    public String groupEdit(@PathVariable(value = "id") long id,
                            Model model, HttpServletRequest request) {
        if (!groupRepository.existsById(id)) {
            return "redirect:/manager/allGroups";
        }
        if (request.getSession().getAttribute("groupNameError") != null && (Boolean) request.getSession().getAttribute("groupNameError")) {
            model.addAttribute("groupNameError", "Группа с таким именем уже существует");
            request.getSession().setAttribute("groupNameError", false);
        }
        Group group = groupRepository.findById(id).orElseThrow();
        model.addAttribute("group", group);
        //Убираем из списка текущую возрастную группу Группы. ЕЕ добавляем в атрибут select через атрибут group
        Iterable<AgeGroup> ageGroups = ageGroupRepository.findAll().stream().filter(x -> !x.getName().equals(group.getAgeGroup().getName())).collect(Collectors.toList());
        model.addAttribute("allAgeGroups", ageGroups);
        Iterable<Cabinet> cabinets = cabinetRepository.findAll().stream().filter(x -> !x.getNumber().equals(group.getCabinet().getNumber())).filter(x -> !x.isBusy()).collect(Collectors.toList());
        model.addAttribute("allCabinets", cabinets);
        List<User> users = userRepository.findAll().stream().filter(x -> x.getRoles().stream().allMatch(y -> y.getName().equals("ROLE_USER"))).filter(z -> z.getGroup() == null || z.getGroup().getId() == group.getId()).collect(Collectors.toList());
        long maxID = -1; //грузим id последнего элемента на страницу сразу, чтобы не вызывать его в java скрипте, т.к. если список воспитателй пуст, тимлиф не прогружает нормально код скрипта и он крашится.
        try {
            maxID = users.get(users.size() - 1).getId();
        } catch (Exception e) {
        }
        List<User> groupUsers = userRepository.findAll().stream().filter(x -> x.getGroup() != null && x.getGroup().getId() == group.getId()).collect(Collectors.toList());
        switch (groupUsers.size()) {
            case 1:
                model.addAttribute("groupUser_1", groupUsers.get(0).getId());
                break;
            case 2:
                model.addAttribute("groupUser_1", groupUsers.get(0).getId());
                model.addAttribute("groupUser_2", groupUsers.get(1).getId());
                break;
        }
        model.addAttribute("groupUserSize", groupUsers.size());
        model.addAttribute("allUsers", users);
        model.addAttribute("maxID", maxID);
        return "manager/group/editGroup";
    }

    @PostMapping("/manager/editGroup/{id}")
    public String editGroup(@PathVariable(value = "id") long id, @RequestParam String inputGroupName, @RequestParam Long inputAgeGroup,
                            @RequestParam List<Long> inputUser, @RequestParam Long inputCabinet,
                            Model model, HttpServletRequest request) {
        Group group = groupRepository.findById(id).orElseThrow();
        AgeGroup ageGroup = ageGroupRepository.findById(inputAgeGroup).orElseThrow();
        Cabinet cabinet = cabinetRepository.findById(inputCabinet).orElseThrow();
        if (groupRepository.findByGroupName(inputGroupName) != null & !group.getGroupName().equals(inputGroupName)) {
            request.getSession().setAttribute("groupNameError", true);
            return "redirect:/manager/editGroup/{id}";
        }
        List<User> users = new ArrayList<>();
        for (Long var_id : inputUser) {
            if (var_id != -1) {
                users.add(userService.findUserById(var_id));
            }
        }
        Cabinet currentCabinet = cabinetRepository.findByNumber(group.getCabinet().getNumber());
        currentCabinet.setBusy(false);
        cabinetRepository.save(currentCabinet);

        cabinet.setBusy(true);
        cabinetRepository.save(cabinet);
        group.setGroupName(inputGroupName);
        group.setAgeGroup(ageGroup);
        group.setCabinet(cabinet);
        groupRepository.save(group);

        for (User user : userRepository.findAll()) {
            if (user.getGroup() != null && user.getGroup().getId() == id) {
                user.setGroup(null);
                userRepository.save(user);
            }
        }

        for (User user : users) {
            user.setGroup(group);
            userRepository.save(user);
        }
        return "redirect:/manager/allGroups";
    }

    @PostMapping("/manager/allGroups/group-details/{id}/remove")
    public String delete_group(@PathVariable(value = "id") long id,
                               Model model, HttpServletRequest request) {
        Group group = groupRepository.findById(id).orElseThrow();
        Cabinet cabinet = cabinetRepository.findById(group.getCabinet().getId()).orElseThrow();
        try {
            for(User user : group.getUsers()){
                user.setGroup(null);
            }
            for(Child child : group.getChildren()){
                List<IndividualSession> allIndividualSessions = individualSessionRepository.findByChild(child);
                List<ResultMonitoring> allResultMonitorings = resultMonitoringRepository.findByChild(child);
                List<Diploma> allDiplomas = diplomaRepository.findByChild(child);
                for(IndividualSession individualSession : allIndividualSessions){
                    individualSessionRepository.delete(individualSession);
                }
                for(ResultMonitoring resultMonitoring : allResultMonitorings){
                    resultMonitoringRepository.delete(resultMonitoring);
                }
                for(Diploma diploma : allDiplomas){
                    diplomaRepository.delete(diploma);
                }
                childRepository.delete(child);
            }
            for(Lesson lesson : group.getLessons()){
                lessonRepository.delete(lesson);
            }
            for(GroupResultMonitoring resultMonitoring : group.getGroupResultMonitorings()){
                groupResultMonitoringRepository.delete(resultMonitoring);
            }
            groupRepository.delete(group);
            cabinet.setBusy(false);
            cabinetRepository.save(cabinet);

        } catch (Exception e) {
            request.getSession().setAttribute("deleteError", true);
        }
        return "redirect:/manager/allGroups";
    }

    @GetMapping("/manager/allGroups/group-details/{group_id}/editChild/{child_id}")
    public String childEdit(@PathVariable(value = "group_id") long group_id, @PathVariable(value = "child_id") long child_id,
                            Model model) {
        if (!groupRepository.existsById(group_id) || !childRepository.existsById(child_id)) {
            return "redirect:/manager/allGroups";
        }
        Child child = childRepository.findById(child_id).orElseThrow();
        model.addAttribute("child", child);
        List<Group> allGroups = groupRepository.findAll().stream().filter(x -> x.getId() != group_id).collect(Collectors.toList());
        model.addAttribute("allGroups", allGroups);
        return "manager/group/editChild";
    }

    @PostMapping("/manager/allGroups/group-details/{group_id}/editChild/{child_id}")
    public String childEdit(@PathVariable(value = "group_id") long group_id, @PathVariable(value = "child_id") long child_id,
                            @RequestParam String inputLastName, @RequestParam String inputFirstName,
                            @RequestParam String inputPatronymicName, @RequestParam String StrBirthday, @RequestParam Long inputGroup,
                            Model model) {
        Child child = childRepository.findById(child_id).orElseThrow();
        child.setLastName(inputLastName);
        child.setFirstName(inputFirstName);
        child.setPatronymicName(inputPatronymicName);
        SimpleDateFormat simpleDateFormat_inner = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date date = null;
        try {
            date = simpleDateFormat_inner.parse(StrBirthday);
        } catch (ParseException e) {

        }
        child.setDateOfBirth(date);
        childRepository.save(child);

        Group oldGroup = groupRepository.findById(group_id).orElseThrow();
        Group newGroup = groupRepository.findById(inputGroup).orElseThrow();
        oldGroup.getChildren().remove(child);
        newGroup.getChildren().add(child);
        groupRepository.save(oldGroup);
        groupRepository.save(newGroup);
        return "redirect:/manager/allGroups/group-details/" + inputGroup;
    }


    @GetMapping("/manager/report/{group_id}/{isStarEducationYear}")
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
                    System.out.println(child.getFullName());
                    System.out.println("midRating = " + midRating);
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


    @GetMapping("/manager/general-report/{isStarEducationYear}")
    public ResponseEntity<Object> generalReport(@PathVariable(value = "isStarEducationYear") String isStarEducationYear,
                                                Model model, HttpServletRequest request) throws IOException, Docx4JException, JAXBException, CloneNotSupportedException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userRepository.findByUsername(auth.getName());
        List<EducationArea> allEducationAreas = educationAreaRepository.findAll();
        List<EducationDirection> allEducationDirections = educationDirectionRepository.findAll();
        boolean boolStarEducationYear = Boolean.parseBoolean(isStarEducationYear);
        Date date = new Date();
        SimpleDateFormat simpleDateFormat_out = new SimpleDateFormat("«dd» MMMM yyyy");
        SimpleDateFormat simpleDateFormat_period = new SimpleDateFormat("yyyy");
        String strDate = simpleDateFormat_out.format(date);
        String period;
        if (date.getMonth() >= 8) {
            Date futureYear = new Date();
            futureYear.setYear(futureYear.getYear() - 1);
            String currentYear = simpleDateFormat_period.format(date);
            String strFutureYear = simpleDateFormat_period.format(futureYear);
            period = "" + currentYear + "/" + strFutureYear + " уч.г.";
        } else {
            Date lastYear = new Date();
            lastYear.setYear(lastYear.getYear() - 1);
            String currentYear = simpleDateFormat_period.format(date);
            String strLastYear = simpleDateFormat_period.format(lastYear);
            period = "" + strLastYear + "/" + currentYear + " уч.г.";
        }
        List<Group> allGroups = groupRepository.findAll();


        WordprocessingMLPackage wordDocument = TemplateCreator.getTemplate("Templates reports\\Report_5.docx");
        TemplateCreator.replacePlaceholder(wordDocument, strDate, "date");
        TemplateCreator.replacePlaceholder(wordDocument, currentUser.getFullName(), "userFullName");
        TemplateCreator.replacePlaceholder(wordDocument, period, "period");

        TemplateCreator.addGroupColumn(wordDocument, allGroups, "groupFullName");
        TemplateCreator.addEducationAreaRow(wordDocument, allEducationAreas, allGroups, allEducationDirections, "numbEdArea", boolStarEducationYear);
        System.out.println("+++++++++++++++++++++++++++++++++++++++");
        System.out.println(TemplateCreator.listAllHighLvlStart);
        System.out.println(TemplateCreator.listAllMidLvlStart);
        System.out.println(TemplateCreator.listAllLowLvlStart);
        System.out.println(TemplateCreator.listAllHighLvlEnd);
        System.out.println(TemplateCreator.listAllMidLvlEnd);
        System.out.println(TemplateCreator.listAllLowLvlEnd);
        System.out.println("+++++++++++++++++++++++++++++++++++++++");
        TemplateCreator.replacePlaceholder(wordDocument, period, "period");
        TemplateCreator.addEducationAreaRowSecondTable(wordDocument, allEducationAreas, "edArea", boolStarEducationYear);

        return TemplateCreator.downloadReport(wordDocument, "Report");
    }

}
