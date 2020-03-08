package pt.ulisboa.tecnico.socialsoftware.tutor.question.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

@Entity
@Table(
        name = "studentQuestions",
        indexes = {
                @Index(name = "question_indx_0", columnList = "key")
        })

public class StudentQuestion extends Question {
    public enum ApprovedStatus {
        APPROVED, REJECTED, PENDING
    }

}



