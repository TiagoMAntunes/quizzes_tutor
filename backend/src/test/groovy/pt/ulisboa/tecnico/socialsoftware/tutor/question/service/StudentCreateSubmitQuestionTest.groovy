package pt.ulisboa.tecnico.socialsoftware.tutor.question.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.QuestionService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.TopicRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification

@DataJpaTest
class StudentCreateSubmitQuestionTest extends Specification {
    public static final String COURSE_NAME = "Software Architecture"
    public static final String INVALID_COURSE_NAME = "Invalid_Course"
    public static final String TITLE_NAME = "Main_Title"
    public static final String TOPIC_NAME = "Main_Topic"
    public static final String INVALID_TOPIC_NAME = "Invalid_Topic"
    public static final String BODY_TEXT = "Main_Body_Text"
    public static final String BLANK = "    "


    @Autowired
    UserRepository userRepository

    @Autowired
    CourseRepository courseRepository

    @Autowired
    TopicRepository topicRepository

    def student
    def course
    def topic
    def bodyText

    def setup() {
        questionService = new QuestionService()
    }

    def "the course and topic exist and title and body of text is valid "() {
        // the question is created or submited
        expect: false
    }

    def "not the student that create the question"() {
        // an exception is thrown
        expect: false
    }

    def "title already exists"() {
        // an exception is thrown
        expect: false
    }

    def "title is blank"() {
        // an exception is thrown
        expect: false
    }

    def "title is empty"() {
        // an exception is thrown
        expect: false
    }

    def "body of text is blank"() {
        // an exception is thrown
        expect: false
    }

    def "body of text is empty"() {
        // an exception is thrown
        expect: false
    }

    def "course is empty"() {
        // an exception is thrown
        expect: false
    }

    def "course is blank"() {
        // an exception is thrown
        expect: false
    }

    def "course doesnt exist"() {
        // an exception is thrown
        expect: false
    }

    def "topic is empty"() {
        // an exception is thrown
        expect: false
    }

    def "topic is blank"() {
        // an exception is thrown
        expect: false
    }

    def "topic doesnt exist"() {
        // an exception is thrown
        expect: false
    }
}