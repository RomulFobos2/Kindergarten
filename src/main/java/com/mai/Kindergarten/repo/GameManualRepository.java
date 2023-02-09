package com.mai.Kindergarten.repo;

import com.mai.Kindergarten.models.GameManual;
import com.mai.Kindergarten.models.Manual;
import org.springframework.data.jpa.repository.JpaRepository;

//Класс для работы в БД с сущностью Diploma
public interface GameManualRepository extends JpaRepository<GameManual, Long> {
}
