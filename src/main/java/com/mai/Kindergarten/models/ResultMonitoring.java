package com.mai.Kindergarten.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "t_result_monitoring")
public class ResultMonitoring {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne
    @JoinColumn(name = "group_id")
    private Group group; //группа в которой был ребенок на момент выставления оценки
    @OneToOne
    @JoinColumn(name = "educationDirection_id")
    private EducationDirection educationDirection; //направление развития
//    @OneToOne
//    @JoinColumn(name = "educationIndicator_id")
//    private EducationIndicator educationIndicator; //какой то один конкретный вопрос
    private String question;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user; //воспитатель поставившей оценку

    private int rating; //Оценка
    private boolean isStarEducationYear; //true начало годаю, false конец
    private Date date; //дата выставления оценка

    @ManyToOne
    private Child child;

    public ResultMonitoring(Group group, EducationDirection educationDirection, User user, EducationIndicator educationIndicator, int rating, boolean isStarEducationYear, Date date, Child child) {
        this.group = group;
        this.educationDirection = educationDirection;
        this.user = user;
//        this.educationIndicator = educationIndicator;
        this.question = educationIndicator.getQuestion();
        this.rating = rating;
        this.isStarEducationYear = isStarEducationYear;
        this.date = date;
        this.child = child;
    }

}
