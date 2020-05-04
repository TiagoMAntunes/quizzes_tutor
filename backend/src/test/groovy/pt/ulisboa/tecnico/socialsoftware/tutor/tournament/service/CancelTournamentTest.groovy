package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
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
class CancelTournamentTest extends Specification {
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
    def TWO_DAYS_AGO_DATETIME
    def IN_TWO_DAYS_TIME
    def IN_FOUR_DAYS_TIME
    def TOPIC_LIST
    def courseExecutionId
    def userId
    def tournamentId
    def courseExecution

    def setup() {
        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        TWO_DAYS_AGO_DATETIME = LocalDateTime.now().minusDays(2)
        IN_TWO_DAYS_TIME = LocalDateTime.now().plusDays(2).format(formatter)
        IN_FOUR_DAYS_TIME = LocalDateTime.now().plusDays(4).format(formatter)

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
        tournamentDto.setStartTime(IN_TWO_DAYS_TIME)
        tournamentDto.setFinishTime(IN_FOUR_DAYS_TIME)
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
        tournament.setStartTime(TWO_DAYS_AGO_DATETIME)

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

    @TestConfiguration
    static class TournamentServiceImplTestContextConfiguration {

        @Bean
        TournamentService tournamentService() {
            return new TournamentService()
        }
    }
}
