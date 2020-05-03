package pt.ulisboa.tecnico.socialsoftware.tutor.question.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import spock.lang.Specification

@DataJpaTest
class ApproveBecomesAvailableTest extends Specification{

    def "a question is approved and then it is made available"(){
        //the question becomes part of the available questions
        expect: false
    }

    def "a question is rejected and then it is made available"(){
        //an exception is thrown
        expect: false
    }

    def "a question is in the pending status and then it is made available"(){
        //an exception is thrown
        expect: false
    }
}

