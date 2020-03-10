package pt.ulisboa.tecnico.socialsoftware.tutor.question.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.dto.UserDto;

import pt.ulisboa.tecnico.socialsoftware.tutor.user.dto.UserDto;

import javax.persistence.*;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;

@Entity

@Table(
        name = "studentQuestions",
        indexes = {
                @Index(name = "studentQuestion_indx_0", columnList = "keyStu")
        })

public class StudentQuestion extends Question {

    public enum QuestionStatus {
        APPROVED, REJECTED, PENDING
    }

    @Column(unique=true, nullable = false)
    private Integer keyStu;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private User user;

    @Enumerated(EnumType.STRING)
    private QuestionStatus questionStatus = QuestionStatus.PENDING;

    public StudentQuestion(){

    }
    public StudentQuestion(Course course, QuestionDto questionDto, UserDto userDto){
        super(course, questionDto);

        checkConsistentUser(userDto);

        this.keyStu = questionDto.getKey();
        this.user = new User();
        this.user.setId(userDto.getId());
        this.user.setName(userDto.getName());
        this.user.setUsername(userDto.getUsername());
        this.user.setRole(userDto.getRole());

        this.questionStatus = QuestionStatus.valueOf(questionDto.getStatus());;

        course.addStudentQuestion(this);

    }

    public QuestionStatus getQuestionStatus() {
        return this.questionStatus;
    }

    public void setQuestionStatus(QuestionStatus status) {
        this.questionStatus = status;
    }

    public User getUser() {
        return this.user;
    }

    public void setUser(User userDto) {
        this.user = userDto;
    }

    public Integer getKeyStu() {
        return keyStu;
    }

    public void setKeyStu(Integer key) {
        this.keyStu = key;
    }

    // do toString

    private void checkConsistentUser(UserDto user){
        if (user.getName() == null || user.getName().trim().length() == 0 ||
                user.getUsername() == null || user.getUsername().trim().length() == 0) {
            throw new TutorException(USER_MISSING_DATA);
        }
        if (user.getRole() != User.Role.STUDENT) {
            throw new TutorException(ACCESS_DENIED);
        }
    }

}



