package pt.ulisboa.tecnico.socialsoftware.tutor.tournament;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository;
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

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.QUIZ_NOT_FOUND;
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.TOURNAMENT_NOT_FOUND;

@Service
public class TournamentService {

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseExecutionRepository courseExecutionRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Retryable(
        value = {SQLException.class },
        backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public TournamentDto createTournament(TournamentDto tournamentDto, int courseExecutionId, int creatorId) {
        if (tournamentDto.getTopics() == null)
            throw new TutorException(ErrorMessage.NO_TOPICS_SELECTED);

        Set<Topic> topicsSet = tournamentDto.getTopics().stream().map(topicDto -> topicRepository.findById(topicDto.getId()).orElseThrow(() -> new TutorException(ErrorMessage.TOPIC_NOT_FOUND, topicDto.getId()))).collect(Collectors.toSet());

        List<Topic> topics = new ArrayList<>(topicsSet);

        if (topics.isEmpty())
            throw new TutorException(ErrorMessage.NO_TOPICS_SELECTED);

        if (tournamentDto.getNumberOfQuestions() <= 0)
            throw new TutorException(ErrorMessage.TOURNAMENT_HAS_NO_QUESTIONS);

        User creator = userRepository.findById(creatorId).orElseThrow(() -> new TutorException(ErrorMessage.USER_NOT_FOUND, creatorId));

        if (creator.getRole() != User.Role.STUDENT)
            throw new TutorException(ErrorMessage.TOURNAMENT_CREATION_INCORRECT_ROLE);

        CourseExecution courseExecution = courseExecutionRepository.findById(courseExecutionId).orElseThrow(() -> new TutorException(ErrorMessage.COURSE_EXECUTION_NOT_FOUND, courseExecutionId));

        Tournament tournament = new Tournament(tournamentDto, topics, creator);

        checkTournamentTimes(tournament);

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

    private void checkTournamentTimes(Tournament tournament) {
        if (tournament.getStartTime().isAfter(tournament.getFinishTime()) || tournament.getStartTime().isEqual(tournament.getFinishTime()))
            throw new TutorException(ErrorMessage.INVALID_TOURNAMENT_TIME);
        else if (tournament.getStartTime().isBefore(LocalDateTime.now()))
            throw new TutorException(ErrorMessage.TOURNAMENT_ALREADY_STARTED);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void cancelTournament(Integer tournamentId, Integer userId) {
        Tournament tournament = tournamentRepository.findById(tournamentId).orElseThrow(() -> new TutorException(ErrorMessage.TOURNAMENT_NOT_FOUND, tournamentId));

        if (!tournament.getCreator().getId().equals(userId))
            throw new TutorException(ErrorMessage.TOURNAMENT_USER_IS_NOT_THE_CREATOR);

        if (tournament.getStartTime().isBefore(LocalDateTime.now()))
            throw new TutorException(ErrorMessage.TOURNAMENT_HAS_STARTED);

        tournament.cancel();
        entityManager.remove(tournament);
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
                tournament.getFinishTime().isAfter(now) &&
                tournament.getStartTime().isAfter(now);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void joinTournament(Integer tournamentId, Integer courseExecutionId, Integer userId){
        Tournament tournament = getTournament(tournamentId);
        User user = getUser(userId);

        checkIsStudentRole(user);
        checkTournamentIsOpen(tournamentId, courseExecutionId);
        checkNotSignedUpYet(tournament, user);

        tournament.signUp(user);
        user.addTournament(tournament);
    }

    public int getTournamentSignedUpNumber(Integer tournamentId){
        return getTournament(tournamentId).getSignedUpNumber();
    }

    private void checkNotSignedUpYet(Tournament tournament, User user) {
        if(tournament.hasSignedUp(user))
            throw new TutorException(ErrorMessage.TOURNAMENT_ALREADY_JOINED);
    }

    private void checkTournamentIsOpen(Integer tournamentId, Integer courseExecutionId) {
        if(!tournamentIsOpen(tournamentId, courseExecutionId))
            throw new TutorException(ErrorMessage.TOURNAMENT_NOT_OPEN);
    }

    private void checkIsStudentRole(User user) {
        if (user.getRole() != User.Role.STUDENT)
            throw new TutorException(ErrorMessage.TOURNAMENT_JOIN_WRONG_ROLE);
    }

    private User getUser(Integer userId) {
        return userRepository.findById(userId).orElseThrow(() -> new TutorException(ErrorMessage.USER_NOT_FOUND, userId));
    }

    private Tournament getTournament(Integer tournamentId) {
        return tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new TutorException(ErrorMessage.TOURNAMENT_NOT_FOUND, tournamentId));
    }
}
