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
    def TWO_DAYS_AGO_TIME
    def IN_TWO_DAYS_TIME
    def IN_FOUR_DAYS_TIME
    def TOPIC_LIST
    def courseExecution

    def setup() {
        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        TWO_DAYS_AGO_TIME = LocalDateTime.now().minusDays(2).format(formatter)
        IN_TWO_DAYS_TIME = LocalDateTime.now().plusDays(2).format(formatter)
        IN_FOUR_DAYS_TIME = LocalDateTime.now().plusDays(4).format(formatter)

        //Creates a user
        def user = new User()
        user.setKey(1)
        userRepository.save(user)

        //Creates a course
        def course = new Course(COURSE_NAME, Course.Type.TECNICO)
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

        //Creates a topicDto list with 1 element
        def topicList = new ArrayList<TopicDto>()
        topicList.add(new TopicDto(topic))

        TOPIC_LIST = topicList
    }

    // A cancelable tournament is one that the student created but hasn't started yet
    def "cancel a cancelable tournament"() {
        given: "a cancelable tournament"
        def userId = userRepository.findAll().get(0).getId()
        def user = userRepository.findAll().get(0)

        def tournamentDto = new TournamentDto()
        tournamentDto.setStartTime(IN_TWO_DAYS_TIME)
        tournamentDto.setFinishTime(IN_FOUR_DAYS_TIME)
        tournamentDto.setTopics(TOPIC_LIST)
        tournamentDto.setNumberOfQuestions(NUMBER_QUESTIONS)
        tournamentService.createTournament(tournamentDto, courseExecution, user)

        def tournamentId = tournamentRepository.findAll().get(0).getId()

        when: "cancel tournament"
        tournamentService.cancelTournament(tournamentId, user.getId())

        then: "tournament is canceled"
        tournamentRepository.count() == 0L
        userRepository.findAll().get(0).getCreatedTournaments().size() == 0
        courseExecutionRepository.findAll().get(0).getTournaments().size() == 0
        topicRepository.findAll().get(0).getTournaments().size() == 0
    }

    def "cancel a tournament not created by the student"() {
        given: "a cancelable tournament"
        def userId = userRepository.findAll().get(0).getId()
        def user = userRepository.findAll().get(0)

        def tournamentDto = new TournamentDto()
        tournamentDto.setStartTime(IN_TWO_DAYS_TIME)
        tournamentDto.setFinishTime(IN_FOUR_DAYS_TIME)
        tournamentDto.setTopics(TOPIC_LIST)
        tournamentDto.setNumberOfQuestions(NUMBER_QUESTIONS)
        tournamentService.createTournament(tournamentDto, courseExecution, user)

        def tournamentId = tournamentRepository.findAll().get(0).getId()

        when: "not the creater tries to cancel"
        tournamentService.cancelTournament(tournamentId, userId+100)

        then: "tournament not canceled"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.TOURNAMENT_NOT_THE_CREATER
        tournamentRepository.count() == 1L
        userRepository.findAll().get(0).getCreatedTournaments().size() == 1
        courseExecutionRepository.findAll().get(0).getTournaments().size() == 1
        topicRepository.findAll().get(0).getTournaments().size() == 1
    }

    def "cancel a tournament after it as started"() {
        given: "a tournament that has started"
        def userId = userRepository.findAll().get(0).getId()
        def user = userRepository.findAll().get(0)

        def tournamentDto = new TournamentDto()
        tournamentDto.setStartTime(TWO_DAYS_AGO_TIME)
        tournamentDto.setFinishTime(IN_FOUR_DAYS_TIME)
        tournamentDto.setTopics(TOPIC_LIST)
        tournamentDto.setNumberOfQuestions(NUMBER_QUESTIONS)
        tournamentService.createTournament(tournamentDto, courseExecution, user)

        def tournamentId = tournamentRepository.findAll().get(0).getId()

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
        // the tournament should be canceled and removed from the students tournaments
        // depends on feature tdp:f3
        expect: false
    }

    @TestConfiguration
    static class TournamentServiceImplTestContextConfiguration {

        @Bean
        TournamentService tournamentService() {
            return new TournamentService()
        }
    }
}
