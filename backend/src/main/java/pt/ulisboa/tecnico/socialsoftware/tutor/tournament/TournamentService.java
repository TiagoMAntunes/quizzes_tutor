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
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
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
    public TournamentDto createTournament(TournamentDto tournamentDto, CourseExecution courseExecution, int creatorId) {
        if (tournamentDto.getKey() == null)
            tournamentDto.setKey(getMaxTournamentKey() + 1);

        Set<Topic> topicsSet = tournamentDto.getTopics().stream().map(topicDto -> topicRepository.findById(topicDto.getId()).orElseThrow()).collect(Collectors.toSet());

        List<Topic> topics = new ArrayList<>(topicsSet);

        if (topics.isEmpty())
            throw new TutorException(ErrorMessage.NO_TOPICS_SELECTED);

        if (tournamentDto.getNumberOfQuestions() <= 0)
            throw new TutorException(ErrorMessage.TOURNAMENT_HAS_NO_QUESTIONS);

        User creator = userRepository.findById(creatorId).orElseThrow(() -> new TutorException(ErrorMessage.USER_NOT_FOUND, creatorId));

        Tournament tournament = new Tournament(tournamentDto, topics, creator);

        if (tournament.getStartTime().isAfter(tournament.getFinishTime()) || tournament.getStartTime().isEqual(tournament.getFinishTime()))
            throw new TutorException(ErrorMessage.INVALID_TOURNAMENT_TIME);
        else if (tournament.getStartTime().isBefore(LocalDateTime.now()))
            throw new TutorException(ErrorMessage.TOURNAMENT_ALREADY_STARTED);

        topics.forEach(topic -> topic.addTournament(tournament));

        //Add tournament to course execution
        courseExecution.addTournament(tournament);
        tournament.setCourseExecution(courseExecution);

        //Set the creator
        creator.addCreatedTournament(tournament);
        tournament.setCreator(creator);

        entityManager.persist(tournament);
        return new TournamentDto(tournament);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public List<TournamentDto> getOpenTournaments(Integer courseExecutionId){
        return tournamentRepository.findAll().stream()
                .filter(tournament -> tournamentIsOpen(tournament.getId(), courseExecutionId))
                .map(TournamentDto::new)
                .collect(Collectors.toList());
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public boolean tournamentIsOpen(Integer tournamentId, Integer courseExecutionId){
        LocalDateTime now = LocalDateTime.now();
        Tournament tournament = getTournament(tournamentId);
        return (tournament.getCourseExecution().getId().equals(courseExecutionId)) &&
                tournament.getFinishTime().isAfter(now);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void joinTournament(Integer tournamentId, Integer courseExecutionId, Integer userId){
        Tournament tournament = getTournament(tournamentId);
        User user = getUser(userId);

        if(!tournamentIsOpen(tournamentId, courseExecutionId))
            throw new TutorException(ErrorMessage.TOURNAMENT_NOT_OPEN);

        if(tournament.hasSignedUp(user))
            throw new TutorException(ErrorMessage.TOURNAMENT_ALREADY_JOINED);

        tournament.signUp(user);
        user.addTournament(tournament);
    }

    private User getUser(Integer userId) {
        return userRepository.findById(userId).orElseThrow(() -> new TutorException(ErrorMessage.USER_NOT_FOUND, userId));
    }

    private Tournament getTournament(Integer tournamentId) {
        return tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new TutorException(ErrorMessage.TOURNAMENT_NOT_FOUND, tournamentId));
    }


    private Integer getMaxTournamentKey() {
        Integer val = tournamentRepository.getMaxTournamentKey();
        return val != null ? val : 0;
    }
}
