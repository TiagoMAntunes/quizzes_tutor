package pt.ulisboa.tecnico.socialsoftware.tutor.tournament;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.TopicConjunction;
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
    public TournamentDto createTournament(TournamentDto tournamentDto) {
        if (tournamentDto.getKey() == null)
            tournamentDto.setKey(getMaxTournamentKey() + 1);

        Set<Topic> topics_set = tournamentDto.getTopics().stream().map(topicDto -> topicRepository.findById(topicDto.getId()).orElseThrow()).collect(Collectors.toSet());

        List<Topic> topics = new ArrayList<>(topics_set);

        if (topics.size() == 0)
            throw new TutorException(ErrorMessage.NO_TOPICS_SELECTED);

        if (tournamentDto.getNumberOfQuestions() <= 0)
            throw new TutorException(ErrorMessage.TOURNAMENT_HAS_NO_QUESTIONS);

        Tournament tournament = new Tournament(tournamentDto, topics);
        if (tournament.getStartTime().isAfter(tournament.getFinishTime()))
            throw new TutorException(ErrorMessage.INVALID_TOURNAMENT_TIME);
        else if (tournament.getFinishTime().isBefore(LocalDateTime.now()))
            throw new TutorException(ErrorMessage.TOURNAMENT_ALREADY_FINISHED);

        entityManager.persist(tournament);
        return new TournamentDto(tournament);
    }


    private Integer getMaxTournamentKey() {
        Integer val = tournamentRepository.getMaxTournamentKey();
        return val != null ? val : 0;
    }
}