package pt.ulisboa.tecnico.socialsoftware.tutor.tournament;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import pt.ulisboa.tecnico.socialsoftware.tutor.config.DateHandler;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.TopicRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.QuizService;
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz;
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.dto.QuizDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.repository.QuizRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.domain.Tournament;
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto.TournamentDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto.TournamentScoreboardDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.repository.TournamentRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.sql.SQLException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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

    @Autowired
    private QuizService quizService;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Retryable(
        value = {SQLException.class },
        backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public TournamentDto createTournament(TournamentDto tournamentDto, int courseExecutionId, int creatorId) {
        if (tournamentDto.getTopics() == null)
            throw new TutorException(ErrorMessage.NO_TOPICS_SELECTED);

        Set<Topic> topics = tournamentDto.getTopics().stream().map(topicDto -> topicRepository.findById(topicDto.getId()).orElseThrow(() -> new TutorException(ErrorMessage.TOPIC_NOT_FOUND, topicDto.getId()))).collect(Collectors.toSet());

        if (topics.isEmpty())
            throw new TutorException(ErrorMessage.NO_TOPICS_SELECTED);

        if (tournamentDto.getNumberOfQuestions() <= 0)
            throw new TutorException(ErrorMessage.TOURNAMENT_HAS_NO_QUESTIONS);

        if ( DateHandler.toLocalDateTime(tournamentDto.getStartTime()) == null )
            throw new TutorException(ErrorMessage.TOURNAMENT_INVALID_START_TIME);

        if ( DateHandler.toLocalDateTime(tournamentDto.getFinishTime()) == null )
            throw new TutorException(ErrorMessage.TOURNAMENT_INVALID_FINISH_TIME);

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
        else if (tournament.getStartTime().isBefore(DateHandler.now()))
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

        if (tournament.getStartTime().isBefore(DateHandler.now()))
            throw new TutorException(ErrorMessage.TOURNAMENT_HAS_STARTED);

        tournament.cancel();
        entityManager.remove(tournament);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public List<TournamentDto> getOpenTournaments(Integer courseExecutionId, Integer userId) {
        return tournamentRepository.findAll().stream()
                .filter(tournament -> tournament.getCourseExecution().getId().equals(courseExecutionId)
                && tournamentIsOpen(tournament.getId()))
                .map( tournament -> new TournamentDto(tournament, userId) )
                .collect(Collectors.toList());
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public boolean tournamentIsOpen(Integer tournamentId) {
        LocalDateTime now = DateHandler.now();
        Tournament tournament = getTournament(tournamentId);
        return (tournament.getFinishTime().isAfter(now) &&
                tournament.getStartTime().isAfter(now));
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public TournamentDto joinTournament(Integer tournamentId, Integer userId){
        Tournament tournament = getTournament(tournamentId);
        User user = getUser(userId);

        checkIsStudentRole(user);
        checkSameCourseExecution(user, tournament);
        checkTournamentIsOpen(tournamentId);
        checkNotSignedUpYet(tournament, user);

        tournament.signUp(user);
        user.addTournament(tournament);

        if(tournament.getSignedUpNumber() == 2)
            this.generateTournamentQuiz(tournament);

        return new TournamentDto(tournament, userId);
    }

    private void generateTournamentQuiz(Tournament tournament) {
        //Generate quiz
        QuizDto quiz = new QuizDto();
        quiz.setType(Quiz.QuizType.TOURNAMENT.toString());
        quiz.setAvailableDate(DateHandler.toISOString(tournament.getStartTime()));
        quiz.setConclusionDate(DateHandler.toISOString(tournament.getFinishTime()));
        quiz.setScramble(true);
        quiz.setTitle(tournament.getTitle());
        quiz.setKey(quizService.getMaxQuizKey() + 1);

        CourseExecution courseExecution = tournament.getCourseExecution();

        // Get all available questions with one of the given topics at least
        List<Question> availableQuestions = questionRepository.findAvailableQuestions(courseExecution.getCourse().getId())
                .stream().filter(question -> tournament.getTopics().stream()
                        .anyMatch(topic -> question.getTopics().contains(topic))).collect(Collectors.toList());

        Collections.shuffle(availableQuestions); //Random selection of questions

        // Validate enough questions
        if (availableQuestions.size() < tournament.getNumberOfQuestions())
            tournament.setNumberOfQuestions(availableQuestions.size());

        ArrayList<QuestionDto> quizQuestions = new ArrayList<>();
        int i = 0;
        for (Question q : availableQuestions) {
            if (i++ == tournament.getNumberOfQuestions())
                break;
            QuestionDto question = new QuestionDto(q);
            question.setSequence(i); // if we don't add this, it will crash because it doesn't come from frontend
            quizQuestions.add(question);

        }
        quiz.setQuestions(quizQuestions);

        quizService.createQuiz(courseExecution.getId(), quiz);
        tournament.setQuiz(quizRepository.findByKey(quiz.getKey()).get()); // No need to throw as we just created it
    }

    public int getTournamentSignedUpNumber(Integer tournamentId) {
        return getTournament(tournamentId).getSignedUpNumber();
    }

    private int getTournamentNumberOfQuestions(Integer tournamentId){
        return getTournament(tournamentId).getNumberOfQuestions();
    }

    private String getTournamentTitle(Integer tournamentId){
        return getTournament(tournamentId).getTitle();
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public TournamentScoreboardDto getTournamentScoreboard(Integer tournamentId){
        TournamentScoreboardDto scoreboard = new TournamentScoreboardDto();

        scoreboard.setScores(getAllTournamentScores(tournamentId));
        scoreboard.setAverageScore(getTournamentAverageScore(tournamentId));
        scoreboard.setNumberOfParticipants(getTournamentSignedUpNumber(tournamentId));
        scoreboard.setNumberOfQuestions(getTournamentNumberOfQuestions(tournamentId));
        scoreboard.setTournamentTitle(getTournamentTitle(tournamentId));

        return scoreboard;
    }

    private List<Integer> getAllTournamentScores(Integer tournamentId){
        Tournament tournament = getTournament(tournamentId);
        Integer executionId = tournament.getCourseExecution().getId();
        Quiz quiz = tournament.getQuiz();

        List<Integer> scores = new ArrayList<>();

        if(quiz != null) {
            quiz.getQuizAnswers().stream()
                    .filter(quizAnswer -> quizAnswer.canResultsBePublic(executionId))
                    .forEach(quizAnswer -> scores.add(quizAnswer.getScore()));
        }

        return scores;
    }

    private float getTournamentAverageScore(Integer tournamentId){
        List<Integer> scores = getAllTournamentScores(tournamentId);

        float size = scores.size();
        if(size == 0) return 0;

        float total = scores.stream().reduce(0, Integer::sum);

        return total/size;
    }


    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public CourseDto findTournamentCourseExecution(int id) {
        return this.tournamentRepository.findById(id)
                .map(Tournament::getCourseExecution)
                .map(CourseDto::new)
                .orElseThrow(() -> new TutorException(TOURNAMENT_NOT_FOUND, id));
    }

    private void checkNotSignedUpYet(Tournament tournament, User user) {
        if(tournament.hasSignedUp(user))
            throw new TutorException(ErrorMessage.TOURNAMENT_ALREADY_JOINED);
    }

    private void checkTournamentIsOpen(Integer tournamentId) {
        if(!tournamentIsOpen(tournamentId))
            throw new TutorException(ErrorMessage.TOURNAMENT_NOT_OPEN);
    }

    private void checkIsStudentRole(User user) {
        if (user.getRole() != User.Role.STUDENT)
            throw new TutorException(ErrorMessage.TOURNAMENT_JOIN_WRONG_ROLE);
    }

    private void checkSameCourseExecution(User user, Tournament tournament){
        if(!user.hasCourseExecution(tournament.getCourseExecution().getId()))
            throw new TutorException(ErrorMessage.TOURNAMENT_DIFF_COURSE_EXEC);
    }

    private User getUser(Integer userId) {
        return userRepository.findById(userId).orElseThrow(() -> new TutorException(ErrorMessage.USER_NOT_FOUND, userId));
    }

    private Tournament getTournament(Integer tournamentId) {
        return tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new TutorException(ErrorMessage.TOURNAMENT_NOT_FOUND, tournamentId));
    }
}
