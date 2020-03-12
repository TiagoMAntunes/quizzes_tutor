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
        name = "studentQuestions"
)

public class StudentQuestion extends Question {

    public enum QuestionStatus {
        APPROVED, REJECTED, PENDING
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    private String explanation;


    @Enumerated(EnumType.STRING)
    private QuestionStatus questionStatus = QuestionStatus.PENDING;

    public StudentQuestion(){
        super();
    }
    public StudentQuestion(Course course, QuestionDto questionDto, User user){
        super(course, questionDto);

        checkConsistentUser(user);

        this.user = user;
        this.explanation = null;
        this.setQuestionStatus(QuestionStatus.PENDING);

        user.addStudentQuestion(this);
    }

    public QuestionStatus getQuestionStatus() {
        return this.questionStatus;
    }

    public void setQuestionStatus(QuestionStatus status) {
        this.questionStatus = status;
    }

    public String getRejectionExplanation() { return this.explanation; }

    public void setRejectionExplanation(String explanation) {
        if(this.questionStatus == QuestionStatus.REJECTED) {
            this.explanation = explanation;
        }else{
            throw new TutorException(CANT_ADD_EXPLANATION);
        }
    }

    public User getUser() {
        return this.user;
    }

    public void setUser(User userDto) {
        this.user = userDto;
    }

    // TODO toString

    private void checkConsistentUser(User user){
        if (user.getName() == null || user.getName().trim().length() == 0 ||
                user.getUsername() == null || user.getUsername().trim().length() == 0) {
            throw new TutorException(USER_MISSING_DATA);
        }
        if (user.getRole() != User.Role.STUDENT) {
            throw new TutorException(ACCESS_DENIED);
        }
    }

}



