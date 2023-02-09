package com.mai.Kindergarten.repo;

import com.mai.Kindergarten.models.Cabinet;
import org.springframework.data.jpa.repository.JpaRepository;

//Класс для работы в БД с сущностью Cabinet
public interface CabinetRepository extends JpaRepository<Cabinet, Long> {
    Cabinet findByNumber(String number);
}
