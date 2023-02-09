package com.mai.Kindergarten.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "t_talk_manual")
public class TalkManual {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Column(length=100000)
    private byte[] file;
    private String fileName;
    @OneToOne
    @JoinColumn(name = "educationDirection_id")
    private EducationDirection educationDirection;

    public TalkManual(String name, byte[] file, String fileName, EducationDirection educationDirection) {
        this.name = name;
        this.file = file;
        this.fileName = fileName;
        this.educationDirection = educationDirection;
    }

}
