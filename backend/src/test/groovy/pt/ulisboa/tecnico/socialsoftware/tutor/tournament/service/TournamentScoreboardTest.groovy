package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.AnswerService
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.repository.QuizAnswerRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.config.DateHandler
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
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
import spock.lang.Specification

import java.time.LocalDateTime

@DataJpaTest
class TournamentScoreboardTest extends Specification {
    public static final int NUMBER_QUESTIONS = 1
    public static final String TOPIC_NAME = "Main_Topic"
    public static final String COURSE_ABREV = "Software Architecture"
    public static final String ACADEMIC_TERM = "1"
    public static final String TOURNAMENT_NAME = "Tournament 1"
    public static final String QUESTION_TITLE = "Question"
    public static final LocalDateTime THREE_DAYS_EARLIER = DateHandler.now().minusDays(3)
    public static final String THREE_DAYS_LATER = DateHandler.toISOString(DateHandler.now().plusDays(3))
    public static final String ONE_DAY_LATER = DateHandler.toISOString(DateHandler.now().plusDays(1))

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

    def TOPIC_LIST
    def COURSE_EXEC_ID
    def USER
    def USER_ID
    def openTournamentDto
    def openTournament
    def openTournamentId
    def courseExec
    def optionOk
    def student1
    def student2


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
        tournamentDto.setTitle(TOURNAMENT_NAME)
        tournamentDto.setStartTime(ONE_DAY_LATER)
        tournamentDto.setFinishTime(THREE_DAYS_LATER)
        tournamentDto.setTopics(TOPIC_LIST)
        tournamentDto.setNumberOfQuestions(NUMBER_QUESTIONS)

        openTournamentDto = tournamentDto
        tournamentService.createTournament(openTournamentDto, COURSE_EXEC_ID, USER_ID)
        openTournament = tournamentRepository.findAll().get(0)
        openTournamentId = openTournament.getId()

        //creates 2 students
        student1 = new User()
        student1.setKey(userRepository.getMaxUserNumber() + 1)
        student1.setRole(User.Role.STUDENT)
        student1.addCourse(courseExec)
        userRepository.save(student1)

        student2 = new User()
        student2.setKey(userRepository.getMaxUserNumber() + 1)
        student2.setRole(User.Role.STUDENT)
        student2.addCourse(courseExec)
        userRepository.save(student2)

        student1 = userRepository.findByKey(student1.getKey())
        student2 = userRepository.findByKey(student2.getKey())
    }


    def "tournament has quiz and user has concluded it with 0 score"(){
        given: "a student and the tournament creator"
        def creator = USER

        when: "they sign up for the tournament"
        tournamentService.joinTournament(openTournamentId, student1.getId())
        tournamentService.joinTournament(openTournamentId, creator.getId())

        and: "the student concludes the quiz incorrectly"
        def quiz = tournamentRepository.findAll().get(0).getQuiz()
        quiz.setAvailableDate(THREE_DAYS_EARLIER)
        def quizAnswer = new QuizAnswer(student1, quiz)
        quizAnswerRepository.save(quizAnswer)

        student1.addQuizAnswer(quizAnswer)
        answerService.concludeQuiz(student1, quiz.getId())

        def scoreboard = tournamentService.getTournamentScoreboard(openTournamentId)

        then:"tournament has one participant with 0 avg score"
        scoreboard.getScores().size() == 1
        scoreboard.getAverageScore() == 0
    }


    def "two users participate in tournament quiz"(){
        when: "two students sign up for the tournament"
        tournamentService.joinTournament(openTournamentId, student1.getId())
        tournamentService.joinTournament(openTournamentId, student2.getId())

        and: "the first student concludes the tournament quiz correctly"
        def quiz = tournamentRepository.findAll().get(0).getQuiz()
        quiz.setAvailableDate(THREE_DAYS_EARLIER)
        def quizAnswer = new QuizAnswer(student1, quiz)
        def statementAnswerDto = new StatementAnswerDto()
        statementAnswerDto.setOptionId(optionOk.getId())
        statementAnswerDto.setSequence(0)
        statementAnswerDto.setTimeTaken(100)
        answerService.submitAnswer(student1, quiz.getId(), statementAnswerDto)
        student1.addQuizAnswer(quizAnswer)
        answerService.concludeQuiz(student1, quiz.getId())

        and: "the second student concludes the tournament quiz incorrectly"
        quizAnswer = new QuizAnswer(student2, quiz)
        quizAnswerRepository.save(quizAnswer)
        student2.addQuizAnswer(quizAnswer)
        answerService.concludeQuiz(student2, quiz.getId())

        def scoreboard = tournamentService.getTournamentScoreboard(openTournamentId)

        then:"two students have participated in the tournament and it has a 0.5 avg score"
        scoreboard.getScores().size() == 2
        scoreboard.getAverageScore() == 0.5
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
