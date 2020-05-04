package pt.ulisboa.tecnico.socialsoftware.tutor.question.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import spock.lang.Specification

@DataJpaTest
class StudentDashboardQuestionApprovedTest extends Specification {

    def setup() {

    }

    def "find the number of questions submitted greater than zero"() {
      expect: true;
    }

    def "find the number of questions submitted equals zero"() {
       expect: true;
    }

    def "teacher try to find the number of questions submitted"() {
       expect: true;
    }
}
