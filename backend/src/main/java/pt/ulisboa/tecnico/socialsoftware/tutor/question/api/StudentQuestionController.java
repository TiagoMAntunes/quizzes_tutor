package pt.ulisboa.tecnico.socialsoftware.tutor.question.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.StudentQuestionService;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.StudentQuestion;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.StudentQuestionDto;

import java.util.List;

@RestController
public class StudentQuestionController {

    @Autowired
    private StudentQuestionService  studentQuestionService;

    @GetMapping("/student_questions/{studentId}")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public List<StudentQuestionDto> getAllStudentQuestions(@PathVariable int studentId){
        return this.studentQuestionService.getStudentQuestions(studentId);
    }
}