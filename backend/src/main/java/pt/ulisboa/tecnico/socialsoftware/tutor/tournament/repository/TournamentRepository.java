package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.domain.Tournament;
import javax.transaction.Transactional;

import java.util.Optional;

@Repository
@Transactional
public interface  TournamentRepository extends JpaRepository<Tournament, Integer> {
    @Query(value = "SELECT * FROM tournaments t WHERE t.quiz_id = :quizId", nativeQuery = true)
    Optional<Tournament> findTournamentFromQuizId(int quizId);

    
}
