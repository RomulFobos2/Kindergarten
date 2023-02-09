package com.mai.Kindergarten.repo;

import com.mai.Kindergarten.models.Child;
import com.mai.Kindergarten.models.EducationDirection;
import com.mai.Kindergarten.models.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

//Класс для работы в БД с сущностью Child
public interface ChildRepository extends JpaRepository<Child, Long> {
    Child findByLastName(String lastName);

    @Query(value = "SELECT group_id FROM t_child e WHERE e.id = :id",nativeQuery = true)
    Long findByChildId(@Param("id") Long id);
}
