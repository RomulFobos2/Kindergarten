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
import java.text.SimpleDateFormat;
import java.util.*;

//Класс для обработки страниц пользователя(воспитателя) для работы с детьми
@Controller
public class UserPanelMonitoringController {
    @Autowired
    UserRepository userRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private ChildRepository childRepository;
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private EducationAreaRepository educationAreaRepository;
    @Autowired
    private EducationDirectionRepository educationDirectionRepository;
    @Autowired
    private EducationIndicatorRepository educationIndicatorRepository;
    @Autowired
    private ResultMonitoringRepository resultMonitoringRepository;
    @Autowired
    private IndividualSessionRepository individualSessionRepository;

    @GetMapping("/user/allEducationAreas")
    public String educationAreasList(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userRepository.findByUsername(auth.getName());
        Group currentGroup = currentUser.getGroup();
        AgeGroup currentAgeGroup = currentGroup.getAgeGroup();
        Set<EducationArea> allEducationAreas = new HashSet<>();
        for (EducationDirection educationDirection : educationDirectionRepository.findAll()) {
            if (currentAgeGroup.getId() == educationDirection.getAgeGroup().getId()) {
                allEducationAreas.add(educationDirection.getEducationArea());
            }
        }
        model.addAttribute("allEducationAreas", allEducationAreas);
        return "user/monitoring/allEducationAreas";
    }


    @GetMapping("/user/allEducationAreas/report/{educationArea_id}")
    public String report(@PathVariable(value = "educationArea_id") long educationArea_id,
                         Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userRepository.findByUsername(auth.getName());
        List<Child> allChildren = new ArrayList<>();
        if (currentUser.getGroup() != null) {
            allChildren = currentUser.getGroup().getChildren();
        }
        Collections.sort(allChildren, new Comparator<Child>() {
            @Override
            public int compare(Child o1, Child o2) {
                return o1.getFullName().compareTo(o2.getFullName());
            }
        });
        Group group = currentUser.getGroup();
        List<EducationDirection> allEducationDirections = new ArrayList<>();
        for (EducationDirection educationDirection : educationDirectionRepository.findAll()) {
            if (educationDirection.getAgeGroup().getId() == group.getAgeGroup().getId()
                    && educationDirection.getEducationArea().getId() == educationArea_id) {
                allEducationDirections.add(educationDirection);
            }
        }
        model.addAttribute("allEducationDirections", allEducationDirections);
        model.addAttribute("educationArea", educationAreaRepository.findById(educationArea_id).orElseThrow());
        model.addAttribute("allChildren", allChildren);
        model.addAttribute("group", group);
        return "user/monitoring/report";
    }


    @PostMapping("/user/allEducationAreas/report/{educationArea_id}")
    public ResponseEntity<Object> report(@PathVariable(value = "educationArea_id") long educationArea_id,
                                         @RequestParam("inputRating") List<String> inputRating,
                                         @RequestParam("isStartYear") String isStartYear,
                                         Model model, HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userRepository.findByUsername(auth.getName());
        Group group = currentUser.getGroup();
        boolean isStarEducationYear = Boolean.parseBoolean(isStartYear);
        System.out.println(isStarEducationYear);
        Date date = new Date();

        //Получаем все напрваления развития по выбранной образовательной области и возрастной группы
        List<EducationDirection> allEducationDirections = new ArrayList<>();
        for (EducationDirection educationDirection : educationDirectionRepository.findAll()) {
            if (educationDirection.getAgeGroup().getId() == group.getAgeGroup().getId()
                    && educationDirection.getEducationArea().getId() == educationArea_id) {
                allEducationDirections.add(educationDirection);
            }
        }

        //Получаем детей в алфавитном порядке
        List<Child> allChildren = currentUser.getGroup().getChildren();
        Collections.sort(allChildren, new Comparator<Child>() {
            @Override
            public int compare(Child o1, Child o2) {
                return o1.getFullName().compareTo(o2.getFullName());
            }
        });

        //Массивы с ответами по таблицам. Копирую из входящего со страницы массива inputRating в массив с массиовами listRatingList
        //Если на странице 1 таблица с шестью вопросами, вторая с двумя, третья с тремя
        //то будет сформирорвано 3 массива с шесть, двумя и тремя элементами в listRatingList
        List<List<String>> listRatingList = new ArrayList<>();
        int startCopySubList = 0;
        for (EducationDirection educationDirection : allEducationDirections) {
            //Узнаю какое кол-во ячеек было для направления (кол-во вопросов * кол-во_детей)
            int countQuestion = educationDirection.getEducationIndicators().size() * allChildren.size() + startCopySubList;
            listRatingList.add(inputRating.subList(startCopySubList, countQuestion));
            startCopySubList = countQuestion;
        }

        //У каждого ребенка группа, проверяю, есть ли уже resultMonitoringChild (запись с результатом мониторинга) по этому мониторингу
        //если есть, то удаляем старые данные мониторингов по этому EducationDirection и в далнейшем сохраняем новые
        for (Child currentChild : allChildren) {
            for (EducationDirection currentEducationDirection : allEducationDirections) {
                Iterator<ResultMonitoring> iterator = currentChild.getResultMonitors().iterator();
                while (iterator.hasNext()) {
                    ResultMonitoring resultMonitoringChild = iterator.next();
                    if (resultMonitoringChild.getEducationDirection().getId() == currentEducationDirection.getId() & resultMonitoringChild.isStarEducationYear() == isStarEducationYear) {
                        iterator.remove();
                        //Если удаляем ResultMonitoring, то удаляем также и IndividualSession, которая ссылается на этот ResultMonitoring
                        for(IndividualSession individualSession : individualSessionRepository.findAll()){
                            if(individualSession.getResultMonitoring().getId() == resultMonitoringChild.getId()){
                                individualSessionRepository.delete(individualSession);
                            }
                        }
                        resultMonitoringRepository.delete(resultMonitoringChild);
                    }
                }
            }
        }


        for (int indexEdDirection = 0; indexEdDirection < allEducationDirections.size(); indexEdDirection++) {
            EducationDirection currentEducationDirection = allEducationDirections.get(indexEdDirection);
            //Получаем массив с ответами, порядковый номер которого
            // соответсвует порядковому номеру Направления развития в массиве allEducationDirections
            //то есть ответвы в i-массиве масива listRatingList, принадлежат i-направлению развития из массива allEducationDirections
            //тут хранится кол-во_детей*кол_во_вопросов_для_направления_равзития элементов
            List<String> answer = listRatingList.get(indexEdDirection);
            //Получаем кол-во вопросов у данного направления
            int countQuestion = currentEducationDirection.getEducationIndicators().size();
            int indexQuestion = 0;
            //ИНдекс ответов которые принадлежат ребенку в массиве answer
            //то есть с 0 по countQuestion (не включительено countQuestion) будут принадлежать первому ребенку, с countQuestion по countQuestion+countQuestion второму и т.д.
            int indexChild = 0;
//          System.out.println("countQuestion " + countQuestion);
            for (int i = 0; i < answer.size(); i++) {
//                System.out.println("i = " + i);
//                System.out.println("indexQuestion = " + indexQuestion);
//                System.out.println("indexChild = " + indexChild);
                Child currentChild = allChildren.get(indexChild);
                int currentRating = Integer.parseInt(answer.get(i));
                ResultMonitoring resultMonitoring = new ResultMonitoring(group, currentEducationDirection, currentUser, currentEducationDirection.getEducationIndicators().get(indexQuestion), currentRating, isStarEducationYear, date, currentChild);
                resultMonitoringRepository.save(resultMonitoring);
                currentChild.getResultMonitors().add(resultMonitoring);
                childRepository.save(currentChild);
                indexQuestion++;
                if ((i + 1) % countQuestion == 0) {
//                    System.out.println("Следующий ребенок");
                    indexChild++;
                    indexQuestion = 0;
                }
            }
        }
//        System.out.println(listRatingList);
        try {
            //начинаем формировать отчет
            return getReport(educationArea_id, allEducationDirections, currentUser, date, allChildren, isStarEducationYear);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private ResponseEntity<Object> getReport(Long educationArea_id, List<EducationDirection> allEducationDirections, User user, Date date, List<Child> allChildren, boolean isStarEducationYear) throws IOException, Docx4JException, JAXBException, CloneNotSupportedException {
        List<WordprocessingMLPackage> allReports = new ArrayList<>();
        //для каждого направления развития, мониторинг по которым былна странице, формируем отедльный отчет, в конце склеиваем их в один
        for (int i = 0; i < allEducationDirections.size(); i++) {
            //В зависимости от количества и порядкогово номера талици, получаем нужный шаблон отчета (начало, середина, или конец).
            EducationDirection educationDirection = allEducationDirections.get(i);
            int sizeEducationDirection = allEducationDirections.size();
            WordprocessingMLPackage wordDocument;
            switch (sizeEducationDirection) {
                case 1: {
                    //начало, в случае если таблица была одна
                    wordDocument = TemplateCreator.getTemplate("Templates reports\\Report_0.docx");
                    break;
                }
                default: {
                    if (i == 0) {
                        //начало
                        wordDocument = TemplateCreator.getTemplate("Templates reports\\Report_1.docx");
                    } else {
                        if (i != sizeEducationDirection - 1) {
                            //середина
                            wordDocument = TemplateCreator.getTemplate("Templates reports\\Report_2.docx");
                        } else {
                            //конец
                            wordDocument = TemplateCreator.getTemplate("Templates reports\\Report_3.docx");
                        }
                    }
                }
            }
            EducationArea educationArea = educationAreaRepository.getById(educationArea_id);
            SimpleDateFormat simpleDateFormat_out = new SimpleDateFormat("«dd» MMMM yyyy");
            String strDate = simpleDateFormat_out.format(date);
            String groupInfo = user.getGroup().getGroupName() + " (" + user.getGroup().getAgeGroup().getName() + ")";
            TemplateCreator.replacePlaceholder(wordDocument, educationArea.getName(), "educationArea");
            TemplateCreator.replacePlaceholder(wordDocument, strDate, "date");
            TemplateCreator.replacePlaceholder(wordDocument, user.getFullName(), "userFullName");
            TemplateCreator.replacePlaceholder(wordDocument, groupInfo, "groupInfo");
            TemplateCreator.replacePlaceholder(wordDocument, educationDirection.getName(), "educationDirection");
            TemplateCreator.addQuestionColumn(wordDocument, educationDirection.getEducationIndicators(), "question");
            TemplateCreator.addQuestionRow(wordDocument, educationDirection.getEducationIndicators(), "question");
            TemplateCreator.addChildRow(wordDocument, allChildren, "childFullName", educationDirection, isStarEducationYear);
            allReports.add(wordDocument);
        }
        WordprocessingMLPackage resultReport = allReports.get(0);
        for (int i = 1; i < allReports.size(); i++) {
            resultReport = TemplateCreator.merge(resultReport, allReports.get(i));
        }
        return TemplateCreator.downloadReport(resultReport, "Report");
    }

}
