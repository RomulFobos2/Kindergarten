package com.mai.Kindergarten.repo;

import com.mai.Kindergarten.models.Manual;
import org.springframework.data.jpa.repository.JpaRepository;

//Класс для работы в БД с сущностью Diploma
public interface ManualRepository extends JpaRepository<Manual, Long> {
}
