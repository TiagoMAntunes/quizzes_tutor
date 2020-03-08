package pt.ulisboa.tecnico.socialsoftware.tutor.question.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import spock.lang.Specification

@DataJpaTest
class getQuestionsStatusTest extends Specification {

    def "no questions submitted"(){
        //throws an exception
        expect: false
    }

    def "get status of question that is waiting for approval"(){
        //should return a list with the status
        //value should be correct
        expect: false
    }

    def "get status of an approved question"(){
        //should return a list with the status, as well as the teacher that approved it
        //values should be correct
        expect: false
    }

    def "get status of a rejected question with no explanation"(){
        //should return a list with the status, as well as the teacher that rejected it
        //values should be correct
        expect: false
    }

    def "get status of a rejected question with explanation"(){
        //should return a list with the status, the teacher that rejected it and the explanation
        //values should be correct
        expect: false
    }

    def "get status of two submitted questions: one approved question and one rejected with no explanation"(){
        //should return a list with the status, as well as the teachers that approved/rejected it
        //values should be correct
        expect: false
    }

    def "get status of two submitted questions: one approved question and one rejected with explanation"(){
        //should return a list with the status, as well as the teachers that approved/rejected it and the explanation
        //values should be correct
        expect: false
    }
}