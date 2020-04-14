package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.domain.Tournament;
import javax.transaction.Transactional;


@Repository
@Transactional
public interface  TournamentRepository extends JpaRepository<Tournament, Integer> {
}
