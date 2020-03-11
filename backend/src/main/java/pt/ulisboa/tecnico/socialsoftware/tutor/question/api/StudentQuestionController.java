package pt.ulisboa.tecnico.socialsoftware.tutor.question.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.StudentQuestionService;

@RestController
public class StudentQuestionController {
    @Autowired
    private StudentQuestionService  studentQuestionService;

}
