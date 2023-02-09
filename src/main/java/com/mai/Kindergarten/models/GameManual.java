package com.mai.Kindergarten.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "t_game_manual")
public class GameManual {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String type; //Игра: Дидактическая, Ситуационная, Игровое упражнение
    @Column(length=100000)
    private byte[] file;
    private String fileName;
    @OneToOne
    @JoinColumn(name = "educationDirection_id")
    private EducationDirection educationDirection;

    public GameManual(String name, String type, byte[] file, String fileName, EducationDirection educationDirection) {
        this.name = name;
        this.type = type;
        this.file = file;
        this.fileName = fileName;
        this.educationDirection = educationDirection;
    }
}
