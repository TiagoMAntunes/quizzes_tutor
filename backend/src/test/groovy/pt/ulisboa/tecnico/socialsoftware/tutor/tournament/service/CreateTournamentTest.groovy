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
    def courseExecutionEntity

    def setup() {
        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        NOW_TIME = LocalDateTime.now().plusDays(1).format(formatter)
        FINISH_TIME = LocalDateTime.now().plusDays(5).format(formatter)

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

    def "create the tournament"() {
        given: "a tournamentDto"

        def tournamentDto = new TournamentDto()
        tournamentDto.setStartTime(NOW_TIME)
        tournamentDto.setFinishTime(FINISH_TIME)
        tournamentDto.setTopics(TOPIC_LIST)
        tournamentDto.setNumberOfQuestions(NUMBER_QUESTIONS)

        and: "a user"
        def user = userRepository.findAll().get(0)

        when:
        tournamentService.createTournament(tournamentDto, courseExecution, user.getId())

        then:
        tournamentRepository.count() == 1L
        def result = tournamentRepository.findAll().get(0)
        result != null
        result.getStartTime().format(formatter) == NOW_TIME
        result.getFinishTime().format(formatter) == FINISH_TIME
        result.getCreator() == user
        result.getTopics().size() == 1
        result.getNumberOfQuestions() == NUMBER_QUESTIONS

        result.getCreator() == user
        user.getCreatedTournaments().contains(result)

        result.getCourseExecution().getId() == courseExecution
        courseExecutionEntity.getTournaments().contains(result)

        courseExecutionRepository.findAll().get(0).getTournaments().size() == 1
        topicRepository.findAll().get(0).getTournaments().size() == 1
    }

    def "the tournament is created with a start time after finish time"() {
        given: "a tournamentDto with start time after finish time"
        def tournamentDto = new TournamentDto()

        tournamentDto.setStartTime(FINISH_TIME)
        tournamentDto.setFinishTime(NOW_TIME)
        tournamentDto.setTopics(TOPIC_LIST)
        tournamentDto.setNumberOfQuestions(NUMBER_QUESTIONS)

        and: "a user"
        def user = userRepository.findAll().get(0)

        when:
        tournamentService.createTournament(tournamentDto, courseExecution, user.getId())

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.INVALID_TOURNAMENT_TIME
        tournamentRepository.count() == 0L
    }

    def "the tournament is created with a start time before the time of creation"() {
        given: "a tournament with a finish time before the time of creation"

        def tournamentDto = new TournamentDto()
        tournamentDto.setTopics(TOPIC_LIST)
        tournamentDto.setNumberOfQuestions(NUMBER_QUESTIONS)
        tournamentDto.setFinishTime(FINISH_TIME)

        tournamentDto.setStartTime(LocalDateTime.now().minusDays(3).format(formatter))

        and: "a user"
        def user = userRepository.findAll().get(0)

        when:
        tournamentService.createTournament(tournamentDto, courseExecution, user.getId())

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.TOURNAMENT_ALREADY_STARTED
        tournamentRepository.count() == 0L
    }

    def "the tournament is created with 0 topics"() {
        given: "a tournament with no topics"

        def tournamentDto = new TournamentDto()
        tournamentDto.setStartTime(NOW_TIME)
        tournamentDto.setFinishTime(FINISH_TIME)
        tournamentDto.setNumberOfQuestions(NUMBER_QUESTIONS)


        List<Topic> lst = new ArrayList<>();
        tournamentDto.setTopics(lst)

        and: "a user"
        def user = userRepository.findAll().get(0)

        when:
        tournamentService.createTournament(tournamentDto, courseExecution, user.getId())

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.NO_TOPICS_SELECTED
        tournamentRepository.count() == 0L
    }

    def "the tournament is created with 0 or less questions"() {
        given: "a tournament with no questions"

        def tournamentDto = new TournamentDto()
        tournamentDto.setStartTime(NOW_TIME)
        tournamentDto.setFinishTime(FINISH_TIME)
        tournamentDto.setTopics(TOPIC_LIST)

        tournamentDto.setNumberOfQuestions(0)

        and: "a user"
        def user = userRepository.findAll().get(0)

        when:
        tournamentService.createTournament(tournamentDto, courseExecution, user.getId())

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.TOURNAMENT_HAS_NO_QUESTIONS
        tournamentRepository.count() == 0L
    }

    def "the tournament is created with repeated topics"() {
        given: "a tournament with repeated topics"

        def tournamentDto = new TournamentDto()
        tournamentDto.setStartTime(NOW_TIME)
        tournamentDto.setFinishTime(FINISH_TIME)
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

        and: "a user"
        def user = userRepository.findAll().get(0)

        when:
        tournamentService.createTournament(tournamentDto, courseExecution, user.getId())

        then:
        tournamentRepository.count() == 1L
        def result = tournamentRepository.findAll().get(0)
        result.getTopics().size() == 1L

    }

    def "the user does not exist"() {
        given: "a tournament"

        def tournamentDto = new TournamentDto()
        tournamentDto.setStartTime(NOW_TIME)
        tournamentDto.setFinishTime(FINISH_TIME)
        tournamentDto.setTopics(TOPIC_LIST)
        tournamentDto.setNumberOfQuestions(NUMBER_QUESTIONS)

        and: "a user"
        def user = userRepository.findAll().get(0)

        when: "created with a wrong user id"
        tournamentService.createTournament(tournamentDto, courseExecution, user.getId()+1)

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.USER_NOT_FOUND
        tournamentRepository.count() == 0L
    }

    def "the user is not a student" () {
        given: "a tournamentDto"

        def tournamentDto = new TournamentDto()
        tournamentDto.setStartTime(NOW_TIME)
        tournamentDto.setFinishTime(FINISH_TIME)
        tournamentDto.setTopics(TOPIC_LIST)
        tournamentDto.setNumberOfQuestions(NUMBER_QUESTIONS)


        and: "and a non-student user"

        def user = new User()
        user.setKey(2)
        user.setRole(User.Role.TEACHER)
        userRepository.save(user)
        user = userRepository.findByKey(2)

        when:
        tournamentService.createTournament(tournamentDto, courseExecution, user.getId())

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.TOURNAMENT_CREATION_INCORRECT_ROLE
        tournamentRepository.count() == 0L
    }

    def "topics are null" () {
        given: "a tournamentDto"

        def tournamentDto = new TournamentDto()
        tournamentDto.setStartTime(NOW_TIME)
        tournamentDto.setFinishTime(FINISH_TIME)
        tournamentDto.setTopics(null)
        tournamentDto.setNumberOfQuestions(NUMBER_QUESTIONS)

        and: "a user"
        def user = userRepository.findAll().get(0)

        when:
        tournamentService.createTournament(tournamentDto, courseExecution, user.getId())

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.NO_TOPICS_SELECTED
        tournamentRepository.count() == 0L

    }


    @TestConfiguration
    static class TournamentServiceImplTestContextConfiguration {

        @Bean
        TournamentService tournamentService() {
            return new TournamentService()
        }
    }
}