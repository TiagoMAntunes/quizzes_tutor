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
import pt.ulisboa.tecnico.socialsoftware.tutor.config.DateHandler
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserService;
import spock.lang.Specification

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@DataJpaTest
class CreateTournamentServicePerformanceTest extends Specification {

    public static final int NUMBER_QUESTIONS = 5
    public static final String TOPIC_NAME = "Main_Topic"
    public static final String COURSE_NAME = "Software Architecture"
    public static final String COURSE_ABREV = "ES1"
    public static final String TOURNAMENT_TITLE = "title"

    public static final String TOMORROW = DateHandler.toISOString(DateHandler.now().plusDays(1))
    public static final String LATER =DateHandler.toISOString( DateHandler.now().plusDays(2))

    //Should be 1000000
    public static final int N_TOURNAMENTS = 1

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

    def TOPIC_LIST
    def course
    def courseExecution
    def courseExecutionEntity

    def setup() {

        //Creates a user
        def user = new User()
        user.setKey(1)
        user.setRole(User.Role.STUDENT)
        userRepository.save(user)

        //Creates a course
        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)

        //Creates a course execution
        courseExecutionEntity = new CourseExecution(course, COURSE_NAME, COURSE_ABREV, Course.Type.TECNICO)
        courseExecutionRepository.save(courseExecutionEntity)
        courseExecution = courseExecutionRepository.findAll().get(0).getId()

        //Creates a topic
        def topic = new Topic()
        topic.setName(TOPIC_NAME)
        topic.setCourse(course)
        course.addTopic(topic)
        topic = topicRepository.save(topic)

        //Creates a topicdto list with 1 element
        def topicList = new ArrayList<TopicDto>()
        topicList.add(new TopicDto(topic))

        TOPIC_LIST = topicList
    }

    def "performance testing to create 1M tournaments"() {
        given: "a tournamentDto"
        def tournamentDto = new TournamentDto()
        tournamentDto.setTitle(TOURNAMENT_TITLE)
        tournamentDto.setStartTime(TOMORROW)
        tournamentDto.setFinishTime(LATER)
        tournamentDto.setTopics(TOPIC_LIST)
        tournamentDto.setNumberOfQuestions(NUMBER_QUESTIONS)

        and: "a user"
        def user = userRepository.findAll().get(0)

        when:
        1.upto(N_TOURNAMENTS, {tournamentService.createTournament(tournamentDto, courseExecution, user.getId())})

        then:
        tournamentRepository.count() == N_TOURNAMENTS

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
