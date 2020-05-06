package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.AnswerService
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.repository.QuizAnswerRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.config.DateHandler
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.AnswersXmlImport
import pt.ulisboa.tecnico.socialsoftware.tutor.question.QuestionService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.TopicRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.QuizService
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.repository.QuizRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.statement.StatementService
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
class TournamentPrivacyTest extends Specification {

    public static final String COURSE_NAME = "Software Architecture"
    public static final String ACRONYM = "AS1"
    public static final String ACADEMIC_TERM = "1 SEM"

    public static final int NUMBER_QUESTIONS = 1
    public static final String TOPIC_NAME = "Main_Topic"
    public static final String COURSE_ABREV = "Software Architecture"
    public static final String TOURNAMENT_TITLE = "title"

    public static final LocalDateTime YESTERDAY = DateHandler.now().minusDays(1)

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

    def CREATOR
    def STUDENT

    def openTournament
    def openTournamentId

    def setup() {
        def formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

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

        //Create a student that creates the tournament

        def creator = new User()
        creator.setKey(1)
        creator.setRole(User.Role.STUDENT)
        creator.getCourseExecutions().add(courseExecution)
        courseExecution.getUsers().add(creator)
        userRepository.save(creator)

        CREATOR_ID = userRepository.findByKey(creator.getKey()).getId()
        CREATOR = creator

        //Create student

        def student = new User()
        student.setKey(userRepository.getMaxUserNumber() + 1)
        student.setRole(User.Role.STUDENT)
        student.getCourseExecutions().add(courseExecution)
        courseExecution.getUsers().add(student)
        userRepository.save(student)

        STUDENT_JOINED_ID = userRepository.findByKey(student.getKey()).getId()
        STUDENT = student

        //Creates the tournament

        def tournamentDto = new TournamentDto()
        tournamentDto.setTitle(TOURNAMENT_TITLE)
        tournamentDto.setStartTime(LocalDateTime.now().plusDays(1).format(formatter))
        tournamentDto.setFinishTime(LocalDateTime.now().plusDays(2).format(formatter))
        tournamentDto.setTopics(topicList)
        tournamentDto.setNumberOfQuestions(NUMBER_QUESTIONS)

        tournamentService.createTournament(tournamentDto, courseExecId, CREATOR_ID)
        openTournament = tournamentRepository.findAll().get(0)
        openTournamentId = openTournament.getId()

        //Join the creator
        tournamentService.joinTournament(openTournamentId, CREATOR_ID)

        //Join the student
        tournamentService.joinTournament(openTournamentId, STUDENT_JOINED_ID)

        //Make quiz available
        openTournament.getQuiz().setAvailableDate(YESTERDAY)

        //Create quiz answers by students and set them as answered
        statementService.getAvailableQuizzes(CREATOR_ID, courseExecution.getId())
        statementService.getAvailableQuizzes(STUDENT_JOINED_ID, courseExecution.getId())
        quizAnswerRepository.findAll().get(0).setCompleted(true)
        quizAnswerRepository.findAll().get(1).setCompleted(true)
    }

    def "User sets tournament privacy to private"(){
        given: "a student"
        def student = STUDENT_JOINED_ID

        when: "sets his tournament privacy to private"
        student.setTournamentPrivacy(true)

        then:
        student.getTournamentPrivacy() == true
    }


    def "User sets tournament privacy to public"(){
        given: "a student"
        def student = STUDENT_JOINED_ID

        when: "sets his tournament privacy to private"
        student.setTournamentPrivacy(false)

        then:
        student.getTournamentPrivacy() == false
    }

    def "Get course tournaments information with all students private"() {
        given: "2 students that don't allow their information to be public"
        CREATOR.setTournamentPrivacy(true)
        STUDENT.setTournamentPrivacy(true)

        when: "tournament information is asked"
        def tournamentInformation = tournamentService.getCourseInformation(openTournamentId)

        then: "no information is displayed"
        tournamentInformation.getNumberOfParticipants() == 0
    }

    def "Get course tournaments information with 1 public student"() {
        given: "1 student that allows his information to be public, and 1 student that doesn't"
        CREATOR.setTournamentPrivacy(false)
        STUDENT.setTournamentPrivacy(true)

        when: "tournament information is asked"
        def tournamentInformation = tournamentService.getCourseInformation(openTournamentId)

        then: "Only 1 student shows up"
        tournamentInformation.getNumberOfParticipants() == 1
    }

    @TestConfiguration
    static class TournamentServiceImplTestContextConfiguration {

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

    }

}
