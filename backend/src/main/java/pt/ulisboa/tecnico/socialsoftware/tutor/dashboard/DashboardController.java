package pt.ulisboa.tecnico.socialsoftware.tutor.dashboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;

import java.security.Principal;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.AUTHENTICATION_ERROR;

@RestController
public class DashboardController {
    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/student/{courseId}/dashboard")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public DashboardDto createDashboard(Principal principal, @PathVariable int courseId) {
        User user = (User) ((Authentication) principal).getPrincipal();
        if(user == null){
            throw new TutorException(AUTHENTICATION_ERROR);
        }

        Integer userId = user.getId();

        DashboardDto dashboard = new DashboardDto();
        dashboard.setNumberQuestionsSubmitted(this.dashboardService.findNumberStudentQuestionsSubmitted(userId, courseId));
        dashboard.setNumberQuestionsApproved(this.dashboardService.findNumberStudentQuestionsApproved(userId, courseId));
        dashboard.setCreatedTournaments(this.dashboardService.getCreatedTournamentsNumber(userId, courseId));
        dashboard.setParticipatedTournamentsNumber(this.dashboardService.getParticipatedTournamentsNumber(userId, courseId));
        dashboard.setNotYetParticipatedTournamentsNumber(this.dashboardService.getNotYetParticipatedTournamentsNumber(userId, courseId));
        dashboard.setAverageTournamentScore(this.dashboardService.getAverageTournamentScore(userId, courseId));

        return dashboard;
    }
}
