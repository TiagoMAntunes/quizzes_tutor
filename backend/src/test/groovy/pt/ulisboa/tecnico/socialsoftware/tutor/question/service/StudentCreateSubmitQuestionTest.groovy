//package pt.ulisboa.tecnico.socialsoftware.tutor.question.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import spock.lang.Specification

@DataJpaTest
class StudentCreateSubmitQuestionTest extends Specification {

    def questionService

    /*def setup() {
        questionService = new QuestionService()
    }
*/
    def "the course and topic exist and title and body of text is valid "() {
        // the question is created or submited
        expect: false
    }

    def "not a student"() {
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