package pt.ulisboa.tecnico.socialsoftware.tutor.question.api;

import org.springframework.beans.factory.annotation.Autowired;
<<<<<<< HEAD
import org.springframework.web.bind.annotation.RestController;
=======
import org.springframework.http.ResponseEntity;
>>>>>>> ppa
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.StudentQuestionService;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.StudentQuestion;
<<<<<<< HEAD
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.StudentQuestionDto;

import java.util.List;
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


    @GetMapping("/student_questions/{studentId}")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public List<StudentQuestionDto> getAllStudentQuestions(@PathVariable int studentId){
        return this.studentQuestionService.getStudentQuestions(studentId);
    }
}

    @PostMapping("/student_questions/{questionId}/approve-reject")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ResponseEntity studentQuestionApproveRejectStatus(@PathVariable Integer questionId, @Valid @RequestBody Information inform) {
        studentQuestionService.studentQuestionApproveReject(questionId, StudentQuestion.QuestionStatus.valueOf(inform.status), inform.explanation, inform.teacherId,  inform.courseExecutionId);
        return ResponseEntity.ok().build();
    }
}

