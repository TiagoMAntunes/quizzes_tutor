package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.AnswerService
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.AnswersXmlImport
import pt.ulisboa.tecnico.socialsoftware.tutor.question.QuestionService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.TopicRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentService
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto.TournamentDto
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.repository.TournamentRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.QuizService
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.repository.QuizRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.config.DateHandler
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserService;
import spock.lang.Specification

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@DataJpaTest
class CancelTournamentTest extends Specification {
    public static final int NUMBER_QUESTIONS = 5
    public static final int TOURNAMENT_ID = 1
    public static final String TOPIC_NAME = "Main_Topic"
    public static final String COURSE_NAME = "Software Architecture"
    public static final String COURSE_ABREV = "ES1"
    public static final String TOURNAMENT_TITLE = "title"

    public static final LocalDateTime YESTERDAY = DateHandler.now().minusDays(1)
    public static final String TOMORROW = DateHandler.toISOString(DateHandler.now().plusDays(1))
    public static final String LATER = DateHandler.toISOString(DateHandler.now().plusDays(2))

    @Autowired
    TournamentRepository tournamentRepository

    @Autowired
    TournamentService tournamentService

    @Autowired
    UserRepository userRepository

    @Autowired
    TopicRepository topicRepository

    @Autowired
    CourseRepository courseRepository

    @Autowired
    CourseExecutionRepository courseExecutionRepository

    @Autowired
    QuizRepository quizRepository

    def TOPIC_LIST
    def courseExecutionId
    def userId
    def STUDENT_JOINED_ID
    def tournamentId
    def courseExecution

    def setup() {
        //Creates a course
        def course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)

        //Creates a course execution
        def newCourseExecution = new CourseExecution(course, COURSE_NAME, COURSE_ABREV, Course.Type.TECNICO)
        courseExecutionRepository.save(newCourseExecution)
        courseExecution = courseExecutionRepository.findAll().get(0)
        courseExecutionId = courseExecution.getId()

        //Creates a user
        def user = new User()
        user.setKey(1)
        user.setRole(User.Role.STUDENT)
        user.addCourse(courseExecution)
        userRepository.save(user)
        userId = userRepository.findAll().get(0).getId()

        def student1 = new User()
        student1.setKey(userRepository.getMaxUserNumber() + 1)
        student1.setRole(User.Role.STUDENT)
        student1.getCourseExecutions().add(courseExecution)
        courseExecution.getUsers().add(student1)
        userRepository.save(student1)

        STUDENT_JOINED_ID = userRepository.findByKey(student1.getKey()).getId()

        //Creates a topic
        def topic = new Topic()
        topic.setName(TOPIC_NAME)
        topic.setCourse(course)
        course.addTopic(topic)
        topic = topicRepository.save(topic)

        //Creates a topicDto list with 1 element
        def topicList = new ArrayList<TopicDto>()
        topicList.add(new TopicDto(topic))

        TOPIC_LIST = topicList

        //Creates a cancelable tournament
        def tournamentDto = new TournamentDto()
        tournamentDto.setTitle(TOURNAMENT_TITLE)
        tournamentDto.setStartTime(TOMORROW)
        tournamentDto.setFinishTime(LATER)
        tournamentDto.setTopics(TOPIC_LIST)
        tournamentDto.setNumberOfQuestions(NUMBER_QUESTIONS)
        tournamentService.createTournament(tournamentDto, courseExecutionId, userId)

        tournamentId = tournamentRepository.findAll().get(0).getId()
    }

    // A cancelable tournament is one that the student created but hasn't started yet
    def "cancel a cancelable tournament"() {
        when: "cancel tournament"
        tournamentService.cancelTournament(tournamentId, userId)

        then: "tournament is canceled"
        tournamentRepository.count() == 0L
        userRepository.findAll().get(0).getCreatedTournaments().size() == 0
        courseExecutionRepository.findAll().get(0).getTournaments().size() == 0
        topicRepository.findAll().get(0).getTournaments().size() == 0
    }

    def "cancel a non existing tournament"() {
        given: "non existing tournament"
        def nonExistingTournamentId = tournamentId + 10

        when: "try to cancel"
        tournamentService.cancelTournament(nonExistingTournamentId, userId)

        then: "error thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.TOURNAMENT_NOT_FOUND
    }

    def "cancel a tournament not created by the student"() {
        given: "user that is not the creator"
        def notTheCreatorId = userId + 24

        when: "not the creator tries to cancel"
        tournamentService.cancelTournament(tournamentId, notTheCreatorId)

        then: "tournament not canceled"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.TOURNAMENT_USER_IS_NOT_THE_CREATOR
        tournamentRepository.count() == 1L
        userRepository.findAll().get(0).getCreatedTournaments().size() == 1
        courseExecutionRepository.findAll().get(0).getTournaments().size() == 1
        topicRepository.findAll().get(0).getTournaments().size() == 1
    }

    def "cancel a tournament after it as started"() {
        given: "a tournament that has started"
        def tournament = tournamentRepository.findAll().get(0)
        tournament.setStartTime(YESTERDAY)

        when: "tries to cancel"
        tournamentService.cancelTournament(tournamentId, userId)

        then: "tournament not canceled"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.TOURNAMENT_HAS_STARTED
        tournamentRepository.count() == 1L
        userRepository.findAll().get(0).getCreatedTournaments().size() == 1
        courseExecutionRepository.findAll().get(0).getTournaments().size() == 1
        topicRepository.findAll().get(0).getTournaments().size() == 1
    }

    def "cancel a tournament with 1 student signed up"() {
        given: "a cancelable tournament with 1 student signed up"
        tournamentService.joinTournament(tournamentId, userId)

        when: "cancel tournament"
        tournamentService.cancelTournament(tournamentId, userId)

        then: "tournament is canceled"
        tournamentRepository.count() == 0L
        userRepository.findAll().get(0).getCreatedTournaments().size() == 0
        userRepository.findAll().get(0).getSignedUpTournaments().size() == 0
        courseExecutionRepository.findAll().get(0).getTournaments().size() == 0
        topicRepository.findAll().get(0).getTournaments().size() == 0
    }

    def "cancel a tournament with a quiz associated"() {
        given: "a cancelable tournament with a quiz associated"
        tournamentService.joinTournament(tournamentId, userId)
        tournamentService.joinTournament(tournamentId, STUDENT_JOINED_ID)

        when: "cancel tournament"
        tournamentService.cancelTournament(tournamentId, userId)

        then: "tournament is canceled"
        tournamentRepository.count() == 0L
        userRepository.findAll().get(0).getCreatedTournaments().size() == 0
        userRepository.findAll().get(0).getSignedUpTournaments().size() == 0
        courseExecutionRepository.findAll().get(0).getTournaments().size() == 0
        topicRepository.findAll().get(0).getTournaments().size() == 0
        quizRepository.count() == 0L
    }

    @TestConfiguration
    static class TournamentServiceImplTestContextConfiguration {

        @Bean
        TournamentService tournamentService() {
            return new TournamentService()
        }

        @Bean
        QuizService quizService() {
            return new QuizService()
        }

        @Bean
        AnswerService answerService() {
            return new AnswerService()
        }

        @Bean
        AnswersXmlImport answersXmlImport() {
            return new AnswersXmlImport()
        }

        @Bean
        QuestionService questionService() {
            return new QuestionService()
        }

        @Bean
        UserService userService() {
            return new UserService()
        }
    }
}
