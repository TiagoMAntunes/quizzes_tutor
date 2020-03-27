package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
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
class JoinTournamentServicePerformanceTest extends Specification {

    public static final int NUMBER_QUESTIONS = 5
    public static final String TOPIC_NAME = "Main_Topic"
    public static final String COURSE_NAME = "Software Architecture"
    public static final String COURSE_ABREV = "ES1"

    //Should be 100000
    public static final int N_USERS = 1

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

    def tournamentDto
    def courseExecution

    def setup() {
        def formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        def NOW_TIME = LocalDateTime.now().plusDays(1).format(formatter)
        def FINISH_TIME = LocalDateTime.now().plusDays(5).format(formatter)


        //Creates a course
        def course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)

        //Creates a course execution
        def courseExecutionEntity = new CourseExecution(course, COURSE_NAME, COURSE_ABREV, Course.Type.TECNICO)
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

        def TOPIC_LIST = topicList

        tournamentDto = new TournamentDto()
        tournamentDto.setStartTime(NOW_TIME)
        tournamentDto.setFinishTime(FINISH_TIME)
        tournamentDto.setTopics(TOPIC_LIST)
        tournamentDto.setNumberOfQuestions(NUMBER_QUESTIONS)

    }


    def "performance testing 100k users joining the same tournament"() {
        given: "100k users and 1 tournament to join"
        def course_execution_object = courseExecutionRepository.findAll().get(0)
        1.upto(N_USERS, {
            i ->
                def user = new User()
                user.setKey(i as Integer)
                user.setRole(User.Role.STUDENT)
                user.addCourse(course_execution_object)
                userRepository.save(user)
        })
        def user_id = userRepository.findAll().get(0).getId()
        tournamentService.createTournament(tournamentDto, courseExecution, user_id)
        def tournament_id = tournamentRepository.findAll().get(0).getId()

        when:
        1.upto(N_USERS, {
            id->
                tournamentService.joinTournament(tournament_id, courseExecution, id as Integer)
        })

        then:
        tournamentService.getTournamentSignedUpNumber(tournament_id) == N_USERS

    }

    @TestConfiguration
    static class TournamentServiceImplTestContextConfiguration {

        @Bean
        TournamentService tournamentService() {
            return new TournamentService()
        }
    }
}