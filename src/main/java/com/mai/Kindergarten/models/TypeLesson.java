package com.mai.Kindergarten.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "t_type_lesson")
public class TypeLesson {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
//    @OneToMany(mappedBy = "typeLesson", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<Lesson> lessons;
    @ManyToOne
    @JoinColumn(name = "educationDirection_id")
    private EducationDirection educationDirection;

}
