package pt.ulisboa.tecnico.socialsoftware.tutor.question.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.StudentQuestion;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.dto.UserDto;
import java.io.Serializable;

public class StudentQuestionDto extends QuestionDto implements Serializable {
    private String questionStatus;
    private UserDto user;

    public StudentQuestionDto(){
        super();
    }

    public StudentQuestionDto(StudentQuestion studentQuestion){
        super(studentQuestion);
        this.questionStatus = studentQuestion.getStatus().name();

        this.user = new UserDto(studentQuestion.getUser());

    }

    public String getQuestionStatus() {
        return this.questionStatus;
    }

    public void setStudentQuestionStatus(String status) {
        this.questionStatus = status;
    }

    public UserDto getUserDto() {
        return this.user;
    }

    public void setUserDto(UserDto userDto) {
        this.user = userDto;
    }

}
