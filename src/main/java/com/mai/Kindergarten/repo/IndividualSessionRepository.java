package com.mai.Kindergarten.repo;

import com.mai.Kindergarten.models.AgeGroup;
import com.mai.Kindergarten.models.Child;
import com.mai.Kindergarten.models.IndividualSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

//Класс для работы в БД с сущностью IndividualSession
public interface IndividualSessionRepository extends JpaRepository<IndividualSession, Long> {
    List<IndividualSession> findByChild(Child child);
}
