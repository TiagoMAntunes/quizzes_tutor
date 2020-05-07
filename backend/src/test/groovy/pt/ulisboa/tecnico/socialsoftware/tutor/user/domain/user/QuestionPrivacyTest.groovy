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
class QuestionPrivacyTest extends Specification {

    @Autowired
    UserRepository userRepository

    @Autowired
    UserService userService

    def STUDENT

    def setup() {
        def student = new User()
        student.setKey(userService.getMaxUserNumber() + 1)
        student.setRole(User.Role.STUDENT)

        STUDENT = student

        userRepository.save(student)
    }

    def "Change question privacy to private"() {
        given: "a student"
        def student = STUDENT

        when: "sets his question privacy to private"
        userService.setQuestionPrivacy(student.getId(), true)

        then:
        student.getQuestionPrivacy() == true
    }

    def "Change question privacy to public"() {
        given: "a student"
        def student = STUDENT

        when: "sets his question privacy to public"
        userService.setQuestionPrivacy(student.getId(), false)

        then:
        student.getQuestionPrivacy() == false
    }

    @TestConfiguration
    static class QuestionServiceImplTestContextConfiguration {

        @Bean
        UserService userService() {
            return new UserService()
        }
    }
}