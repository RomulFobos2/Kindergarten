package com.mai.Kindergarten.repo;

import com.mai.Kindergarten.models.Child;
import com.mai.Kindergarten.models.Diploma;
import com.mai.Kindergarten.models.ResultMonitoring;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

//Класс для работы в БД с сущностью Diploma
public interface DiplomaRepository extends JpaRepository<Diploma, Long> {
    List<Diploma> findByChild(Child child);
}
