package com.mai.Kindergarten.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "t_manual")
public class Manual {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String type;
    private String form;
    @Column(length=100000)
    private byte[] file;
    private String fileName;
    @OneToOne
    @JoinColumn(name = "educationDirection_id")
    private EducationDirection educationDirection;

    public Manual(String name, String type, String form, byte[] file, EducationDirection educationDirection, String fileName) {
        this.name = name;
        this.type = type;
        this.form = form;
        this.file = file;
        this.educationDirection = educationDirection;
        this.fileName = fileName;
    }
}
