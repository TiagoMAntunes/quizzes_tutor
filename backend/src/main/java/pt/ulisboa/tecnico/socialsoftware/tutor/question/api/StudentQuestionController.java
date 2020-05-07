package pt.ulisboa.tecnico.socialsoftware.tutor.question.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.DashboardDto;
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
    @GetMapping("/student_questions/{courseId}/logged_student")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public List<StudentQuestionDto> getSpecificStudentQuestions(@PathVariable int courseId, Principal principal){
        User user = (User) ((Authentication) principal).getPrincipal();
        if(user == null){
            throw new TutorException(AUTHENTICATION_ERROR);
        }
        return this.studentQuestionService.getStudentQuestions(courseId, user.getId());
    }

    @PostMapping("/student_questions/evaluate/{questionId}/{status}")
    @PreAuthorize("hasRole('ROLE_TEACHER') and hasPermission(#questionId, 'QUESTION.ACCESS')")
    public ResponseEntity studentQuestionApproveRejectStatus(@PathVariable Integer questionId, @PathVariable String status) {
        studentQuestionService.studentQuestionApproveReject(questionId, StudentQuestion.QuestionStatus.valueOf(status));
        return ResponseEntity.ok().build();
    }

    @PutMapping("/student_questions/available/{questionId}")
    @PreAuthorize("hasRole('ROLE_TEACHER') and hasPermission(#questionId, 'QUESTION.ACCESS')")
    public ResponseEntity studentQuestionApproveToAvailable(@PathVariable Integer questionId) {
        studentQuestionService.studentQuestionApproveToAvailable(questionId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/student_questions/{questionId}/explanation")
    @PreAuthorize("hasRole('ROLE_TEACHER') and hasPermission(#questionId, 'QUESTION.ACCESS')")
    public ResponseEntity studentQuestionAddExplanation(@PathVariable Integer questionId, @Valid @RequestBody String explanation) {
        studentQuestionService.setStudentQuestionExplanation(questionId, explanation);
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

    @GetMapping("/student/{courseId}/dashboard")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public DashboardDto createDashboard(Principal principal, @PathVariable int courseId) {
        User user = (User) ((Authentication) principal).getPrincipal();
        if(user == null){
            throw new TutorException(AUTHENTICATION_ERROR);
        }
        DashboardDto dashboard = new DashboardDto();
        dashboard.setNumberQuestionsSubmitted(this.studentQuestionService.findNumberStudentQuestionsSubmitted(user.getId(), courseId));
        dashboard.setNumberQuestionsApproved(this.studentQuestionService.findNumberStudentQuestionsApproved(user.getId(), courseId));
        return dashboard;
    }

    @PutMapping("/student/{studentQuestionId}/resubmit")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public StudentQuestionDto studentResubmitQuestion(@PathVariable Integer studentQuestionId, @Valid @RequestBody QuestionDto questionDto) {
       return studentQuestionService.resubmitRejectedStudentQuestion(studentQuestionId, questionDto);
    }
}