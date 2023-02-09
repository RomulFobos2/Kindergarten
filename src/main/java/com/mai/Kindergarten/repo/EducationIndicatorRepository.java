package com.mai.Kindergarten.repo;

import com.mai.Kindergarten.models.EducationIndicator;
import org.springframework.data.jpa.repository.JpaRepository;

//Класс для работы в БД с сущностью IndicatorEducation
public interface EducationIndicatorRepository extends JpaRepository<EducationIndicator, Long> {
    EducationIndicator findByQuestion(String question);
}
