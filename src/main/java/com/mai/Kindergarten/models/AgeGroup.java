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
@Table(name = "t_age_group")
public class AgeGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String childrenMinAge;
    private String childrenMaxAge;
    private int durationLesson;
    private int maxDurationLessons;
    private int maxCountLesson;
//    @OneToMany(mappedBy = "ageGroup", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<Group> groups;

    public AgeGroup(String name, String childrenMinAge, String childrenMaxAge, int durationLesson, int maxDurationLessons) {
        this.name = name;
        this.childrenMaxAge = childrenMaxAge;
        this.childrenMinAge = childrenMinAge;
        this.durationLesson = durationLesson;
        this.maxDurationLessons = maxDurationLessons;
        this.maxCountLesson = maxDurationLessons / durationLesson;
    }
}
