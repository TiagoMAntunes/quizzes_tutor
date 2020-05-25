package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.AnswerService
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.repository.QuizAnswerRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.DashboardService
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.AnswersXmlImport
import pt.ulisboa.tecnico.socialsoftware.tutor.question.QuestionService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.TopicRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentService
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.domain.Tournament
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto.TournamentDto
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.repository.TournamentRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.config.DateHandler;
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.QuizService
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserService
import spock.lang.Specification

import java.util.function.Predicate;
import java.time.LocalDateTime


@DataJpaTest
class StudentBanTest extends Specification {
    public static final int NUMBER_QUESTIONS = 1
    public static final String TOPIC_NAME = "Main_Topic"
    public static final String COURSE_ABREV = "Software Architecture"
    public static final String ACADEMIC_TERM = "1"
    public static final String DIFF_ACADEMIC_TERM = "2"
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
    QuestionRepository questionRepository

    @Autowired
    AnswerService answerService

    @Autowired
    QuizAnswerRepository quizAnswerRepository

    @Autowired
    DashboardService dashboardService

    def TOPIC_LIST
    def COURSE_EXEC_ID
    def DIFF_COURSE_EXEC_ID
    def USER
    def USER_ID
    def TEACHER
    def TEACHER_ID
    def openTournamentDto
    def openTournament
    def openTournamentId
    def diffExecOpenTournamentDto
    def diffExecOpenTournament
    def diffExecOpenTournamentId
    def courseExec
    def QUESTION_TITLE = "Question"


    def setup() {

        //Creates a course
        def course1 = new Course(COURSE_ABREV, Course.Type.TECNICO)
        courseRepository.save(course1)

        //Creates a course execution
        def courseExecution = new CourseExecution(course1, COURSE_ABREV, ACADEMIC_TERM, Course.Type.TECNICO)
        courseExecutionRepository.save(courseExecution)

        //Creates a different course execution
        def diffCourseExecution = new CourseExecution(course1, COURSE_ABREV, DIFF_ACADEMIC_TERM, Course.Type.TECNICO)
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
        courseExec = courseExecutionRepository.findAll().get(0)
        COURSE_EXEC_ID = courseExec.getId()

        //Creates an available question with the given topic
        def question = new Question()
        question.setCourse(courseExec.getCourse())
        question.addTopic(topic)
        question.setTitle(QUESTION_TITLE)
        question.setStatus(Question.Status.AVAILABLE)
        questionRepository.save(question)


        //Creates an open tournament
        def diffCourseExec = courseExecutionRepository.findAll().get(1)
        DIFF_COURSE_EXEC_ID = diffCourseExec.getId()

        //Creates a user
        def tmp_user1 = new User()
        tmp_user1.setKey(1)
        tmp_user1.setRole(User.Role.STUDENT)
        tmp_user1.addCourse(courseExec)
        userRepository.save(tmp_user1)
        USER = userRepository.findAll().get(0)
        USER_ID = USER.getId()

        //Creates a teacher
        def tmp_user2 = new User()
        tmp_user2.setKey(2)
        tmp_user2.setRole(User.Role.TEACHER)
        tmp_user2.addCourse(courseExec)
        userRepository.save(tmp_user2)
        TEACHER = userRepository.findAll().get(1)
        TEACHER_ID = TEACHER.getId()

        def tournamentDto = new TournamentDto()
        tournamentDto.setTitle(TOURNAMENT_TITLE)
        tournamentDto.setStartTime(TOMORROW)
        tournamentDto.setFinishTime(LATER)
        tournamentDto.setTopics(TOPIC_LIST)
        tournamentDto.setNumberOfQuestions(NUMBER_QUESTIONS)

        openTournamentDto = tournamentDto
        tournamentService.createTournament(openTournamentDto, COURSE_EXEC_ID, USER_ID)
        openTournament = tournamentRepository.findAll().get(0)
        openTournamentId = openTournament.getId()

        diffExecOpenTournamentDto = tournamentDto
        tournamentService.createTournament(diffExecOpenTournamentDto, DIFF_COURSE_EXEC_ID, USER_ID)
        diffExecOpenTournament = tournamentRepository.findAll().get(1)
        diffExecOpenTournamentId = diffExecOpenTournament.getId()
    }

    def "user is teacher and the user to ban is a student"(){
        when:
        tournamentService.banStudentFromTournaments(TEACHER_ID, USER_ID)

        then:"student cant join tournament"
        def student = userRepository.findAll().get(0)
        student.getIsBanned()

        tournamentService.joinTournament(openTournament.getId(), USER_ID)

        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.USER_IS_BANNED
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
        DashboardService dashboardService() {
            return new DashboardService()
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