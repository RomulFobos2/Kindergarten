package com.mai.Kindergarten.repo;

import com.mai.Kindergarten.models.AgeGroup;
import com.mai.Kindergarten.models.EducationDirection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

//Класс для работы в БД с сущностью DirectionEducation
public interface EducationDirectionRepository extends JpaRepository<EducationDirection, Long> {
//    @Query(value = "SELECT id FROM `t_education_direction` WHERE " +
//            "education_area_id = :education_area_id",nativeQuery = true)
//    EducationDirection findByAgeGroupId(@Param("education_area_id") Long education_area_id);

    @Query(value = "SELECT * FROM t_education_direction e WHERE e.name = :name and e.age_group_id = :age_group_id and e.education_area_id = :education_area_id",nativeQuery = true)
    EducationDirection findDateByUniqueId(@Param("name") String name, @Param("age_group_id") Long age_group_id, @Param("education_area_id") Long education_area_id);

    List<EducationDirection> findByAgeGroup(AgeGroup ageGroup);

//    @Query(value = "SELECT id FROM t_education_direction WHERE age_group_id = :age_group_id",nativeQuery = true)
//    @Query(value = "SELECT * FROM t_education_direction e WHERE e.age_group_id = :age_group_id",nativeQuery = true)
//    EducationDirection findDateByAgeGroupId(@Param("age_group_id") Long id);
//    EducationDirection findDateByAgeGroupId(Long id);

}
