package pt.ulisboa.tecnico.socialsoftware.tutor.question.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.StudentQuestion;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.dto.UserDto;
import java.io.Serializable;

public class StudentQuestionDto extends QuestionDto implements Serializable {
    private String questionStatus;
    private String explanation;
    private UserDto user;

    public StudentQuestionDto(){
        super();
    }

    public StudentQuestionDto(StudentQuestion studentQuestion){
        super(studentQuestion);
        this.questionStatus = studentQuestion.getStatus().name();
        this.explanation = studentQuestion.getRejectionExplanation();
        this.user = new UserDto(studentQuestion.getUser());

    }

    public String getQuestionStatus() {
        return this.questionStatus;
    }

    public void setStudentQuestionStatus(String status) {
        this.questionStatus = status;
    }

    public String getRejectionExplanation() { return this.explanation; }

    public void setRejectionExplanation(String explanation) { this.explanation = explanation; }

    public UserDto getUserDto() {
        return this.user;
    }

    public void setUserDto(UserDto userDto) {
        this.user = userDto;
    }

}
