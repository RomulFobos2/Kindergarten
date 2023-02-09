package com.mai.Kindergarten.repo;

import com.mai.Kindergarten.models.TypeLesson;
import org.springframework.data.repository.CrudRepository;

//Класс для работы в БД с сущностью TypeLesson
public interface TypeLessonRepository extends CrudRepository<TypeLesson, Long> {
}
