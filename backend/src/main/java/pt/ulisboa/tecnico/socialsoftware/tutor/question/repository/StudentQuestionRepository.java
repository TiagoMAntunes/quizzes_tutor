package pt.ulisboa.tecnico.socialsoftware.tutor.question.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Assessment;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;

import java.util.List;

@Repository
@Transactional
public interface StudentQuestionRepository extends JpaRepository<Assessment, Integer> {
    @Query(value = "SELECT * FROM courses c WHERE c.course_id = :courseId", nativeQuery = true)
    List<Question> findStudentQuestions(int courseId);

}