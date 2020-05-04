package pt.ulisboa.tecnico.socialsoftware.tutor.question.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.StudentQuestion;

import java.util.List;

@Repository
@Transactional
public interface StudentQuestionRepository extends JpaRepository<StudentQuestion, Integer> {
    @Query( value = "SELECT * FROM student_questions c WHERE c.course_id = :courseId", nativeQuery = true)
    List<Question> findStudentQuestions(int courseId);

    @Query( value = "SELECT count(*) FROM student_questions sq WHERE sq.user_id = :userId", nativeQuery = true)
    Integer findStudentQuestionsSubmitted(int userId);

    @Query( value = "SELECT count(*) FROM student_questions sq WHERE sq.user_id = :userId and sq.QuestionStatus = QuestionStatus.APPROVED", nativeQuery = true)
    Integer findStudentQuestionsApproved(int userId);
}
