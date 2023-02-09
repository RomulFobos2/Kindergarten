package com.mai.Kindergarten.repo;

import com.mai.Kindergarten.models.AgeGroup;
import com.mai.Kindergarten.models.Group;
import org.springframework.data.jpa.repository.JpaRepository;

//Класс для работы в БД с сущностью AgeGroup
public interface AgeGroupRepository extends JpaRepository<AgeGroup, Long> {
    AgeGroup findByName(String name);
}
