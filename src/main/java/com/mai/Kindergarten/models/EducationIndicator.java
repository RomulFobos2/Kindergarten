package com.mai.Kindergarten.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "t_education_indicator")
public class EducationIndicator {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length=100000)
    private String question;

    public EducationIndicator(String question) {
        this.question = question;
    }
}
