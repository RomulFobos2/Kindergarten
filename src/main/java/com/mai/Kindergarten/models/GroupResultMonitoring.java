package com.mai.Kindergarten.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "t_group_result_monitoring")
public class GroupResultMonitoring {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne
    @JoinColumn(name = "educationDirection_id")
    private EducationDirection educationDirection; //направление развития

    private boolean isStarEducationYear; //true начало годаю, false конец
    private String highLevel;
    private String midLevel;
    private String lowLevel;
    private String countAvailableChild; //Кол-во детей которое прошло мониторинг
    private String countAllChild; //Кол-во детей которое числилось в группе на момент мониторинга

    public GroupResultMonitoring(EducationDirection educationDirection, boolean isStarEducationYear, String highLevel, String midLevel, String lowLevel, String countAvailableChild, String countAllChild) {
        this.educationDirection = educationDirection;
        this.isStarEducationYear = isStarEducationYear;
        this.highLevel = highLevel;
        this.midLevel = midLevel;
        this.lowLevel = lowLevel;
        this.countAvailableChild = countAvailableChild;
        this.countAllChild = countAllChild;
    }
}
