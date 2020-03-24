package pt.ulisboa.tecnico.socialsoftware.tutor.question.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.StudentQuestion;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.dto.UserDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;

import java.io.Serializable;

public class StudentQuestionDto extends QuestionDto implements Serializable {
    private String questionStatus;
    private String explanation;
    private UserDto userDto;
    private User user;

    public StudentQuestionDto(){
        super();
    }

    public StudentQuestionDto(StudentQuestion studentQuestion) {
        super(studentQuestion);
        this.questionStatus = studentQuestion.getQuestionStatus().name();
        this.explanation = studentQuestion.getRejectionExplanation();
        this.user = studentQuestion.getUser();
        this.userDto = new UserDto(studentQuestion.getUser());
    }

    public String getQuestionStatus() { return this.questionStatus; }

    public void setStudentQuestionStatus(String status) { this.questionStatus = status; }

    public String getRejectionExplanation() { return this.explanation; }

    public void setRejectionExplanation(String explanation) { this.explanation = explanation; }

    public User getUser() {
        return this.user;
    }

    public UserDto getUserDto() {
        return this.userDto;
    }

    public void setUserDto(UserDto userDto) {
        this.userDto = userDto;
    }

}
