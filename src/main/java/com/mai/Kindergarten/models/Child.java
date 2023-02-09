package com.mai.Kindergarten.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "t_child")
public class Child {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String lastName; //Фамилия
    private String firstName; //Имя
    private String patronymicName; //Отчетство
    @Transient
    private String fullName;
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private Date dateOfBirth;
    private int age;


    //    @OneToMany
//    @JoinColumn(name = "child_id")
    @OneToMany(mappedBy = "child")
    private List<ResultMonitoring> resultMonitors = new ArrayList<>();

    @OneToMany(mappedBy = "child")
    private List<IndividualSession> individualSessions = new ArrayList<>();

//    @ManyToOne
//    @JoinColumn(name = "group_id")
//    private Group group;

    public Child(String lastName, String firstName, String patronymicName, Date dateOfBirth) {
        this.lastName = lastName;
        this.firstName = firstName;
        this.patronymicName = patronymicName;
        this.fullName = firstName + " " + lastName + " " + patronymicName;
        this.dateOfBirth = dateOfBirth;
        calculateAge(dateOfBirth);
    }

    public void calculateAge(Date dateOfBirth) {
        LocalDate today = LocalDate.now();
        Period period = Period.between(dateOfBirth.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(), today);
        this.age = period.getYears();
    }

    public void calculateAge() {
        LocalDate today = LocalDate.now();
        Period period = Period.between(dateOfBirth.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(), today);
        this.age = period.getYears();
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
        calculateAge(dateOfBirth);
    }

    public String getFullName() {
        return lastName + " " + firstName + " " + patronymicName;
    }

    public String getAgeInfo() {
        String strAge = String.valueOf(age);
        char lastNumber = strAge.charAt(strAge.length() - 1);
        switch (lastNumber) {
            case '0':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                strAge = strAge + " лет";
                break;
            case '1':
                strAge = strAge + " год";
                break;
            case '2':
            case '3':
            case '4':
                strAge = strAge + " года";
        }
        return strAge;
    }
}
