package it.polito.ai.laboratorio3.repositories;

import it.polito.ai.laboratorio3.entities.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.List;

@Repository
public interface TeamRepository extends JpaRepository<Team,Long> {
    @Query("SELECT team FROM Team team WHERE team.idCreator = :idC")
    @Transactional
    @Modifying
    List<Team> findTeamsByIdCreator(String idC);
    @Query("SELECT team FROM Team team WHERE team.name = :name")
    @Transactional
    @Modifying
    List<Team> findTeamsByName(String name);
}
