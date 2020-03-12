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
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.domain.Tournament
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto.TournamentDto
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.repository.TournamentRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


@DataJpaTest
class TournamentSignUpTest extends Specification {
    public static final int NUMBER_QUESTIONS = 5
    public static final String TOPIC_NAME = "Main_Topic"
    public static final String COURSE_NAME = "Software Architecture"
    public static final String COURSE_ABREV = "SA"

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
    def FIVE_DAYS_EARLIER
    def THREE_DAYS_EARLIER
    def TWO_DAYS_EARLIER
    def FIVE_DAYS_LATER
    def THREE_DAYS_LATER
    def TOPIC_LIST
    def user
    def COURSE_EXEC
    def COURSE_EXEC_ID
    def USER
    def USER_ID
    def openTournament1

    def setup() {
        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        NOW_TIME = LocalDateTime.now().format(formatter)
        FIVE_DAYS_LATER = LocalDateTime.now().plusDays(5).format(formatter)
        THREE_DAYS_LATER = LocalDateTime.now().plusDays(3).format(formatter)
        FIVE_DAYS_EARLIER = LocalDateTime.now().minusDays(5).format(formatter)
        THREE_DAYS_EARLIER = LocalDateTime.now().minusDays(3).format(formatter)
        TWO_DAYS_EARLIER = LocalDateTime.now().minusDays(2).format(formatter)

        //Creates a user
        def tmp_user = new User()
        tmp_user.setKey(1)
        userRepository.save(tmp_user)
        USER = userRepository.findAll().get(0)
        USER_ID = USER.getId()



        //Creates a course
        def course1 = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course1)

        //Creates a course execution
        def courseExecution = new CourseExecution(course1, COURSE_NAME, COURSE_ABREV, Course.Type.TECNICO)
        courseExecutionRepository.save(courseExecution)

        //Creates a topic
        def topic = new Topic()
        topic.setName(TOPIC_NAME)
        topic.setCourse(course1)
        course1.addTopic(topic)
        topic = topicRepository.save(topic)

        //Creates a topicdto list with 1 element
        def topicList = new ArrayList<TopicDto>()
        topicList.add(new TopicDto(topic))

        TOPIC_LIST = topicList

        //Creates an open tournament
        def course2 = courseExecutionRepository.findAll().get(0)
        COURSE_EXEC = course2
        COURSE_EXEC_ID = course2.getId()

        def tournamentDto = new TournamentDto()
        tournamentDto.setStartTime(THREE_DAYS_EARLIER)
        tournamentDto.setFinishTime(THREE_DAYS_LATER)
        tournamentDto.setTopics(TOPIC_LIST)
        tournamentDto.setNumberOfQuestions(NUMBER_QUESTIONS)

        openTournament1 = tournamentDto
    }

    def "tournament is not open"(){
        given:"a tournament that is not open"
        tournamentService.createTournament(openTournament1, COURSE_EXEC, USER)
        def result = tournamentRepository.findAll().get(0)
        result.setFinishTime(LocalDateTime.parse(THREE_DAYS_EARLIER, formatter))
        tournamentRepository.save(result)
        def tournament = tournamentRepository.findAll().get(0)

        when:
        tournamentService.joinTournament(tournament.getId(), COURSE_EXEC_ID, USER_ID)

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.TOURNAMENT_NOT_OPEN
        !tournament.hasSignedUp(USER)
        !USER.hasTournament(tournament)
    }

    def "tournament is open and student hasnt signed up for it yet"(){
        given:"an open tournament that the student hasnt joined"
        tournamentService.createTournament(openTournament1, COURSE_EXEC, USER)
        def tournament = tournamentRepository.findAll().get(0)

        when:
        tournamentService.joinTournament(tournament.getId(), COURSE_EXEC_ID, USER_ID)

        then:
        tournament.hasSignedUp(USER)
        USER.hasTournament(tournament)
    }

    def "tournament is open and student has already signed up for it"(){
        given:"an open tournament that the student has already joined"
        tournamentService.createTournament(openTournament1, COURSE_EXEC, USER)
        def tournament = tournamentRepository.findAll().get(0)
        tournamentService.joinTournament(tournament.getId(), COURSE_EXEC_ID, USER_ID)

        when:
        tournamentService.joinTournament(tournament.getId(), COURSE_EXEC_ID, USER_ID)

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.TOURNAMENT_ALREADY_JOINED
    }

    def "no user with the given id"(){
        given:"an unused user id"
        tournamentService.createTournament(openTournament1, COURSE_EXEC, USER)
        def tournament = tournamentRepository.findAll().get(0)
        def userId = 1
        def user = new User()
        user.setId(userId)

        when:
        tournamentService.joinTournament(tournament.getId(), COURSE_EXEC_ID, userId)

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.USER_NOT_FOUND
        !tournament.hasSignedUp(user)
    }

    def "no tournament with the given id"(){
        given:"an unused tournament id"
        def tournamentId = 1
        def tournament = new Tournament();
        tournament.setId(tournamentId)

        when:
        tournamentService.joinTournament(tournamentId, COURSE_EXEC_ID, USER_ID)

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.TOURNAMENT_NOT_FOUND
        !USER.hasTournament(tournament)
    }

    @TestConfiguration
    static class TournamentServiceImplTestContextConfiguration {

        @Bean
        TournamentService tournamentService() {
            return new TournamentService()
        }
    }
}