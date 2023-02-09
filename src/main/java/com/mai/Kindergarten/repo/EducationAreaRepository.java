package com.mai.Kindergarten.repo;

import com.mai.Kindergarten.models.EducationArea;
import com.mai.Kindergarten.models.Group;
import org.springframework.data.jpa.repository.JpaRepository;

//Класс для работы в БД с сущностью EducationArea
public interface EducationAreaRepository extends JpaRepository<EducationArea, Long> {
    EducationArea findByName(String name);
}
