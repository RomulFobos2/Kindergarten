package com.mai.Kindergarten.models;

import com.mai.Kindergarten.service.MyService;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "t_group")
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String groupName;

    //Не нужное поле по идее
    @OneToMany(mappedBy = "group")
    private List<User> users;

//    @ManyToMany(fetch = FetchType.EAGER)
//    private List<User> users;  //2вариант с сущностью User

    //    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
//    @OneToMany(mappedBy = "group")
    @OneToMany
    @JoinColumn(name = "group_id")
    private List<Child> children;

    @ManyToOne
    @JoinColumn(name = "age_group_id")
    private AgeGroup ageGroup;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true) //Хозяин отношений
    private List<Lesson> lessons = new ArrayList<>();

    @OneToOne
    @JoinColumn(name = "cabinet_id")
    private Cabinet cabinet;

    private boolean updateInYear = true;

    @OneToMany
    @JoinColumn(name = "group_id")
    private List<GroupResultMonitoring> groupResultMonitorings = new ArrayList<>();

    public Group(String groupName, List<User> users, AgeGroup ageGroup, Cabinet cabinet) {
        this.groupName = groupName;
        this.users = users;
        this.ageGroup = ageGroup;
        this.cabinet = cabinet;
    }


    //Сортировка по дню недели и времени начала необходима для вывода расписания на форму
    public List<Lesson> getLessons() {
        Collections.sort(this.lessons, new Comparator<Lesson>() {
            @Override
            public int compare(Lesson o1, Lesson o2) {
                try {
                    int dayO1 = MyService.getDayOfWeek(o1.getDayWeek());
                    int dayO2 = MyService.getDayOfWeek(o2.getDayWeek());
                    if (dayO1 > dayO2) {
                        return 1;
                    } else {
                        if (dayO1 == dayO2) {
                            return o1.getTimeOfStartLesson().compareTo(o2.getTimeOfStartLesson());
                        } else {
                            return -1;
                        }
                    }
                } catch (Exception e) {
                    return 1;
                }
            }
        });
        return lessons;
    }

    //Принимаем индекс дня недели от 0 до 4 и какой урок в дне по счету
    public String getHourLesson(int dayIndex, int countLessonInDay) {

        try {
            String days = MyService.getDayOfWeek(dayIndex);
            Lesson lesson = getLessons().stream().filter(x -> x.getDayWeek().equals(days)).collect(Collectors.toList()).get(countLessonInDay);
            String hour = String.format("%02d", lesson.getTimeOfStartLesson().getHours());
            return hour;
        } catch (Exception e) {
            return "09";

        }

    }

    public String getHourEndLesson(int dayIndex, int countLessonInDay) {
        try {
            String days = MyService.getDayOfWeek(dayIndex);
            Lesson lesson = getLessons().stream().filter(x -> x.getDayWeek().equals(days)).collect(Collectors.toList()).get(countLessonInDay);
            String hour = String.format("%02d", lesson.getTimeOfEndLesson().getHours());
            return hour;
        } catch (Exception e) {
            return "09";

        }

    }

    public String getMinuteLesson(int dayIndex, int countLessonInDay) {
        try {
            String days = MyService.getDayOfWeek(dayIndex);
            Lesson lesson = getLessons().stream().filter(x -> x.getDayWeek().equals(days)).collect(Collectors.toList()).get(countLessonInDay);
            String minute = String.format("%02d", lesson.getTimeOfStartLesson().getMinutes());
            return minute;
        } catch (Exception e) {
            return "00";

        }
    }

    public String getMinuteEndLesson(int dayIndex, int countLessonInDay) {
        String days = MyService.getDayOfWeek(dayIndex);
        try {
            Lesson lesson = getLessons().stream().filter(x -> x.getDayWeek().equals(days)).collect(Collectors.toList()).get(countLessonInDay);
            String minute = String.format("%02d", lesson.getTimeOfEndLesson().getMinutes());
            return minute;
        } catch (Exception e) {
            return "00";
        }


    }
}
