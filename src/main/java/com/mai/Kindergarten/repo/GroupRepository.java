package com.mai.Kindergarten.repo;

import com.mai.Kindergarten.models.Group;
import org.springframework.data.jpa.repository.JpaRepository;

//Класс для работы в БД с сущностью Group
public interface GroupRepository extends JpaRepository<Group, Long> {
    Group findByGroupName(String groupName);
}
