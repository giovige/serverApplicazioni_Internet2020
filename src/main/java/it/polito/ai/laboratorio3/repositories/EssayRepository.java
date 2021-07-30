package it.polito.ai.laboratorio3.repositories;

import it.polito.ai.laboratorio3.entities.Essay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EssayRepository extends JpaRepository<Essay, Long> {
}
