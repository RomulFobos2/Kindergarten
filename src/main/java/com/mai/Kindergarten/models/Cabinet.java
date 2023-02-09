package com.mai.Kindergarten.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "t_cabinet")
public class Cabinet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String number;
    private boolean busy;
//    @OneToOne(mappedBy = "ageGroup", cascade = CascadeType.ALL, orphanRemoval = true)
//    private Group group;

    public Cabinet(String number) {
        this.number = number;
    }
}
