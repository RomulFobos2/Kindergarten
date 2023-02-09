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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class UserPanelIndividualSessionsController {
    @Autowired
    UserRepository userRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private AgeGroupRepository ageGroupRepository;
    @Autowired
    private ChildRepository childRepository;
    @Autowired
    private EducationDirectionRepository educationDirectionRepository;
    @Autowired
    private TalkManualRepository talkManualRepository;
    @Autowired
    private GameManualRepository gameManualRepository;
    @Autowired
    private ManualRepository manualRepository;
    @Autowired
    private ResultMonitoringRepository resultMonitoringRepository;
    @Autowired
    private EducationAreaRepository educationAreaRepository;
    @Autowired
    private IndividualSessionRepository individualSessionRepository;


    @GetMapping("/user/individualSessions")
    public String individualSessionsDetails(Model model, HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        User user = userRepository.findByUsername(auth.getName());
        Group group = user.getGroup();
        List<Child> allChildren = group.getChildren();

        List<Manual> allManuals = manualRepository.findAll();

        List<TalkManual> allTalkManuals = talkManualRepository.findAll();

        List<GameManual> allGameManuals = gameManualRepository.findAll();

        //Только результаты моников для 1) детей текущей группы 2) с рейтингом == 1 3)начало года (для конца года такой план не составляется)
        List<ResultMonitoring> allResultMonitorings = resultMonitoringRepository.findAll().stream().filter(x -> x.getGroup().getId() == group.getId()).filter(x -> x.getRating() == 1).filter(x -> x.isStarEducationYear()).collect(Collectors.toList());

        model.addAttribute("user", user);
        model.addAttribute("group", group);
        model.addAttribute("allChildren", allChildren);
        model.addAttribute("allManuals", allManuals);
        model.addAttribute("allTalkManuals", allTalkManuals);
        model.addAttribute("allGameManuals", allGameManuals);
        model.addAttribute("allResultMonitorings", allResultMonitorings);
        Date startDate = new Date();
        startDate.setDate(1);
        Calendar myCalendar = (Calendar) Calendar.getInstance().clone();
        myCalendar.set(startDate.getYear(), startDate.getMonth(), 1);
        int max_date = myCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        Date endDate = new Date();
        endDate.setDate(max_date);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        ResultMonitoring resultMonitoring = allResultMonitorings.get(0);
        return "user/individualSessions/individualSessions";
    }

/*
inputChild, inputResultMonitoring, inputEducationArea, inputEducationDirection, inputTypeMaterial, inputManual, inputDate
 */


    /*
            for (Child currentChild : allChildren) {
            for (EducationDirection currentEducationDirection : allEducationDirections) {
                Iterator<ResultMonitoring> iterator = currentChild.getResultMonitors().iterator();
                while (iterator.hasNext()) {
                    ResultMonitoring resultMonitoringChild = iterator.next();
                    if (resultMonitoringChild.getEducationDirection().getId() == currentEducationDirection.getId() & resultMonitoringChild.isStarEducationYear() == isStarEducationYear) {
                        iterator.remove();
                        resultMonitoringRepository.delete(resultMonitoringChild);
                    }
                }
            }
        }
     */

    @PostMapping("/user/individualSessions")
    public ResponseEntity<Object> individualReport(@RequestParam List<Long> inputChild, @RequestParam List<Long> inputResultMonitoring,
                                                   @RequestParam List<Long> inputEducationArea, @RequestParam List<Long> inputEducationDirection,
                                                   @RequestParam List<Integer> inputTypeMaterial, @RequestParam List<String> inputManual,
                                                   @RequestParam List<String> inputDate,
                                                   Model model, HttpServletRequest request) throws IOException, Docx4JException, JAXBException, CloneNotSupportedException, ParseException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByUsername(auth.getName());
        Group group = user.getGroup();

        List<Child> allChildren = new ArrayList<>();
        List<ResultMonitoring> allResultMonitorings = new ArrayList<>();
        List<EducationArea> allEducationAreas = new ArrayList<>();
        List<EducationDirection> allEducationDirections = new ArrayList<>();

        //можно было бы получить из репозитория, но не охото фильтровать.
        // Поэтому здесь запоминаем все планы которые создали для передачи в методы формирования отчета
        List<IndividualSession> newIndividualSessions = new ArrayList<>();

        for(Long child_id : inputChild){
            Child child = childRepository.findById(child_id).orElseThrow();
            allChildren.add(child);
        }
        for(Long resultMonitoring_id : inputResultMonitoring){
            ResultMonitoring resultMonitoring = resultMonitoringRepository.findById(resultMonitoring_id).orElseThrow();
            allResultMonitorings.add(resultMonitoring);
        }
        for(Long educationArea_id : inputEducationArea){
            EducationArea educationArea = educationAreaRepository.findById(educationArea_id).orElseThrow();
            allEducationAreas.add(educationArea);
        }
        for(Long educationDirection_id : inputEducationDirection){
            EducationDirection educationDirection = educationDirectionRepository.findById(educationDirection_id).orElseThrow();
            allEducationDirections.add(educationDirection);
        }

        //У всех детей которых получили с формы удаляем старые индивидуальные занятия, чтобы заполнить актуальными
        for(Child child : allChildren){
            Iterator<IndividualSession> iterator = child.getIndividualSessions().iterator();
            while (iterator.hasNext()){
                IndividualSession individualSession = iterator.next();
                iterator.remove();
                individualSessionRepository.delete(individualSession);
            }
        }

        for(int i = 0; i < allChildren.size(); i++){
            Child child = allChildren.get(i);
            ResultMonitoring resultMonitoring = allResultMonitorings.get(i);
            EducationArea educationArea = allEducationAreas.get(i);
            EducationDirection educationDirection = allEducationDirections.get(i);
            int idTypeMaterial = inputTypeMaterial.get(i);
            String typeMaterial = "";
            switch (idTypeMaterial){
                case 0 : typeMaterial = "Лекция"; break;
                case 1 : typeMaterial = "Беседа"; break;
                case 2 : typeMaterial = "Игра"; break;
            }
            String nameFileMaterials = inputManual.get(i);
            SimpleDateFormat simpleDateFormat_inner = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = simpleDateFormat_inner.parse(inputDate.get(i));
            IndividualSession individualSession = new IndividualSession(child, resultMonitoring, educationArea, educationDirection, typeMaterial, nameFileMaterials, date);
            individualSessionRepository.save(individualSession);
            child.getIndividualSessions().add(individualSession);
            childRepository.save(child);
            newIndividualSessions.add(individualSession);
//            System.out.print("Ребенок - " + child.getFullName());
//            System.out.print(" Проблема с критерием - " + resultMonitoring.getQuestion());
//            System.out.print(" Область - " + educationArea.getName());
//            System.out.print(" Направление - " + educationDirection.getName());
//            System.out.print(" Тип занятия - " + strTypeMaterials);
//            System.out.print(" Имя файла с материалом - " + nameFileMaterials);
//            System.out.println(" Дата - " + date);
        }


        Date date = new Date();
        SimpleDateFormat simpleDateFormat_out = new SimpleDateFormat("«dd» MMMM yyyy");
        SimpleDateFormat simpleDateFormat_period = new SimpleDateFormat("yyyy");
        SimpleDateFormat simpleDateFormat_Month = new SimpleDateFormat("MMMM");
        String strDate = simpleDateFormat_out.format(date);
        String period;
        if (date.getMonth() >= 8) {
            Date futureYear = new Date();
            futureYear.setYear(futureYear.getYear() - 1);
            String currentYear = simpleDateFormat_period.format(date);
            String strFutureYear = simpleDateFormat_period.format(futureYear);
            period = "" + currentYear + "/" + strFutureYear;
        } else {
            Date lastYear = new Date();
            lastYear.setYear(lastYear.getYear() - 1);
            String currentYear = simpleDateFormat_period.format(date);
            String strLastYear = simpleDateFormat_period.format(lastYear);
            period = "" + strLastYear + "/" + currentYear;
        }

        String groupName = group.getGroupName() + " (" + group.getAgeGroup().getName() + ")";


        WordprocessingMLPackage wordDocument = TemplateCreator.getTemplate("Templates reports\\Report_8.docx");
        TemplateCreator.replacePlaceholder(wordDocument, groupName, "groupName");
        TemplateCreator.replacePlaceholder(wordDocument, period, "period");
        TemplateCreator.replacePlaceholder(wordDocument, simpleDateFormat_Month.format(date).toUpperCase(Locale.ROOT), "Month");
        TemplateCreator.replacePlaceholder(wordDocument, user.getFullName(), "userFullName");
        TemplateCreator.replacePlaceholder(wordDocument, strDate, "date");
        TemplateCreator.addIndividualSessionRow(wordDocument, newIndividualSessions, "num");
        return TemplateCreator.downloadReport(wordDocument, "Report");
    }


}
