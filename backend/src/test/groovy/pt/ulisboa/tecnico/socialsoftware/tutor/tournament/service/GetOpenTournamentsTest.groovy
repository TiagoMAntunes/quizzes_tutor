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
class GetOpenTournamentsTest extends Specification {
    public static final int NUMBER_QUESTIONS = 5
    public static final String TOPIC_NAME = "Main_Topic"
    public static final String COURSE_NAME = "Software Architecture"
    public static final String DIFF_COURSE_NAME = "Object Oriented Programming"
    public static final String COURSE_ABREV = "SA"
    public static final String DIFF_COURSE_ABREV = "OOP"

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
    def COURSE_EXEC
    def COURSE_EXEC_ID
    def DIFF_COURSE_EXEC
    def DIFF_COURSE_EXEC_ID
    def openTournament1
    def openTournament2
    def openTournament3
    def diffCourseTournament
    def user

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

        //Creates a course
        def course1 = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course1)

        def diffCourse = new Course(DIFF_COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(diffCourse)

        //Creates a course execution
        def courseExecution = new CourseExecution(course1, COURSE_NAME, COURSE_ABREV, Course.Type.TECNICO)
        courseExecutionRepository.save(courseExecution)

        def diffCourseExecution = new CourseExecution(diffCourse, COURSE_NAME, DIFF_COURSE_ABREV, Course.Type.TECNICO)
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
        user = userRepository.findAll().get(0).getId()

        def course2 = courseExecutionRepository.findAll().get(0)
        COURSE_EXEC = course2
        COURSE_EXEC_ID = course2.getId()

        def tournamentDto = new TournamentDto()
        tournamentDto.setStartTime(THREE_DAYS_EARLIER)
        tournamentDto.setFinishTime(THREE_DAYS_LATER)
                tournamentDto.setTopics(TOPIC_LIST)
        tournamentDto.setNumberOfQuestions(NUMBER_QUESTIONS)

        openTournament1 = tournamentDto


        //Creates an open tournament for a different course
        def course3 = courseExecutionRepository.findAll().get(1)
        DIFF_COURSE_EXEC = course3
        DIFF_COURSE_EXEC_ID = course3.getId()

        def tournamentDto3 = new TournamentDto()
        tournamentDto3.setStartTime(THREE_DAYS_LATER)
        tournamentDto3.setFinishTime(FIVE_DAYS_LATER)
                tournamentDto3.setTopics(TOPIC_LIST)
        tournamentDto3.setNumberOfQuestions(NUMBER_QUESTIONS)

        diffCourseTournament = tournamentDto3

        //Creates 2 more open tournaments for this course
        def tournamentDto4 = new TournamentDto()
        tournamentDto4.setStartTime(FIVE_DAYS_EARLIER)
        tournamentDto4.setFinishTime(THREE_DAYS_LATER)
                tournamentDto4.setTopics(TOPIC_LIST)
        tournamentDto4.setNumberOfQuestions(NUMBER_QUESTIONS)

        openTournament2 = tournamentDto4

        def tournamentDto5 = new TournamentDto()
        tournamentDto5.setStartTime(TWO_DAYS_EARLIER)
        tournamentDto5.setFinishTime(THREE_DAYS_LATER)
                tournamentDto5.setTopics(TOPIC_LIST)
        tournamentDto5.setNumberOfQuestions(NUMBER_QUESTIONS)

        openTournament3 = tournamentDto5
    }

    def "no open tournaments"(){
        given:"a tournament that is already closed"
        tournamentService.createTournament(openTournament2, COURSE_EXEC, user)
        def result = tournamentRepository.findAll().get(0)
        result.setFinishTime(LocalDateTime.parse(THREE_DAYS_EARLIER, formatter))

        when:
        def tournaments = tournamentService.getOpenTournaments(COURSE_EXEC_ID)

        then:
        tournaments.size() == 0
    }

    def "no open tournaments for this course"(){
        given:"a tournament that is open, but belongs to a different course"
        tournamentService.createTournament(diffCourseTournament, DIFF_COURSE_EXEC, user)

        when:
        def tournaments = tournamentService.getOpenTournaments(COURSE_EXEC_ID)

        then:
        tournaments.size() == 0
    }

    def "one open tournament"(){
        given:"an open tournament for this course"
        tournamentService.createTournament(openTournament1, COURSE_EXEC, user)

        when:
        def tournaments = tournamentService.getOpenTournaments(COURSE_EXEC_ID)

        then:
        tournaments.size() == 1

    }

    def "3 open tournaments"(){
        given:"3 open tournaments for this course"
        tournamentService.createTournament(openTournament1, COURSE_EXEC, user)
        tournamentService.createTournament(openTournament2, COURSE_EXEC, user)
        tournamentService.createTournament(openTournament3, COURSE_EXEC, user)

        when:
        def tournaments = tournamentService.getOpenTournaments(COURSE_EXEC_ID)

        then:"returns the 3 tournaments"
        tournaments.size() == 3


    }

    @TestConfiguration
    static class TournamentServiceImplTestContextConfiguration {

        @Bean
        TournamentService tournamentService() {
            return new TournamentService()
        }
    }
}