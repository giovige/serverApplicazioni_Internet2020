package it.polito.ai.laboratorio3.repositories;

import it.polito.ai.laboratorio3.entities.Docente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocenteRepository  extends JpaRepository<Docente,String> {
}
