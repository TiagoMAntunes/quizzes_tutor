package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import spock.lang.Specification

@DataJpaTest
class CancelTournamentTest extends Specification {

    // A cancelable tournament is one that the student created but hasn't started yet
    def "cancel a cancelable tournament"() {
        // the tournament should be canceled
        expect: false
    }

    def "cancel a tournament not created by the student"() {
        // throw an exception
        expect: false
    }

    def "cancel a tournament after it as started"() {
        // throw an exception
        expect: false
    }

    def "cancel a tournament with 1 student signed up"() {
        // the tournament should be canceled and removed from the students tournaments
        expect: false
    }
}
