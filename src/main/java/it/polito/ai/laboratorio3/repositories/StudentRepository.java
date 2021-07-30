package it.polito.ai.laboratorio3.repositories;

import it.polito.ai.laboratorio3.entities.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepository extends JpaRepository<Student,String> {
}
