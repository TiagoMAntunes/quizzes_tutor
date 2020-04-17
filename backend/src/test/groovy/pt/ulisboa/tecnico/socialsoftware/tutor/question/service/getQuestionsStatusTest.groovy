package pt.ulisboa.tecnico.socialsoftware.tutor.question.service

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
import pt.ulisboa.tecnico.socialsoftware.tutor.question.StudentQuestionService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.StudentQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.StudentQuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification

@DataJpaTest
class getQuestionsStatusTest extends Specification {
    public static final String COURSE_NAME = "Software Architecture"
    public static final String ACRONYM = "AS1"
    public static final String ACADEMIC_TERM = "1 SEM"
    public static final String QUESTION_TITLE = "question title"
    public static final String QUESTION_CONTENT = "question content"
    public static final String OPTION_CONTENT = "optionId content"
    public static final String EXPLANATION = "explanation"

    @Autowired
    StudentQuestionService studentQuestionService

    @Autowired
    UserRepository userRepository

    @Autowired
    CourseRepository courseRepository

    @Autowired
    CourseExecutionRepository courseExecutionRepository

    @Autowired
    StudentQuestionRepository studentQuestionRepository

    def student;
    def course;
    def questionDto;
    def options;
    def courseExecution;

    def setup() {
        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)
        courseExecution = new CourseExecution(course, ACRONYM, ACADEMIC_TERM, Course.Type.TECNICO)
        courseExecutionRepository.save(courseExecution)

        student = new User("User2", "Student1", 2, User.Role.STUDENT)
        userRepository.save(student)
        student.addCourse(courseExecution)

        questionDto = new QuestionDto()
        questionDto.setKey(1)
        questionDto.setTitle(QUESTION_TITLE)
        questionDto.setContent(QUESTION_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())

        def optionDto = new OptionDto()
        optionDto.setContent(OPTION_CONTENT)
        optionDto.setCorrect(true)
        options = new ArrayList<OptionDto>()
        options.add(optionDto)
        questionDto.setOptions(options)
    }

    def "no questions submitted by the student"(){
        when:
        studentQuestionService.getStudentQuestions(course.getId(), student.getId())

        then: "an exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.NO_QUESTION_SUBMITTED
        studentQuestionRepository.count() == 0L
    }

    def "get status of question that is waiting for approval"(){
        given: "a student that has submitted a question"
        studentQuestionService.createStudentQuestion(course.getId(), questionDto, student.getId())

        when:
        def result = studentQuestionService.getStudentQuestions(course.getId(), student.getId())

        then:
        result.size() == 1
        result.get(0).getQuestionStatus() == 'PENDING'
        result.get(0).getId() != null
        result.get(0).getUserDto().getId() == student.getId();
    }

    def "get status of a question that was approved question"(){
        given: "a student that has submitted a question that was approved"
        studentQuestionService.createStudentQuestion(course.getId(), questionDto, student.getId())
        def question = studentQuestionRepository.findAll().get(0)
        studentQuestionService.studentQuestionApproveReject(question.getId(), StudentQuestion.QuestionStatus.APPROVED)

        when:
        def result = studentQuestionService.getStudentQuestions(course.getId(), student.getId())

        then:
        result.size() == 1
        result.get(0).getQuestionStatus() == 'APPROVED'
        result.get(0).getId() != null
        result.get(0).getUserDto().getId() == student.getId();
    }

    def "get status of a question that was rejected with no explanation"(){
        given: "a student that has submitted a question that was rejected"
        studentQuestionService.createStudentQuestion(course.getId(), questionDto, student.getId())
        def question = studentQuestionRepository.findAll().get(0)
        studentQuestionService.studentQuestionApproveReject(question.getId(), StudentQuestion.QuestionStatus.REJECTED)

        when:
        def result = studentQuestionService.getStudentQuestions(course.getId(), student.getId())

        then:
        result.size() == 1
        result.get(0).getQuestionStatus() == 'REJECTED'
        result.get(0).getId() != null
        result.get(0).getUserDto().getId() == student.getId();
        result.get(0).getRejectionExplanation() == null
    }

    def "get status of a question that was rejected with an explanation"(){
        given: "a student that has submitted a question that was rejected"
        studentQuestionService.createStudentQuestion(course.getId(), questionDto, student.getId())
        def question = studentQuestionRepository.findAll().get(0)
        studentQuestionService.studentQuestionApproveReject(question.getId(), StudentQuestion.QuestionStatus.REJECTED)
        studentQuestionService.setStudentQuestionExplanation(question.getId(), EXPLANATION)

        when:
        def result = studentQuestionService.getStudentQuestions(course.getId(), student.getId())

        then:
        result.size() == 1
        result.get(0).getQuestionStatus() == 'REJECTED'
        result.get(0).getId() != null
        result.get(0).getUserDto().getId() == student.getId();
        result.get(0).getRejectionExplanation() == EXPLANATION
    }

    def "get status of two submitted questions: one approved question and one rejected with an explanation"(){
        given: "a student that has submitted two questions, one approved and another rejected with an explanation"
        studentQuestionService.createStudentQuestion(course.getId(), questionDto, student.getId())
        def question = studentQuestionRepository.findAll().get(0)
        studentQuestionService.studentQuestionApproveReject(question.getId(), StudentQuestion.QuestionStatus.APPROVED)
        def questionDto2 = new QuestionDto()
        questionDto2.setKey(2)
        questionDto2.setTitle(QUESTION_TITLE)
        questionDto2.setContent(QUESTION_CONTENT)
        questionDto2.setStatus(Question.Status.AVAILABLE.name())
        questionDto2.setOptions(options)
        studentQuestionService.createStudentQuestion(course.getId(), questionDto2, student.getId())
        def question2 = studentQuestionRepository.findAll().get(1)
        studentQuestionService.studentQuestionApproveReject(question2.getId(), StudentQuestion.QuestionStatus.REJECTED)
        studentQuestionService.setStudentQuestionExplanation(question2.getId(), EXPLANATION)

        when:
        def result = studentQuestionService.getStudentQuestions(course.getId(), student.getId())

        then:
        result.size() == 2
        result.get(0).getQuestionStatus() == 'APPROVED'
        result.get(0).getId() != null
        result.get(0).getUserDto().getId() == student.getId();
        result.get(1).getQuestionStatus() == 'REJECTED'
        result.get(1).getId() != null
        result.get(1).getUserDto().getId() == student.getId();
        result.get(1).getRejectionExplanation() == EXPLANATION
    }
    @TestConfiguration
    static class StudentQuestionServiceImplTestContextConfiguration {

        @Bean
        StudentQuestionService studentQuestionService() {
            return new StudentQuestionService()
        }
    }
}