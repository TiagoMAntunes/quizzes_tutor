package pt.ulisboa.tecnico.socialsoftware.tutor.question.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;
import javax.persistence.*;


@Entity
@Table(name = "studentQuestions")
public class StudentQuestion extends Question {

    public enum QuestionStatus {
        APPROVED, REJECTED, PENDING
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "explanation")
    private String explanation;

    @Enumerated(EnumType.STRING)
    private QuestionStatus questionStatus = QuestionStatus.PENDING;

    public StudentQuestion(){
        super();
    }

    public StudentQuestion(Course course, QuestionDto questionDto, User user) {
        super(course, questionDto);

        this.user = user;
        this.explanation = null;
        this.setQuestionStatus(QuestionStatus.PENDING);
        this.setStatus(Question.Status.DISABLED);

        user.addStudentQuestion(this);
    }

    public QuestionStatus getQuestionStatus() { return this.questionStatus; }

    public void setQuestionStatus(QuestionStatus status) { this.questionStatus = status; }

    public String getRejectionExplanation() { return this.explanation; }

    public void setRejectionExplanation(String explanation) { this.explanation = explanation; }

    public User getUser() { return this.user; }

    public void setUser(User userDto) { this.user = userDto; }

    public void updateQuestion(QuestionDto questionDto) {
        super.update(questionDto);
    }

    public void updateRejectedQuestion(QuestionDto questionDto) {
        if(this.getQuestionStatus() != QuestionStatus.REJECTED) {
            throw new TutorException(QUESTION_NOT_REJECTED);
        }
        super.update(questionDto);
        this.setQuestionStatus(QuestionStatus.PENDING);
        this.setRejectionExplanation(null);
    }
}




