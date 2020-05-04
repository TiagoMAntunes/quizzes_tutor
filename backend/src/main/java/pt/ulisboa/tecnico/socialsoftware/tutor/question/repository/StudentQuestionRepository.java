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
    @Query( value = "SELECT * FROM studentQuestions c WHERE c.course_id = :courseId", nativeQuery = true)
    List<Question> findStudentQuestions(int courseId);

    @Query( value = "SELECT count(user_id) FROM stundentQuestions c WHERE c.user_id = :userId", nativeQuery = true)
    int findStudentQuestionsSubmitted(int userId);
}