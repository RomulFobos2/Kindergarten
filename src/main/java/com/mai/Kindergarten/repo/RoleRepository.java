package com.mai.Kindergarten.repo;

import com.mai.Kindergarten.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;

//Класс для работы в БД с сущностью Role
public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByName(String name);
}
