package pt.ulisboa.tecnico.socialsoftware.tutor.question.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.StudentQuestionService;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.StudentQuestion;

import javax.validation.Valid;

@RestController
public class StudentQuestionController {

    @Autowired
    private StudentQuestionService  studentQuestionService;

    @PostMapping("/questions/{questionId}/approve-reject")
    @PreAuthorize("hasRole('ROLE_TEACHER') and hasPermission(#questionId, 'QUESTION.ACCESS')")
    public ResponseEntity studentQuestionApproveRejectStatus(@PathVariable Integer questionId, @Valid @RequestBody String status, @Valid @RequestBody String explanation, @Valid @RequestBody int teacherId, @Valid @RequestBody int courseExecutionId) {
        studentQuestionService.studentQuestionApproveReject(questionId, StudentQuestion.QuestionStatus.valueOf(status), explanation, teacherId,  courseExecutionId);
        return ResponseEntity.ok().build();
    }
}
