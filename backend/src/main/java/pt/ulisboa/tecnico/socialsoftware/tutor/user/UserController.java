package pt.ulisboa.tecnico.socialsoftware.tutor.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;

import java.security.Principal;

@RestController
public class UserController {

    @Autowired
    UserService userService;

    @PutMapping("/user/tournament/privacy")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public ResponseEntity setTournamentPrivacy(@RequestParam boolean privacy, Principal principal) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if (user == null) {
            throw new TutorException(ErrorMessage.AUTHENTICATION_ERROR);
        }

        userService.setTournamentPrivacy(user.getId(), privacy);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/tournament/privacy")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public boolean getTournamentPrivacy(Principal principal) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if (user == null) {
            throw new TutorException(ErrorMessage.AUTHENTICATION_ERROR);
        }

        return userService.getTournamentPrivacy(user.getId());
    }

}
