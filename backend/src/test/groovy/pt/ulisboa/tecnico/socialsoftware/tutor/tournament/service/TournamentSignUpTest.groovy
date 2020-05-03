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
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.QuizService
import spock.lang.Specification

import java.util.function.Predicate; 
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


@DataJpaTest
class TournamentSignUpTest extends Specification {
    public static final int NUMBER_QUESTIONS = 1
    public static final String TOPIC_NAME = "Main_Topic"
    public static final String COURSE_ABREV = "Software Architecture"
    public static final String ACADEMIC_TERM = "1"
    public static final String DIFF_ACADEMIC_TERM = "2"

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
        tournamentDto.setStartTime(ONE_DAY_LATER)
        tournamentDto.setFinishTime(THREE_DAYS_LATER)
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

    def "tournament has ended"(){
        given:"a tournament that has already ended"
        openTournament.setFinishTime(LocalDateTime.parse(THREE_DAYS_EARLIER, formatter))

        when:
        tournamentService.joinTournament(openTournamentId, USER_ID)

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.TOURNAMENT_NOT_OPEN

        def tournament = tournamentRepository.findAll().get(0)
        def user = userRepository.findAll().get(0)
        !tournament.hasSignedUp(user)
        !user.hasTournament(tournament)
    }

    def "tournament has already started"(){
        given:"a tournament that has already started"
        openTournament.setStartTime(LocalDateTime.parse(THREE_DAYS_EARLIER, formatter))

        when:
        tournamentService.joinTournament(openTournamentId, USER_ID)

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.TOURNAMENT_NOT_OPEN

        def tournament = tournamentRepository.findAll().get(0)
        def user = userRepository.findAll().get(0)
        !tournament.hasSignedUp(user)
        !user.hasTournament(tournament)
    }

    def "tournament is open and student hasnt signed up for it yet"(){
        when:
        tournamentService.joinTournament(openTournament.getId(), USER_ID)

        then:
        def tournament = tournamentRepository.findAll().get(0)
        def user = userRepository.findAll().get(0)
        tournament.hasSignedUp(user)
        user.hasTournament(tournament)
    }

    def "tournament is open and student has already signed up for it"(){
        given:"an open tournament that the student has already joined"
        tournamentService.joinTournament(openTournamentId, USER_ID)

        when:
        tournamentService.joinTournament(openTournamentId, USER_ID)

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.TOURNAMENT_ALREADY_JOINED
    }

    def "tournament is open but user isnt a student"(){
        when:
        tournamentService.joinTournament(openTournamentId, TEACHER_ID)

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.TOURNAMENT_JOIN_WRONG_ROLE

        def tournament = tournamentRepository.findAll().get(0)
        def user = userRepository.findAll().get(1)
        !tournament.hasSignedUp(user)
        !user.hasTournament(tournament)
    }

    def "no user with the given id"(){
        given:"an unused user id"
        
        int userId = userRepository.getMaxUserNumber() + 1

        when:
        tournamentService.joinTournament(openTournamentId, userId)

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.USER_NOT_FOUND

        def tournament = tournamentRepository.findAll().get(0)
        !tournament.hasSignedUpWithId(userId);
    }

    def "no tournament with the given id"(){
        given:"an unused tournament id"
        def tournamentId = 1
        def tournament = new Tournament();
        tournament.setId(tournamentId)

        when:
        tournamentService.joinTournament(tournamentId, USER_ID)

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.TOURNAMENT_NOT_FOUND

        def user = userRepository.findAll().get(0)
        !user.hasTournament(tournament)
    }

    def "tournament is open but belongs to a different course execution"(){
        when:
        tournamentService.joinTournament(diffExecOpenTournamentId, USER_ID)

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.TOURNAMENT_DIFF_COURSE_EXEC

        def tournament = tournamentRepository.findAll().get(1)
        def user = userRepository.findAll().get(0)
        !tournament.hasSignedUp(user)
        !user.hasTournament(tournament)
    }

    def "tournament generates quiz when has 2 sign ups"() {
        given: "two students"
        def student1 = new User()
        student1.setKey(userRepository.getMaxUserNumber() + 1)
        student1.setRole(User.Role.STUDENT)
        student1.addCourse(courseExec)

        def student2 = new User()
        student2.setKey(userRepository.getMaxUserNumber() + 2)
        student2.setRole(User.Role.STUDENT)
        student2.addCourse(courseExec)

        userRepository.save(student1)
        userRepository.save(student2)

        student1 = userRepository.findByKey(student1.getKey())
        student2 = userRepository.findByKey(student2.getKey())

        when: "both of them register to the tournament"
        tournamentService.joinTournament(openTournamentId, student1.getId())
        tournamentService.joinTournament(openTournamentId, student2.getId())

        then: "tournament should have the tournament quiz generated"
        def tournament = tournamentRepository.findAll().get(0)
        def quiz = tournament.getQuiz()
        quiz.getType() == Quiz.QuizType.TOURNAMENT
        quiz.getAvailableDate() == tournament.getStartTime()
        quiz.getConclusionDate() == tournament.getFinishTime()
        quiz.getResultsDate() == quiz.getConclusionDate() // results must be available after the end
        quiz.getQuizQuestions().size() == tournament.getNumberOfQuestions()
        quiz.getQuizQuestions().stream() // Check if all the questions are of at least one of the given topics
                            .allMatch({question -> question.getQuestion().getTopics().stream()
                                                        .anyMatch({topic -> tournament.getTopics().contains(topic)} as Predicate<Topic>)} as Predicate<Question>)

    }

    def "tournament generates quiz when creator and another student join the tournament"() {
        given: "a student and the tournament creator"
        def student = new User()
        student.setKey(userRepository.getMaxUserNumber() + 1)
        student.setRole(User.Role.STUDENT)
        student.addCourse(courseExec)
        userRepository.save(student)

        student = userRepository.findByKey(student.getKey())
        def creator = USER

        when: "they sign up for the tournament"
        tournamentService.joinTournament(openTournamentId, student.getId())
        tournamentService.joinTournament(openTournamentId, creator.getId())

        then:
        tournamentRepository.findAll().get(0).getQuiz() != null
    }

    def "tournament has not generated quiz with 1 student"() {
        given: "a student"
        def student = new User()
        student.setKey(userRepository.getMaxUserNumber() + 1)
        student.setRole(User.Role.STUDENT)
        student.addCourse(courseExec)
        userRepository.save(student)

        student = userRepository.findByKey(student.getKey())

        when: "signs up for the tournament"
        tournamentService.joinTournament(openTournamentId, student.getId())

        then: "no quiz is generated"
        tournamentRepository.findAll().get(0).getQuiz() == null
    }

    def "tournament has quiz with more than 2 students signedup "() {
        given: "three students"
        def student1 = new User()
        student1.setKey(userRepository.getMaxUserNumber() + 1)
        student1.setRole(User.Role.STUDENT)
        student1.addCourse(courseExec)

        def student2 = new User()
        student2.setKey(userRepository.getMaxUserNumber() + 2)
        student2.setRole(User.Role.STUDENT)
        student2.addCourse(courseExec)

        def student3 = new User()
        student3.setKey(userRepository.getMaxUserNumber() + 3)
        student3.setRole(User.Role.STUDENT)
        student3.addCourse(courseExec)

        userRepository.save(student1)
        userRepository.save(student2)
        userRepository.save(student3)

        student1 = userRepository.findByKey(student1.getKey())
        student2 = userRepository.findByKey(student2.getKey())
        student3 = userRepository.findByKey(student3.getKey())

        when: "all of them register to the tournament"
        tournamentService.joinTournament(openTournamentId, student1.getId())
        tournamentService.joinTournament(openTournamentId, student2.getId())
        tournamentService.joinTournament(openTournamentId, student3.getId())

        then: "The quiz must be generated"
        tournamentRepository.findAll().get(0).getQuiz() != null
    }

    def "tournament has not generated quiz with only the creator signed up"() {
        given: "the creator of the tournamnet"
        def creator = USER

        when: "signs up for the tournament"
        tournamentService.joinTournament(openTournamentId, creator.getId())

        then: "no quiz is generated"
        tournamentRepository.findAll().get(0).getQuiz() == null
    }

    def "the quiz does not have enough questions and resizes its number of questions"() {
        given: "a tournament with 2 questions and only 1 question in the course"
        openTournament.setNumberOfQuestions(2)
        tournamentRepository.save(openTournament)


        and: "two students"
        def student1 = new User()
        student1.setKey(userRepository.getMaxUserNumber() + 1)
        student1.setRole(User.Role.STUDENT)
        student1.addCourse(courseExec)

        def student2 = new User()
        student2.setKey(userRepository.getMaxUserNumber() + 2)
        student2.setRole(User.Role.STUDENT)
        student2.addCourse(courseExec)

        userRepository.save(student1)
        userRepository.save(student2)

        when: "both of them register to the tournament"
        tournamentService.joinTournament(openTournamentId, student1.getId())
        tournamentService.joinTournament(openTournamentId, student2.getId())

        then:
        def tournament = tournamentRepository.findById(openTournamentId).get()
        tournament.getQuiz().getQuizQuestions().size() == 1
        tournament.getNumberOfQuestions() == 1
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