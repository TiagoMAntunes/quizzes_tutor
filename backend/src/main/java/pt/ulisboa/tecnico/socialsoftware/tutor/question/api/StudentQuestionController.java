package pt.ulisboa.tecnico.socialsoftware.tutor.question.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.StudentQuestionService;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.StudentQuestion;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.StudentQuestionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;

import java.security.Principal;
import java.util.List;
import javax.validation.Valid;
import java.io.Serializable;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.AUTHENTICATION_ERROR;

@RestController
public class StudentQuestionController{

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

    @PostMapping("/student_questions/{questionId}/approve-reject")
    @PreAuthorize("hasRole('ROLE_TEACHER') and hasPermission(#questionId, 'QUESTION.ACCESS')")
    public ResponseEntity studentQuestionApproveRejectStatus(@PathVariable Integer questionId, @Valid @RequestBody Information inform) {
        studentQuestionService.studentQuestionApproveReject(questionId, StudentQuestion.QuestionStatus.valueOf(inform.status), inform.explanation, inform.teacherId,  inform.courseExecutionId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/courses/{courseId}/student_questions")
    @PreAuthorize("hasRole('ROLE_STUDENT') and hasPermission(#courseId, 'COURSE.ACCESS')")
    public StudentQuestionDto createStudentQuestion(@PathVariable int courseId, @Valid @RequestBody QuestionDto question, Principal principal) {

        User user = (User) ((Authentication) principal).getPrincipal();

        if(user == null){
            throw new TutorException(AUTHENTICATION_ERROR);
        }
        question.setStatus(Question.Status.AVAILABLE.name());
        return this.studentQuestionService.createStudentQuestion(courseId, question, user.getId());
    }
}