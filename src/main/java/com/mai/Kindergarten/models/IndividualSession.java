package com.mai.Kindergarten.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.regexp.RE;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "t_individual_session")
public class IndividualSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private Child child;


    @OneToOne
    @JoinColumn(name = "resultMonitoring_id")
    private ResultMonitoring resultMonitoring;

    @OneToOne
    @JoinColumn(name = "educationArea_id")
    private EducationArea educationArea;

    @OneToOne
    @JoinColumn(name = "educationDirection_id")
    private EducationDirection educationDirection;

    private String typeMaterial;
    private String nameFileMaterials;
    private Date date;

    public IndividualSession(Child child, ResultMonitoring resultMonitoring, EducationArea educationArea, EducationDirection educationDirection, String typeMaterial, String nameFileMaterials, Date date) {
        this.child = child;
        this.resultMonitoring = resultMonitoring;
        this.educationArea = educationArea;
        this.educationDirection = educationDirection;
        this.typeMaterial = typeMaterial;
        this.nameFileMaterials = nameFileMaterials;
        this.date = date;
    }
}
