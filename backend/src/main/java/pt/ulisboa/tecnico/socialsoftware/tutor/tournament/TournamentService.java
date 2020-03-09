package pt.ulisboa.tecnico.socialsoftware.tutor.tournament;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.TopicRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.domain.Tournament;
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto.TournamentDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.repository.TournamentRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import static java.util.Comparator.*;

@Service
public class TournamentService {

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private UserRepository userRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Retryable(
        value = {SQLException.class },
        backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public TournamentDto createTournament(TournamentDto tournamentDto, CourseExecution courseExecution) {
        if (tournamentDto.getKey() == null)
            tournamentDto.setKey(getMaxTournamentKey() + 1);

        Set<Topic> topicsSet = tournamentDto.getTopics().stream().map(topicDto -> topicRepository.findById(topicDto.getId()).orElseThrow()).collect(Collectors.toSet());

        List<Topic> topics = new ArrayList<>(topicsSet);

        if (topics.size() == 0)
            throw new TutorException(ErrorMessage.NO_TOPICS_SELECTED);

        if (tournamentDto.getNumberOfQuestions() <= 0)
            throw new TutorException(ErrorMessage.TOURNAMENT_HAS_NO_QUESTIONS);

        Tournament tournament = new Tournament(tournamentDto, topics);
        if (tournament.getStartTime().isAfter(tournament.getFinishTime()))
            throw new TutorException(ErrorMessage.INVALID_TOURNAMENT_TIME);
        else if (tournament.getFinishTime().isBefore(LocalDateTime.now()))
            throw new TutorException(ErrorMessage.TOURNAMENT_ALREADY_FINISHED);

        for (Topic t : topics) 
            t.addTournament(tournament);
        
        courseExecution.addTournament(tournament);
        tournament.setCourseExecution(courseExecution);

        entityManager.persist(tournament);
        return new TournamentDto(tournament);
    }

    public List<Tournament> getOpenTournaments(CourseExecution courseExecution){
        LocalDateTime now = LocalDateTime.now();

        return tournamentRepository.findAll().stream()
                .filter(tournament -> tournament.getCourseExecution().getId() == courseExecution.getId())
                .filter(tournament -> tournament.getStartTime().isBefore(now))
                .filter(tournament -> tournament.getFinishTime().isAfter(now))
                .sorted(comparing(Tournament::getStartTime).reversed())
                .collect(Collectors.toList());
    }


    private Integer getMaxTournamentKey() {
        Integer val = tournamentRepository.getMaxTournamentKey();
        return val != null ? val : 0;
    }
}
