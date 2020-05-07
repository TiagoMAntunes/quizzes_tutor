package pt.ulisboa.tecnico.socialsoftware.tutor.dashboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.repository.QuizAnswerRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.StudentQuestionRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz;
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.domain.Tournament;
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.repository.TournamentRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Objects;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.ACCESS_DENIED;

@Service
public class DashboardService {
    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QuizAnswerRepository quizAnswerRepository;

    @Autowired
    private StudentQuestionRepository studentQuestionRepository;

    @Autowired
    private CourseExecutionRepository courseExecutionRepository;


    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public DashboardDto getStudentDashboard(Integer userId, Integer execId){
        DashboardDto dashboard = new DashboardDto();

        Integer courseId = getCourseExecution(execId).getCourse().getId();

        dashboard.setNumberQuestionsSubmitted(findNumberStudentQuestionsSubmitted(userId, courseId));
        dashboard.setNumberQuestionsApproved(findNumberStudentQuestionsApproved(userId, courseId));
        dashboard.setCreatedTournaments(getCreatedTournamentsNumber(userId, execId));
        dashboard.setParticipatedTournamentsNumber(getParticipatedTournamentsNumber(userId, execId));
        dashboard.setNotYetParticipatedTournamentsNumber(getNotYetParticipatedTournamentsNumber(userId, execId));
        dashboard.setAverageTournamentScore(getAverageTournamentScore(userId, execId));

        return dashboard;
    }

    private int  findNumberStudentQuestionsSubmitted(int studentId, int courseId) {
        User user = userRepository.findById(studentId).orElseThrow(() -> new TutorException(ACCESS_DENIED, studentId));
        checkRoleStudent(user);
        return studentQuestionRepository.findNumberStudentQuestionsSubmitted(user.getId(), courseId);
    }

    private int  findNumberStudentQuestionsApproved(int studentId, int courseId) {
        User user = userRepository.findById(studentId).orElseThrow(() -> new TutorException(ACCESS_DENIED, studentId));
        checkRoleStudent(user);
        return studentQuestionRepository.findNumberStudentQuestionsApproved(user.getId(), courseId);
    }

    private void checkRoleStudent(User student) {
        if(student.getRole() != User.Role.STUDENT){
            throw new TutorException(ACCESS_DENIED);
        }
    }

    public int getCreatedTournamentsNumber(Integer userId, Integer executionId) {
        return getUser(userId).getCreatedTournamentsNumber(executionId);
    }

    public int getParticipatedTournamentsNumber(Integer userId, Integer executionId){
        User user = getUser(userId);
        return (int) user.getSignedUpTournamentsCourseExec(executionId).stream()
                .filter(tournament -> hasParticipatedInTournament(user, tournament)).count();
    }

    public int getNotYetParticipatedTournamentsNumber(Integer userId, Integer executionId) {
        User user = getUser(userId);
        return (int) user.getSignedUpTournamentsCourseExec(executionId).stream()
                .filter(tournament -> canParticipateInTournament(user, tournament)).count();
    }

    private float getAverageTournamentScore(Integer userId, Integer executionId){
        User user = getUser(userId);
        int correctTournamentAnswers = (int) user.getQuizAnswers().stream()
                .filter(quizAnswer -> quizAnswer.canResultsBePublic(executionId) && quizAnswer.getQuiz().getType().equals(Quiz.QuizType.TOURNAMENT))
                .map(QuizAnswer::getQuestionAnswers)
                .flatMap(Collection::stream)
                .map(QuestionAnswer::getOption)
                .filter(Objects::nonNull)
                .filter(Option::getCorrect).count();

        int totalTournamentAnswers = (int) user.getQuizAnswers().stream()
                .filter(quizAnswer -> quizAnswer.canResultsBePublic(executionId) && quizAnswer.getQuiz().getType().equals(Quiz.QuizType.TOURNAMENT))
                .map(QuizAnswer::getQuestionAnswers)
                .mapToLong(Collection::size)
                .sum();

        if(totalTournamentAnswers == 0) return 0; //avoid division by 0

        return ((float)correctTournamentAnswers)*100/totalTournamentAnswers;
    }

    private boolean canParticipateInTournament(User user, Tournament tournament) {
        if(tournamentNotOver(tournament.getId())){
            return !hasParticipatedInTournament(user, tournament);
        }
        return false;
    }

    private boolean hasParticipatedInTournament(User user, Tournament tournament){
        if(tournament.hasQuiz()){
            Quiz quiz = tournament.getQuiz();
            QuizAnswer quizAnswer = quizAnswerRepository.findQuizAnswer(quiz.getId(), user.getId()).orElse(null);

            if(quizAnswer == null) return false;

            return quizAnswer.isCompleted();
        }
        return false;
    }

    private boolean tournamentNotOver(Integer tournamentId) {
        LocalDateTime now = LocalDateTime.now();
        Tournament tournament = getTournament(tournamentId);
        return tournament.getFinishTime().isAfter(now);
    }

    private User getUser(Integer userId) {
        return userRepository.findById(userId).orElseThrow(() -> new TutorException(ErrorMessage.USER_NOT_FOUND, userId));
    }

    private CourseExecution getCourseExecution(Integer execId) {
        return courseExecutionRepository.findById(execId).orElseThrow(() -> new TutorException(ErrorMessage.COURSE_EXECUTION_NOT_FOUND));
    }

    private Tournament getTournament(Integer tournamentId) {
        return tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new TutorException(ErrorMessage.TOURNAMENT_NOT_FOUND, tournamentId));
    }
}
