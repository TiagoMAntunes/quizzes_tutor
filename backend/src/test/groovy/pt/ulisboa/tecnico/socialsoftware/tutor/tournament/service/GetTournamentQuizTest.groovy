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
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.AnswersXmlImport
import pt.ulisboa.tecnico.socialsoftware.tutor.question.QuestionService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.TopicRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.QuizService
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentService
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.domain.Tournament
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto.TournamentDto
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.repository.TournamentRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.function.Predicate

@DataJpaTest
class GetTournamentQuizTest extends Specification {
    public static final int NUMBER_QUESTIONS = 1
    public static final String TOPIC_NAME = "Main_Topic"
    public static final String COURSE_ABREV = "Software Architecture"
    public static final String ACADEMIC_TERM = "1"

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
    QuizAnswerRepository quizAnswerRepository

    def formatter
    def NOW_TIME
    def FIVE_DAYS_EARLIER
    def THREE_DAYS_EARLIER
    def TWO_DAYS_EARLIER
    def FIVE_DAYS_LATER
    def THREE_DAYS_LATER
    def ONE_DAY_LATER

    def CREATOR_ID
    def STUDENT_JOINED_ID
    def STUDENT_NOT_JOINED_ID

    def openTournament
    def openTournamentId


    def setup() {
        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        NOW_TIME = LocalDateTime.now().format(formatter)
        FIVE_DAYS_LATER = LocalDateTime.now().plusDays(5).format(formatter)
        THREE_DAYS_LATER = LocalDateTime.now().plusDays(3).format(formatter)
        FIVE_DAYS_EARLIER = LocalDateTime.now().minusDays(5).format(formatter)
        THREE_DAYS_EARLIER = LocalDateTime.now().minusDays(3).format(formatter)
        TWO_DAYS_EARLIER = LocalDateTime.now().minusDays(2).format(formatter)
        ONE_DAY_LATER = LocalDateTime.now().plusDays(1).format(formatter)

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
        creator.addCourse(courseExec)
        userRepository.save(creator)

        CREATOR_ID = userRepository.findByKey(creator.getKey()).getId()


        def student1 = new User()
        student1.setKey(userRepository.getMaxUserNumber() + 1)
        student1.setRole(User.Role.STUDENT)
        student1.addCourse(courseExec)
        userRepository.save(student1)

        STUDENT_JOINED_ID = userRepository.findByKey(student1.getKey()).getId()

        def student2 = new User()
        student2.setKey(userRepository.getMaxUserNumber() + 1)
        student2.setRole(User.Role.STUDENT)
        student2.addCourse(courseExec)
        userRepository.save(student2)

        STUDENT_NOT_JOINED_ID = userRepository.findByKey(student2.getKey()).getId()

        def tournamentDto = new TournamentDto()
        tournamentDto.setStartTime(ONE_DAY_LATER)
        tournamentDto.setFinishTime(THREE_DAYS_LATER)
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

        openTournament.setStartTime(LocalDateTime.parse(THREE_DAYS_EARLIER, formatter))

        when:
        tournamentService.getTournamentQuiz(openTournamentId, CREATOR_ID)

        then:
        noExceptionThrown()
        quizAnswerRepository.findAll().size() == 1
    }

    def "Tournament quiz can be answered but hasn't joined"() {
        given: "A tournament with quiz associated that has started"
        tournamentService.joinTournament(openTournamentId, STUDENT_JOINED_ID)

        openTournament.setStartTime(LocalDateTime.parse(THREE_DAYS_EARLIER, formatter))

        when:
        tournamentService.getTournamentQuiz(openTournamentId, STUDENT_NOT_JOINED_ID)

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.TOURNAMENT_QUIZ_NOT_JOINED
        quizAnswerRepository.findAll().size() == 0
    }

    def "Tournament quiz generated but hasn't started"() {
        given: "A tournament with quiz associated that hasn't started"
        tournamentService.joinTournament(openTournamentId, STUDENT_JOINED_ID)

        when:
        tournamentService.getTournamentQuiz(openTournamentId, STUDENT_JOINED_ID)

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.TOURNAMENT_QUIZ_NOT_YET_AVAILABLE
    }

    def "Tournament quiz already finished"() {
        given: "A tournament with quiz associated that has ended"
        tournamentService.joinTournament(openTournamentId, STUDENT_JOINED_ID)

        openTournament.setStartTime(LocalDateTime.parse(THREE_DAYS_EARLIER, formatter))
        openTournament.setFinishTime(LocalDateTime.parse(TWO_DAYS_EARLIER, formatter))

        when:
        tournamentService.getTournamentQuiz(openTournamentId, STUDENT_JOINED_ID)

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.TOURNAMENT_QUIZ_NO_LONGER_AVAILABLE
        quizAnswerRepository.findAll().size() == 0
    }

    def "Tournament quiz already answered"() {
        given: "A tournament with quiz associated that has started"
        tournamentService.joinTournament(openTournamentId, STUDENT_JOINED_ID)
        openTournament.setStartTime(LocalDateTime.parse(THREE_DAYS_EARLIER, formatter))

        and: "has been answered"
        tournamentService.getTournamentQuiz(openTournamentId, CREATOR_ID) // generates quiz answer
        quizAnswerRepository.findAll().get(0).setCompleted(true)

        when:
        tournamentService.getTournamentQuiz(openTournamentId, CREATOR_ID)

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.TOURNAMENT_QUIZ_ALREADY_COMPLETED
        quizAnswerRepository.findAll().size() == 1
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
