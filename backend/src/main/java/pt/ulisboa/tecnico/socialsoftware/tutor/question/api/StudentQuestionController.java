package pt.ulisboa.tecnico.socialsoftware.tutor.question.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.StudentQuestionService;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.StudentQuestion;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.StudentQuestionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;

import java.security.Principal;

import java.util.List;
import javax.validation.Valid;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.AUTHENTICATION_ERROR;

@RestController
public class StudentQuestionController{

    @Autowired
    private StudentQuestionService  studentQuestionService;

    @GetMapping("/student_questions/{courseId}/all")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public List<StudentQuestionDto> getAllQuestionsOfCourses(@PathVariable int courseId, Principal principal){
        User user = (User) ((Authentication) principal).getPrincipal();

        if(user == null){
            throw new TutorException(AUTHENTICATION_ERROR);
        }
        return this.studentQuestionService.getStudentQuestionsByCourses(user.getCourseExecutions(), courseId);
    }
    @GetMapping("/student_questions/{courseId}/{studentId}")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public List<StudentQuestionDto> getSpecificStudentQuestions(@PathVariable int courseId, @PathVariable int studentId){
        return this.studentQuestionService.getStudentQuestions(courseId, studentId);
    }

    @PostMapping("/student_questions/evaluate/{questionId}/{status}")
    @PreAuthorize("hasRole('ROLE_TEACHER') and hasPermission(#questionId, 'QUESTION.ACCESS')")
    public ResponseEntity studentQuestionApproveRejectStatus(@PathVariable Integer questionId, @PathVariable String status) {
        studentQuestionService.studentQuestionApproveReject(questionId, StudentQuestion.QuestionStatus.valueOf(status));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/student_questions/{questionId}/explanation")
    @PreAuthorize("hasRole('ROLE_TEACHER') and hasPermission(#questionId, 'QUESTION.ACCESS')")
    public ResponseEntity studentQuestionAddExplanation(@PathVariable Integer questionId, @Valid @RequestBody String explanation) {
        studentQuestionService.studentQuestionExplanation(questionId, explanation);
        return ResponseEntity.ok().build();
    }
}

