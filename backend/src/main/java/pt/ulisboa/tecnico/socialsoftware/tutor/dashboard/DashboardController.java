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

    @GetMapping("/student/{execId}/dashboard")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public DashboardDto createDashboard(Principal principal, @PathVariable int execId) {
        User user = (User) ((Authentication) principal).getPrincipal();
        if(user == null){
            throw new TutorException(AUTHENTICATION_ERROR);
        }

        return dashboardService.getStudentDashboard(user.getId(), execId);
    }
}
