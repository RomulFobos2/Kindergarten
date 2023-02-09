package com.mai.Kindergarten.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "t_education_direction")
public class EducationDirection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @OneToOne
    @JoinColumn(name = "educationArea_id")
    private EducationArea educationArea;
    @OneToMany
    @JoinColumn(name = "educationDirection_id")
    private List<EducationIndicator> educationIndicators;
    @OneToOne
    @JoinColumn(name = "ageGroup_id")
    private AgeGroup ageGroup;
//    @OneToMany(mappedBy = "directionEducation", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<TypeLesson> typeLessons;
    private boolean haveLesson; //Необходимо ли отображения направления развития для уроков при формировании расписания
    private boolean isHard; //Сложное занятие или нет. Сложное не ставится в ПН и в ПТ (В понедельник и пятницу не ставятся: математическое развитие (ФЭМП), основы науки и естествознания (ознакомления с природным, предметным, социальным миром), развитие речи
    private boolean isActivity; //Активное занятие или нет (true активное)
    private int countLesson; //Кол-во занятий в неделю

    @OneToOne
    @JoinColumn(name = "educationDirectionTotalCountLesson_id")
    private EducationDirection educationDirectionTotalCountLesson; //Общее кол-во уроков с...


    public EducationDirection(String name, EducationArea educationArea, List<EducationIndicator> educationIndicators, AgeGroup ageGroup,
                              boolean haveLesson, boolean isHard, boolean isActivity, int countLesson, EducationDirection educationDirectionTotalCountLesson) {
        this.name = name;
        this.educationArea = educationArea;
        this.educationIndicators = educationIndicators;
        this.ageGroup = ageGroup;
        this.haveLesson = haveLesson;
        this.isHard = isHard;
        this.isActivity = isActivity;
        this.countLesson = countLesson;
        this.educationDirectionTotalCountLesson = educationDirectionTotalCountLesson;
    }



}
