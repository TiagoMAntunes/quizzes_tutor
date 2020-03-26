package pt.ulisboa.tecnico.socialsoftware.tutor.question.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.StudentQuestionService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.QuestionService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.StudentQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.StudentQuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification

import java.util.ArrayList;

@DataJpaTest
class ApproveRejectQuestionServicePerformanceTest extends Specification {
    public static final String COURSE_NAME = "Software Architecture"
    public static final String ACRONYM = "AS1"
    public static final String ACADEMIC_TERM = "1 SEM"
    public static final String QUESTION_TITLE = "question title"
    public static final String QUESTION_CONTENT = "question content"
    public static final String OPTION_CONTENT = "optionId content"
    public static final String EXPLANATION = "explanation"

    public static final int N_STUDENT_QUESTIONS = 100

    @Autowired
    StudentQuestionService studentQuestionService


    @Autowired
    UserRepository userRepository

    @Autowired
    CourseRepository courseRepository

    @Autowired
    QuestionRepository questionRepository

    @Autowired
    CourseExecutionRepository courseExecutionRepository

    @Autowired
    StudentQuestionRepository studentQuestionRepository

    def course;
    def teacher;
    def student;
    def questionDto;
    def courseExecution;

    def setup() {
        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)
        courseExecution = new CourseExecution(course, ACRONYM, ACADEMIC_TERM, Course.Type.TECNICO)
        courseExecutionRepository.save(courseExecution)

        teacher = new User("User1", "teacher", 1, User.Role.TEACHER)
        userRepository.save(teacher)
        teacher.addCourse(courseExecution)
        student = new User("User2", "student", 2, User.Role.STUDENT)
        userRepository.save(student)
        student.addCourse(courseExecution)

        questionDto = new QuestionDto()
        questionDto.setTitle(QUESTION_TITLE)
        questionDto.setContent(QUESTION_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())

        def optionDto = new OptionDto()
        optionDto.setContent(OPTION_CONTENT)
        optionDto.setCorrect(true)
        def options = new ArrayList<OptionDto>()
        options.add(optionDto)
        questionDto.setOptions(options)
    }

    def "performance testing to get 750k student questions"() {
        given: "750k questions and student questions"
        1.upto(N_STUDENT_QUESTIONS, {key-> questionDto.setKey(key); studentQuestionService.createStudentQuestion(course.getId(), questionDto, student.getId())})

        when:
        1.upto(N_STUDENT_QUESTIONS, {questionId -> studentQuestionService.studentQuestionApproveReject(questionId, StudentQuestion.QuestionStatus.REJECTED, EXPLANATION, teacher.getId(), courseExecution.getId())})

        then:
        studentQuestionRepository.count() == N_STUDENT_QUESTIONS;
    }

    @TestConfiguration
    static class ApproveRejectQuestionServicePerformanceTestContextConfiguration {

        @Bean
        StudentQuestionService studentQuestionService() {
            return new StudentQuestionService()
        }
    }
}