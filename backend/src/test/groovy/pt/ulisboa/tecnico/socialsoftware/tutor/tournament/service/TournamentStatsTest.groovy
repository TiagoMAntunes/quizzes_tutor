package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.AnswerService
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.repository.QuizAnswerRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.DashboardService
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.AnswersXmlImport
import pt.ulisboa.tecnico.socialsoftware.tutor.question.QuestionService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.TopicRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.QuizService
import pt.ulisboa.tecnico.socialsoftware.tutor.statement.dto.StatementAnswerDto
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentService
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto.TournamentDto
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.repository.TournamentRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.config.DateHandler
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserService;
import spock.lang.Specification

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@DataJpaTest
class TournamentStatsTest extends Specification {
    public static final int NUMBER_QUESTIONS = 1
    public static final String TOPIC_NAME = "Main_Topic"
    public static final String COURSE_ABREV = "Software Architecture"
    public static final String ACADEMIC_TERM = "1"
    public static final String QUESTION_TITLE = "Question"
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
    def USER
    def USER_ID
    def openTournamentDto
    def openTournament
    def openTournamentId
    def openTournament2
    def openTournament2Id
    def courseExec
    def optionOk


    def setup() {

        //Creates a course
        def course1 = new Course(COURSE_ABREV, Course.Type.TECNICO)
        courseRepository.save(course1)

        //Creates a course execution
        def courseExecution = new CourseExecution(course1, COURSE_ABREV, ACADEMIC_TERM, Course.Type.TECNICO)
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
        courseExec = courseExecutionRepository.findAll().get(0)
        COURSE_EXEC_ID = courseExec.getId()

        //Creates an available question with the given topic
        def question = new Question()
        question.setCourse(courseExec.getCourse())
        question.addTopic(topic)
        question.setTitle(QUESTION_TITLE)
        question.setStatus(Question.Status.AVAILABLE)
        questionRepository.save(question)

        //Correct option
        optionOk = new Option()
        optionOk.setContent("Option Content")
        optionOk.setCorrect(true)
        optionOk.setSequence(1)
        optionOk.setQuestion(question)

        //Creates a user
        def tmp_user1 = new User()
        tmp_user1.setKey(1)
        tmp_user1.setRole(User.Role.STUDENT)
        tmp_user1.addCourse(courseExec)
        userRepository.save(tmp_user1)
        USER = userRepository.findAll().get(0)
        USER_ID = USER.getId()

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

        tournamentService.createTournament(openTournamentDto, COURSE_EXEC_ID, USER_ID)
        openTournament2 = tournamentRepository.findAll().get(1)
        openTournament2Id = openTournament2.getId()
    }


    def "tournament has quiz and user has concluded it"(){
        given: "a student and the tournament creator"
        def student = new User()
        student.setKey(userRepository.getMaxUserNumber() + 1)
        student.setRole(User.Role.STUDENT)
        student.addCourse(courseExec)
        userRepository.save(student)

        student = userRepository.findByKey(student.getKey())
        def creator = USER

        and: "they sign up for the tournament"
        tournamentService.joinTournament(openTournamentId, student.getId())
        tournamentService.joinTournament(openTournamentId, creator.getId())

        and: "the student concludes the quiz incorrectly"
        def quiz = tournamentRepository.findAll().get(0).getQuiz()
        quiz.setAvailableDate(YESTERDAY)
        def quizAnswer = new QuizAnswer(student, quiz)
        quizAnswerRepository.save(quizAnswer)

        student.addQuizAnswer(quizAnswer)
        answerService.concludeQuiz(student, quiz.getId())

        when:
        def studentDash = dashboardService.getStudentDashboard(student.getId(), COURSE_EXEC_ID)
        def creatorDash = dashboardService.getStudentDashboard(creator.getId(), COURSE_EXEC_ID)

        then:"the student has participated in 1 tournament, and the creator none, both have 0 avg score"
        studentDash.getParticipatedTournamentsNumber() == 1
        studentDash.getNotYetParticipatedTournamentsNumber() == 0
        studentDash.getAverageTournamentScore() == 0;

        creatorDash.getParticipatedTournamentsNumber() == 0
        creatorDash.getNotYetParticipatedTournamentsNumber() == 1
        creatorDash.getAverageTournamentScore() == 0;
    }


    def "user participates in multiple tournaments"(){
        given: "a student and the tournament creator"
        def student = new User()
        student.setKey(userRepository.getMaxUserNumber() + 1)
        student.setRole(User.Role.STUDENT)
        student.addCourse(courseExec)
        userRepository.save(student)

        student = userRepository.findByKey(student.getKey())
        def creator = USER

        and: "they sign up for both tournaments"
        tournamentService.joinTournament(openTournamentId, student.getId())
        tournamentService.joinTournament(openTournamentId, creator.getId())
        tournamentService.joinTournament(openTournament2Id, student.getId())
        tournamentService.joinTournament(openTournament2Id, creator.getId())

        and: "the student concludes a tournament quiz correctly"
        def quiz = tournamentRepository.findAll().get(0).getQuiz()
        quiz.setAvailableDate(YESTERDAY)
        def quizAnswer = new QuizAnswer(student, quiz)
        def statementAnswerDto = new StatementAnswerDto()
        statementAnswerDto.setOptionId(optionOk.getId())
        statementAnswerDto.setSequence(0)
        statementAnswerDto.setTimeTaken(100)
        answerService.submitAnswer(student, quiz.getId(), statementAnswerDto)
        student.addQuizAnswer(quizAnswer)
        answerService.concludeQuiz(student, quiz.getId())

        and: "the student concludes a tournament quiz incorrectly"
        quiz = tournamentRepository.findAll().get(1).getQuiz()
        quiz.setAvailableDate(YESTERDAY)
        quizAnswer = new QuizAnswer(student, quiz)
        quizAnswerRepository.save(quizAnswer)
        student.addQuizAnswer(quizAnswer)
        answerService.concludeQuiz(student, quiz.getId())

        when:
        def student1Dash = dashboardService.getStudentDashboard(student.getId(), COURSE_EXEC_ID)
        def student2Dash = dashboardService.getStudentDashboard(creator.getId(), COURSE_EXEC_ID)

        then:"the student has participated in 2 tournaments and has a 50% average tournament score"
        student1Dash.getParticipatedTournamentsNumber() == 2
        student1Dash.getNotYetParticipatedTournamentsNumber() == 0
        student1Dash.getAverageTournamentScore() == 50;

        student2Dash.getParticipatedTournamentsNumber() == 0
        student2Dash.getNotYetParticipatedTournamentsNumber() == 2
        student2Dash.getAverageTournamentScore() == 0
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

        @Bean
        DashboardService dashboardService() {
            return new DashboardService()
        }

        @Bean
        UserService userService() {
            return new UserService()
        }
    }
}
