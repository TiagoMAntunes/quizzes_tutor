package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.TopicRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentService
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto.TournamentDto
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.repository.TournamentRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


@DataJpaTest
class CreateTournamentTest extends Specification {
    public static final int NUMBER_QUESTIONS = 5
    public static final int TOURNAMENT_ID = 1
    public static final String TOPIC_NAME = "Main_Topic"
    public static final String COURSE_NAME = "Software Architecture"
    public static final String COURSE_ABREV = "ES1"


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

    def formatter
    def NOW_TIME
    def FINISH_TIME
    def TOPIC_LIST
    def course
    def courseExecution

    def setup() {
        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        NOW_TIME = LocalDateTime.now().format(formatter)
        FINISH_TIME = LocalDateTime.now().plusDays(5).format(formatter)

        //Creates a user
        def user = new User()
        user.setKey(1)
        userRepository.save(user)

        //Creates a course
        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)

        //Creates a course execution
        courseExecution = new CourseExecution(course, COURSE_NAME, COURSE_ABREV, Course.Type.TECNICO)
        courseExecutionRepository.save(courseExecution)

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

    def "create the tournament"() {
        given: "a tournamentDto"
        def userId = userRepository.findAll().get(0).getId()

        def tournamentDto = new TournamentDto()
        tournamentDto.setStartTime(NOW_TIME)
        tournamentDto.setFinishTime(FINISH_TIME)
        tournamentDto.setCreatorId(userId)
        tournamentDto.setTopics(TOPIC_LIST)
        tournamentDto.setNumberOfQuestions(NUMBER_QUESTIONS)

        when:
        tournamentService.createTournament(tournamentDto, courseExecution)

        then:
        tournamentRepository.count() == 1L
        def result = tournamentRepository.findAll().get(0)
        result.getId() != null
        result.getKey() != null
        result.getStartTime().format(formatter) == NOW_TIME
        result.getFinishTime().format(formatter) == FINISH_TIME
        result.getCreatorID() == userId
        result.getTopics().size() == 1
        result.getNumberOfQuestions() == NUMBER_QUESTIONS

        courseExecutionRepository.findAll().get(0).getTournaments().size() == 1
        topicRepository.findAll().get(0).getTournaments().size() == 1
    }

    def "the tournament is created with a start time after finish time"() {
        given: "a tournamentDto with start time after finish time"
        def userId = userRepository.findAll().get(0).getId()

        def tournamentDto = new TournamentDto()

        tournamentDto.setStartTime(FINISH_TIME)
        tournamentDto.setFinishTime(NOW_TIME)
        tournamentDto.setCreatorId(userId)
        tournamentDto.setTopics(TOPIC_LIST)
        tournamentDto.setNumberOfQuestions(NUMBER_QUESTIONS)

        when:
        tournamentService.createTournament(tournamentDto, courseExecution)

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.INVALID_TOURNAMENT_TIME
        tournamentRepository.count() == 0L
    }

    def "the tournament is created with a finish time before the time of creation"() {
        given: "a tournament with a finish time before the time of creation"
        def userId = userRepository.findAll().get(0).getId()

        def tournamentDto = new TournamentDto()
        tournamentDto.setCreatorId(userId)
        tournamentDto.setTopics(TOPIC_LIST)
        tournamentDto.setNumberOfQuestions(NUMBER_QUESTIONS)

        tournamentDto.setStartTime(LocalDateTime.now().minusDays(3).format(formatter))
        tournamentDto.setFinishTime(LocalDateTime.now().minusDays(1).format(formatter))

        when:
        tournamentService.createTournament(tournamentDto, courseExecution)

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.TOURNAMENT_ALREADY_FINISHED
        tournamentRepository.count() == 0L
    }

    def "the tournament is created with 0 topics"() {
        given: "a tournament with no topics"
        def userId = userRepository.findAll().get(0).getId()

        def tournamentDto = new TournamentDto()
        tournamentDto.setStartTime(NOW_TIME)
        tournamentDto.setFinishTime(FINISH_TIME)
        tournamentDto.setCreatorId(userId)

        tournamentDto.setNumberOfQuestions(NUMBER_QUESTIONS)


        List<Topic> lst = new ArrayList<>();
        tournamentDto.setTopics(lst)

        when:
        tournamentService.createTournament(tournamentDto, courseExecution)

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.NO_TOPICS_SELECTED
        tournamentRepository.count() == 0L
    }

    def "the tournament is created with 0 or less questions"() {
        given: "a tournament with no questions"
        def userId = userRepository.findAll().get(0).getId()

        def tournamentDto = new TournamentDto()
        tournamentDto.setStartTime(NOW_TIME)
        tournamentDto.setFinishTime(FINISH_TIME)
        tournamentDto.setCreatorId(userId)
        tournamentDto.setTopics(TOPIC_LIST)

        tournamentDto.setNumberOfQuestions(0)

        when:
        tournamentService.createTournament(tournamentDto, courseExecution)

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.TOURNAMENT_HAS_NO_QUESTIONS
        tournamentRepository.count() == 0L
    }

    def "the tournament is created with repeated topics"() {
        given: "a tournament with repeated topics"
        def userId = userRepository.findAll().get(0).getId()

        def tournamentDto = new TournamentDto()
        tournamentDto.setStartTime(NOW_TIME)
        tournamentDto.setFinishTime(FINISH_TIME)
        tournamentDto.setCreatorId(userId)

        tournamentDto.setNumberOfQuestions(NUMBER_QUESTIONS)

        def topic = new Topic()
        topic.setName(TOPIC_NAME)
        topic = topicRepository.save(topic)

        def repeated_topic = new Topic()
        repeated_topic.setName(TOPIC_NAME)
        repeated_topic = topicRepository.save(topic)


        def topicList = new ArrayList()
        topicList.add(new TopicDto(topic))
        topicList.add(new TopicDto(repeated_topic))

        tournamentDto.setTopics(topicList)

        when:
        tournamentService.createTournament(tournamentDto, courseExecution)

        then:
        tournamentRepository.count() == 1L
        def result = tournamentRepository.findAll().get(0)
        result.getTopics().size() == 1L

    }

    @TestConfiguration
    static class TournamentServiceImplTestContextConfiguration {

        @Bean
        TournamentService tournamentService() {
            return new TournamentService()
        }
    }
}