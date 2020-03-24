package pt.ulisboa.tecnico.socialsoftware.tutor.question.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.StudentQuestionService;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.StudentQuestion;

import javax.validation.Valid;
import java.io.Serializable;

@RestController
public class StudentQuestionController implements Serializable{

    static class Information{
        public String status;
        public String explanation;
        public int teacherId;
        public int courseExecutionId;
    }

    @Autowired
    private StudentQuestionService  studentQuestionService;

    @PostMapping("/student_questions/{questionId}/approve-reject")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ResponseEntity studentQuestionApproveRejectStatus(@PathVariable Integer questionId, @Valid @RequestBody Information inform) {
        studentQuestionService.studentQuestionApproveReject(questionId, StudentQuestion.QuestionStatus.valueOf(inform.status), inform.explanation, inform.teacherId,  inform.courseExecutionId);
        return ResponseEntity.ok().build();
    }
}

