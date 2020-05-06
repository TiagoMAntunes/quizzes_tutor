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
    public static final String TOURNAMENT_TITLE = "title"

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
    def ONE_DAY_LATER
    def TOPIC_LIST
    def COURSE_EXEC_ID
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
        ONE_DAY_LATER = LocalDateTime.now().plusDays(1).format(formatter)

        //Creates a user
        def tmp_user = new User()
        tmp_user.setKey(1)
        tmp_user.setRole(User.Role.STUDENT)
        userRepository.save(tmp_user)

        //Creates a course
        def course1 = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course1)

        def course2 = new Course(DIFF_COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course2)

        //Creates a course execution
        def courseExecution = new CourseExecution(course1, COURSE_NAME, COURSE_ABREV, Course.Type.TECNICO)
        courseExecutionRepository.save(courseExecution)

        def diffCourseExecution = new CourseExecution(course2, DIFF_COURSE_NAME, DIFF_COURSE_ABREV, Course.Type.TECNICO)
        courseExecutionRepository.save(diffCourseExecution)


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

        def course = courseExecutionRepository.findAll().get(0)
        COURSE_EXEC_ID = course.getId()

        def tournamentDto1 = new TournamentDto()
        tournamentDto1.setTitle(TOURNAMENT_TITLE)
        tournamentDto1.setStartTime(ONE_DAY_LATER)
        tournamentDto1.setFinishTime(THREE_DAYS_LATER)
        tournamentDto1.setTopics(TOPIC_LIST)
        tournamentDto1.setNumberOfQuestions(NUMBER_QUESTIONS)

        openTournament1 = tournamentDto1


        //Creates an open tournament for a different course
        def diffCourse = courseExecutionRepository.findAll().get(1)
        DIFF_COURSE_EXEC_ID = diffCourse.getId()

        def diffTournamentDto = new TournamentDto()
        diffTournamentDto.setTitle(TOURNAMENT_TITLE)
        diffTournamentDto.setStartTime(ONE_DAY_LATER)
        diffTournamentDto.setFinishTime(FIVE_DAYS_LATER)
        diffTournamentDto.setTopics(TOPIC_LIST)
        diffTournamentDto.setNumberOfQuestions(NUMBER_QUESTIONS)

        diffCourseTournament = diffTournamentDto

        //Creates 2 more open tournaments for this course
        def tournamentDto2 = new TournamentDto()
        tournamentDto2.setTitle(TOURNAMENT_TITLE)
        tournamentDto2.setStartTime(ONE_DAY_LATER)
        tournamentDto2.setFinishTime(THREE_DAYS_LATER)
        tournamentDto2.setTopics(TOPIC_LIST)
        tournamentDto2.setNumberOfQuestions(NUMBER_QUESTIONS)

        openTournament2 = tournamentDto2

        def tournamentDto3 = new TournamentDto()
        tournamentDto3.setTitle(TOURNAMENT_TITLE)
        tournamentDto3.setStartTime(ONE_DAY_LATER)
        tournamentDto3.setFinishTime(THREE_DAYS_LATER)
        tournamentDto3.setTopics(TOPIC_LIST)
        tournamentDto3.setNumberOfQuestions(NUMBER_QUESTIONS)

        openTournament3 = tournamentDto3
    }

    def "no open tournaments"(){
        given:"a tournament that is already closed"
        tournamentService.createTournament(openTournament2, COURSE_EXEC_ID, user)
        def result = tournamentRepository.findAll().get(0)
        result.setFinishTime(LocalDateTime.parse(THREE_DAYS_EARLIER, formatter))

        when:
        def tournaments = tournamentService.getOpenTournaments(COURSE_EXEC_ID, user)

        then:
        tournaments.size() == 0
    }

    def "no open tournaments for this course"(){
        given:"a tournament that is open, but belongs to a different course"
        tournamentService.createTournament(diffCourseTournament, DIFF_COURSE_EXEC_ID, user)

        when:
        def tournaments = tournamentService.getOpenTournaments(COURSE_EXEC_ID, user)

        then:
        tournaments.size() == 0
    }

    def "one open tournament and is the creator"(){
        given:"an open tournament for this course"
        tournamentService.createTournament(openTournament1, COURSE_EXEC_ID, user)

        when:
        def tournaments = tournamentService.getOpenTournaments(COURSE_EXEC_ID, user)

        then:
        tournaments.size() == 1
        tournaments[0].isCreator
    }

    def "one open tournament and is not the creator"(){
        given:"an open tournament for this course"
        tournamentService.createTournament(openTournament1, COURSE_EXEC_ID, user)

        when:
        def tournaments = tournamentService.getOpenTournaments(COURSE_EXEC_ID, user+1)

        then:
        tournaments.size() == 1
        !tournaments[0].isCreator
    }

    def "3 open tournaments"(){
        given:"3 open tournaments for this course"
        tournamentService.createTournament(openTournament1, COURSE_EXEC_ID, user)
        tournamentService.createTournament(openTournament2, COURSE_EXEC_ID, user)
        tournamentService.createTournament(openTournament3, COURSE_EXEC_ID, user)

        when:
        def tournaments = tournamentService.getOpenTournaments(COURSE_EXEC_ID, user)

        then:"returns the 3 tournaments"
        tournaments.size() == 3
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
    }
}