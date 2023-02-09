package com.mai.Kindergarten.repo;

import com.mai.Kindergarten.models.GroupResultMonitoring;
import com.mai.Kindergarten.models.ResultMonitoring;
import org.springframework.data.jpa.repository.JpaRepository;

//Класс для работы в БД с сущностью GroupResultMonitoring
public interface GroupResultMonitoringRepository extends JpaRepository<GroupResultMonitoring, Long> {
}
