package com.mai.Kindergarten.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "t_lesson")
public class Lesson {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String dayWeek;
    @DateTimeFormat(pattern = "HH:mm")
    private Date timeOfStartLesson;
    @DateTimeFormat(pattern = "HH:mm")
    private Date timeOfEndLesson;
    @OneToOne
    @JoinColumn(name = "educationDirection_id")
    private EducationDirection educationDirection;
    @ManyToOne
    private Group group;

    public Lesson(EducationDirection educationDirection, Group group, String name) {
        this.educationDirection = educationDirection;
        this.group = group;
        this.name = name;
    }

//    //Для мызыки костыль
//    public Lesson(String lessonMusic, Group group) {
//        this.group = group;
//        this.lessonName = lessonMusic;
//    }
}
