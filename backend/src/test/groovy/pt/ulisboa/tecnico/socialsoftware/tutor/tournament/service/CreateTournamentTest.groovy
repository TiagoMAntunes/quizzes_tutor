package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import spock.lang.Specification


@DataJpaTest
class CreateTournament extends Specification {

    def "create the tournament"() {
        //the tournament is created with correct data
        expect: false
    }

    def "the tournament is created with a start time after finish time"() {
        //an exception is thrown
        expect: false
    }

    def "the tournament is created with a finish time before the time of creation"() {
        //an exception is thrown
        expect: false
    }

    def "the tournament is created with 0 topics"() {
        //an exception is thrown
        expect: false
    }

    def "the tournament is created with 0 or less questions"() {
        //an exception is thrown
        expect: false
    }

    def "the tournament is created with repeated topics"() {
        //the tournament adapts and only creates it one time
        expect: false
    }
}