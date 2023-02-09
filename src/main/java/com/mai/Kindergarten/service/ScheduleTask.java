package com.mai.Kindergarten.service;


import com.mai.Kindergarten.models.*;

import com.mai.Kindergarten.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ScheduleTask {
    @Autowired
    private MailSender mailSender;
    @Autowired
    private ChildRepository childRepository;
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private AgeGroupRepository ageGroupRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private IndividualSessionRepository individualSessionRepository;



    //Пересчитываем полные года каждого ребенка
    @Scheduled(fixedDelay = 86400)
    public void childAgeUpdate() {
        List<Child> allChildren = childRepository.findAll();
        for (Child child : allChildren) {
            child.calculateAge();
            childRepository.save(child);
        }
    }

    //Задание для повышения возрастной группы всех групп
    @Scheduled(fixedDelay = 200000)
    public void groupAgeUpdate() {
        List<Group> allGroups = groupRepository.findAll();
        Date date = new Date();
        //В мае все имеющиеся группы будут получать статус "Не повышались в этом году"
        if (date.getMonth() == 4) {
            for (Group group : allGroups) {
                group.setUpdateInYear(false);
                groupRepository.save(group);
            }
        }
        //В августе всеx группы у которых статус "Не повышались в этом году", будут повышены. Группы, которые будут созданы в мае, соответвенно не повысятся.
        if (date.getMonth() == 7) {
            List<AgeGroup> allAgeGroups = ageGroupRepository.findAll();
            long maxIndex = allAgeGroups.get(allAgeGroups.size() - 1).getId();
            for (Group group : allGroups) {
                if (!group.isUpdateInYear()) {
                    long currentAgeGroupId = group.getAgeGroup().getId();
                    if (currentAgeGroupId != maxIndex) {
                        group.setAgeGroup(ageGroupRepository.findById(currentAgeGroupId + 1).orElseThrow());
                    }
                    group.setUpdateInYear(true);
                    groupRepository.save(group);
                }
            }
        }
    }

    //С воскресенья по пятницу в 19:00, задание на отправку на почту напоминания с Индивидуальными заданиями
    @Transactional
    @Scheduled(fixedDelay = 6000000)
   // @Scheduled(cron = "0 0 19 * * SUN-THU") //Задание запускается каждый день с воскресенья по четверг в 19:00
    public void sendMessage() throws InterruptedException {
        System.out.println("Задания на отправку почты");
        Date today = new Date();
        List<User> allTeacher = userRepository.findAll().stream().filter(x -> x.getGroup() != null).collect(Collectors.toList());
        for (User user : allTeacher) {
            boolean check = false;
            //String textMessage = "Оповещение!\nНа завтра запланированы следующие индивидуальные занятия";
            String textMessage = "";
            //У каждого воспитателя, берем только тех детей из группы, у которые лист с индивидуальными занятиями не пустой
            List<Child> allChild = groupRepository.findById(user.getGroup().getId()).orElseThrow().getChildren()
                    .stream().filter(x -> x.getIndividualSessions().size() > 0).collect(Collectors.toList());
            for (Child child : allChild) {
                String messageAboutChild = "\n" + child.getFullName();
                System.out.println("Проблемный ребенок: " + child.getFullName());
                //Берем те занятия которые назначены на завтра
                List<IndividualSession> yesterdaySession = child.getIndividualSessions()
                        .stream().filter(x -> x.getDate().getDate() == (today.getDate() + 1)).collect(Collectors.toList());
                for (IndividualSession individualSession : yesterdaySession) {
                    check = true;
                    System.out.println("Занятие по вопросу - " + individualSession.getResultMonitoring().getQuestion());
                    messageAboutChild += "<br>Занятие по вопросу: " + individualSession.getResultMonitoring().getQuestion();
                    messageAboutChild += "<br>Методический материал: " + individualSession.getTypeMaterial() + " - " + individualSession.getNameFileMaterials();
                }
//                textMessage += "\n" + messageAboutChild;
                textMessage += "<br>" + HtmlLetter.getLetterPartChild(messageAboutChild);
            }
            if (check) {
                try {
                    mailSender.sendHTML(user.getUsername(), "Оповещение об индивидуальных занятиях", HtmlLetter.getLetterResult(textMessage));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
