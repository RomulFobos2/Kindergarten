package com.mai.Kindergarten.repo;

import com.mai.Kindergarten.models.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

//Класс для работы в БД с сущностью Lesson
public interface LessonRepository extends JpaRepository<Lesson, Long> {
    Lesson findByDayWeek(String dayWeek);
}
