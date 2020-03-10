package pt.ulisboa.tecnico.socialsoftware.tutor.question.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.StudentQuestion;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.dto.UserDto;
import java.io.Serializable;

public class StudentQuestionDto extends QuestionDto implements Serializable {
    private String questionStatus;
    private UserDto user;
    private Integer keyStu;

    public StudentQuestionDto(){

    }

    public StudentQuestionDto(StudentQuestion studentQuestion){
        super((Question)studentQuestion);
        this.questionStatus = studentQuestion.getStatus().name();

        this.user = new UserDto(studentQuestion.getUser());
        this.keyStu = studentQuestion.getKeyStu();

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

    public Integer getKeyStu() {
        return keyStu;
    }

    public void setKeyStu(Integer key) {
        this.keyStu = key;
    }

}
