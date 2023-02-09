package com.mai.Kindergarten.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "t_user")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username; //Адрес почты, можно будет поменять на телефон
    private String password;
    @Transient
    private String passwordConfirm;
    private String lastName; //Фамилия
    private String firstName; //Имя
    private String patronymicName; //Отчетство
    private boolean needChangePass = true; //Необходимость смены пароля
    @Transient
    private String fullName;

//    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<Diploma> diplomas;

    @OneToMany(mappedBy = "user")
    private List<Diploma> diplomas;

    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Role> roles;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;

//    @Transient
//    @ManyToMany(mappedBy = "users")
//    private Group group;  //2вариант с сущностью Group

    public User(String username, String password, String passwordConfirm, String lastName, String firstName, String patronymicName) {
        this.username = username;
        this.password = password;
        this.passwordConfirm = passwordConfirm;
        this.lastName = lastName;
        this.firstName = firstName;
        this.patronymicName = patronymicName;
        this.fullName = firstName + " " + lastName + " " + patronymicName;
    }


    public String getFullName() {
        return lastName + " " + firstName + " " + patronymicName;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return getRoles();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}
