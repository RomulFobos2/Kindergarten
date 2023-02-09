package com.mai.Kindergarten.repo;

import com.mai.Kindergarten.models.Manual;
import com.mai.Kindergarten.models.TalkManual;
import org.springframework.data.jpa.repository.JpaRepository;

//Класс для работы в БД с сущностью Diploma
public interface TalkManualRepository extends JpaRepository<TalkManual, Long> {
}
