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
@Table(name = "t_diploma")
public class Diploma {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String lastName; //Фамилия
    private String firstName; //Имя
    private String patronymicName; //Отчетство
    private String text; //Название конкурса, номинации
    private String place; //Место
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    private Date date;
    @Column(length=100000)
    private byte[] file;
    private String fileName;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    @OneToOne
    @JoinColumn(name = "child_id")
    private Child child;

    public Diploma(String lastName, String firstName, String patronymicName, String text, String place, Date date, byte[] file, String fileName, User user) {
        this.lastName = lastName;
        this.firstName = firstName;
        this.patronymicName = patronymicName;
        this.text = text;
        this.place = place;
        this.date = date;
        this.file = file;
        this.fileName = fileName;
        this.user = user;
    }
    public Diploma(String lastName, String firstName, String patronymicName, String text, String place, Date date, byte[] file, String fileName, User user, Child child) {
        this.lastName = lastName;
        this.firstName = firstName;
        this.patronymicName = patronymicName;
        this.text = text;
        this.place = place;
        this.date = date;
        this.file = file;
        this.fileName = fileName;
        this.user = user;
        this.child = child;
    }
}
