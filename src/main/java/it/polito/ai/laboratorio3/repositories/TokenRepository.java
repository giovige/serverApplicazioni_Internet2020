package it.polito.ai.laboratorio3.repositories;

import it.polito.ai.laboratorio3.entities.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.List;

@Repository
public interface TokenRepository extends JpaRepository<Token,String> {
    @Query("SELECT token FROM Token token WHERE token.expiryDate > :t")
    List<Token> findAllByExpiryBefore(Timestamp t);

    @Query("SELECT token FROM Token token WHERE token.teamId = :teamid")
    List<Token> findTokenByTeamId(Long teamid);

    @Query("DELETE FROM Token token WHERE token.expiryDate < :t")
    @Transactional
    @Modifying
    void deleteExpiredToken(Timestamp t);

    @Query("DELETE FROM Token token WHERE token.teamId = :teamid")
    @Transactional
    @Modifying
    void deleteTokenAfterActiveTeam(Long teamid);

    @Query("SELECT token FROM Token token WHERE token.teamId=:teamId")
    List<Token>findAllByTeamId(Long teamId);

}
