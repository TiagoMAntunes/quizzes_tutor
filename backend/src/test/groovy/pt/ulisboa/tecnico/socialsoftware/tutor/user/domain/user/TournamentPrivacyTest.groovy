package pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.user

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserService
import spock.lang.Specification



@DataJpaTest
class TournamentPrivacyTest extends Specification {

    @Autowired
    UserRepository userRepository

    @Autowired
    UserService userService

    def STUDENT


    def setup() {
        //Create student
        def student = new User()
        student.setKey(userService.getMaxUserNumber() + 1)
        student.setRole(User.Role.STUDENT)

        STUDENT = student

        userRepository.save(student)
    }

    def "User sets tournament privacy to private"(){
        given: "a student"
        def student = STUDENT

        when: "sets his tournament privacy to private"
        userService.setTournamentPrivacy(student.getId(), true)

        then:
        student.getTournamentPrivacy() == true
    }


    def "User sets tournament privacy to public"(){
        given: "a student"
        def student = STUDENT

        when: "sets his tournament privacy to private"
        userService.setTournamentPrivacy(student.getId(), false)

        then:
        student.getTournamentPrivacy() == false
    }

    @TestConfiguration
    static class TournamentServiceImplTestContextConfiguration {


        @Bean
        UserService userService() {
            return new UserService()
        }

    }

}
