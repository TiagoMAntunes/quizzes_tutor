package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
<<<<<<< HEAD
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course;
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.domain.Tournament;

import javax.transaction.Transactional;
import java.util.Optional;
=======
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.domain.Tournament;

import javax.transaction.Transactional;
>>>>>>> 2800fbdedde5dd542ec11964dee1f1adedcad9ab

@Repository
@Transactional
public interface  TournamentRepository extends JpaRepository<Tournament, Integer> {
        @Query(value = "SELECT MAX(key) from tournaments", nativeQuery = true)
        Integer getMaxTournamentKey();
<<<<<<< HEAD
=======

>>>>>>> 2800fbdedde5dd542ec11964dee1f1adedcad9ab
}