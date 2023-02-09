package com.mai.Kindergarten.repo;

import com.mai.Kindergarten.models.Child;
import com.mai.Kindergarten.models.EducationDirection;
import com.mai.Kindergarten.models.IndividualSession;
import com.mai.Kindergarten.models.ResultMonitoring;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

//Класс для работы в БД с сущностью ResultMonitoring
public interface ResultMonitoringRepository extends JpaRepository<ResultMonitoring, Long> {
    List<ResultMonitoring> findByChild(Child child);
}
