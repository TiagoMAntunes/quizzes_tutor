package pt.ulisboa.tecnico.socialsoftware.tutor.statement.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.AnswerService
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.repository.QuizAnswerRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.config.DateHandler
import pt.ulisboa.tecnico.socialsoftware.tutor.course.*
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.AnswersXmlImport
import pt.ulisboa.tecnico.socialsoftware.tutor.question.QuestionService
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.QuizService
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.repository.QuizRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.statement.StatementService
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.TopicRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto.TournamentDto
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentService
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.domain.Tournament
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.repository.TournamentRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.config.DateHandler
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserService;
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@DataJpaTest
class GetTournamentQuizTest extends Specification {
    static final USERNAME = 'username'
    public static final String COURSE_NAME = "Software Architecture"
    public static final String ACRONYM = "AS1"
    public static final String ACADEMIC_TERM = "1 SEM"
    public static final String QUIZ_TITLE = "Quiz title"

    public static final int NUMBER_QUESTIONS = 1
    public static final String TOPIC_NAME = "Main_Topic"
    public static final String COURSE_ABREV = "Software Architecture"
    public static final String TOURNAMENT_TITLE = "title"

    public static final LocalDateTime BEFORE = DateHandler.now().minusDays(2)
    public static final LocalDateTime YESTERDAY = DateHandler.now().minusDays(1)
    public static final String TOMORROW = DateHandler.toISOString(DateHandler.now().plusDays(1))
    public static final String LATER = DateHandler.toISOString(DateHandler.now().plusDays(2))

    @Autowired
    TournamentRepository tournamentRepository

    @Autowired
    TournamentService tournamentService

    @Autowired
    TopicRepository topicRepository

    @Autowired
    QuestionRepository questionRepository

    @Autowired
    StatementService statementService

    @Autowired
    UserRepository userRepository

    @Autowired
    CourseRepository courseRepository

    @Autowired
    CourseExecutionRepository courseExecutionRepository

    @Autowired
    QuizRepository quizRepository

    @Autowired
    QuizAnswerRepository quizAnswerRepository

    def course
    def courseExecution

    def CREATOR_ID
    def STUDENT_JOINED_ID
    def STUDENT_NOT_JOINED_ID

    def openTournament
    def openTournamentId

    def setup() {
        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)

        courseExecution = new CourseExecution(course, ACRONYM, ACADEMIC_TERM, Course.Type.TECNICO)
        courseExecutionRepository.save(courseExecution)

         //Creates a topic
        def topic = new Topic()
        topic.setName(TOPIC_NAME)
        topic.setCourse(course)
        course.addTopic(topic)
        topic = topicRepository.save(topic)

        //Creates a topicDTO list with 1 element
        def topicList = new ArrayList<TopicDto>()
        topicList.add(new TopicDto(topic))

        //Creates an open tournament
        def courseExec = courseExecutionRepository.findAll().get(0)
        def courseExecId = courseExec.getId()

        //Creates an available question with the given topic
        def question = new Question()
        question.setCourse(courseExec.getCourse())
        question.addTopic(topic)
        question.setTitle("Question")
        question.setStatus(Question.Status.AVAILABLE)
        questionRepository.save(question)


        def creator = new User()
        creator.setKey(1)
        creator.setRole(User.Role.STUDENT)
        creator.getCourseExecutions().add(courseExecution)
        courseExecution.getUsers().add(creator)
        userRepository.save(creator)

        CREATOR_ID = userRepository.findByKey(creator.getKey()).getId()

        def student1 = new User()
        student1.setKey(userRepository.getMaxUserNumber() + 1)
        student1.setRole(User.Role.STUDENT)
        student1.getCourseExecutions().add(courseExecution)
        courseExecution.getUsers().add(student1)
        userRepository.save(student1)

        STUDENT_JOINED_ID = userRepository.findByKey(student1.getKey()).getId()

        def student2 = new User()
        student2.setKey(userRepository.getMaxUserNumber() + 1)
        student2.setRole(User.Role.STUDENT)
        student2.getCourseExecutions().add(courseExecution)
        courseExecution.getUsers().add(student2)
        userRepository.save(student2)

        STUDENT_NOT_JOINED_ID = userRepository.findByKey(student2.getKey()).getId()

        def tournamentDto = new TournamentDto()
        tournamentDto.setTitle(TOURNAMENT_TITLE)
        tournamentDto.setStartTime(TOMORROW)
        tournamentDto.setFinishTime(LATER)
        tournamentDto.setTopics(topicList)
        tournamentDto.setNumberOfQuestions(NUMBER_QUESTIONS)

        tournamentService.createTournament(tournamentDto, courseExecId, CREATOR_ID)
        openTournament = tournamentRepository.findAll().get(0)
        openTournamentId = openTournament.getId()

        tournamentService.joinTournament(openTournamentId, CREATOR_ID)
    }

    def "Tournament quiz can be answered"() {
        given: "A tournament with quiz associated that has started"
        tournamentService.joinTournament(openTournamentId, STUDENT_JOINED_ID)

        openTournament.getQuiz().setAvailableDate(YESTERDAY)

        when:
        def statementQuizDtos = statementService.getAvailableQuizzes(STUDENT_JOINED_ID, courseExecution.getId())

        then:
        statementQuizDtos.size() == 1
        def statementQuizDto = statementQuizDtos.get(0)
        statementQuizDto.getId() != null
        statementQuizDto.getQuizAnswerId() != null
        statementQuizDto.getTitle() == TOURNAMENT_TITLE
        !statementQuizDto.isOneWay()
        statementQuizDto.isForTournament()
        statementQuizDto.getAvailableDate() == DateHandler.toISOString(YESTERDAY)
        statementQuizDto.getConclusionDate() == LATER
        statementQuizDto.getTimeToAvailability() == null
    }

    def "Tournament quiz can be answered but didn't join"() {
        given: "A tournament with quiz associated that has started"
        tournamentService.joinTournament(openTournamentId, STUDENT_JOINED_ID)

        openTournament.getQuiz().setAvailableDate(YESTERDAY)

        when:
        def statementQuizDtos = statementService.getAvailableQuizzes(STUDENT_NOT_JOINED_ID, courseExecution.getId())

        then:
        statementQuizDtos.size() == 0
    }

    def "Tournament quiz generated but hasn't started"() {
        given: "A tournament with quiz associated that hasn't started"
        tournamentService.joinTournament(openTournamentId, STUDENT_JOINED_ID)

        when:
        def statementQuizDtos = statementService.getAvailableQuizzes(STUDENT_JOINED_ID, courseExecution.getId())

        then:
        statementQuizDtos.size() == 0
    }

    def "Tournament quiz already finished"() {
        given: "A tournament with quiz associated that has ended"
        tournamentService.joinTournament(openTournamentId, STUDENT_JOINED_ID)

        openTournament.getQuiz().setAvailableDate(BEFORE)
        openTournament.getQuiz().setConclusionDate(YESTERDAY)

        when:
        def statementQuizDtos = statementService.getAvailableQuizzes(STUDENT_JOINED_ID, courseExecution.getId())

        then:
        statementQuizDtos.size() == 0
    }

    def "Tournament quiz already answered"() {
        given: "A tournament with quiz associated that has started"
        tournamentService.joinTournament(openTournamentId, STUDENT_JOINED_ID)

        openTournament.getQuiz().setAvailableDate(YESTERDAY)

        and: "has been answered"
        statementService.getAvailableQuizzes(STUDENT_JOINED_ID, courseExecution.getId())
        quizAnswerRepository.findAll().get(0).setCompleted(true)

        when:
        def statementQuizDtos = statementService.getAvailableQuizzes(STUDENT_JOINED_ID, courseExecution.getId())

        then:
        statementQuizDtos.size() == 0
    }

    @TestConfiguration
    static class QuizServiceImplTestContextConfiguration {

        @Bean
        TournamentService tournamentService() {
            return new TournamentService()
        }

        @Bean
        StatementService statementService() {
            return new StatementService()
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
        QuizService quizService() {
            return new QuizService()
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
