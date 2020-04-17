package pt.ulisboa.tecnico.socialsoftware.tutor.tournament;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;

import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto.TournamentDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;


import javax.validation.Valid;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.AUTHENTICATION_ERROR;

@RestController
public class TournamentController {
    
    @Autowired
    private TournamentService tournamentService;

    @PostMapping("/executions/{executionId}/tournaments")
    @PreAuthorize("hasRole('ROLE_STUDENT') and hasPermission(#executionId, 'EXECUTION.ACCESS')")
    public TournamentDto createTournament(@PathVariable int executionId, @Valid @RequestBody TournamentDto tournament, Principal principal) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if(user == null){
            throw new TutorException(AUTHENTICATION_ERROR);
        }

        return this.tournamentService.createTournament(tournament, executionId, user.getId());
    }

    // tournaments/{tournamentId} with tournament access would be nice
    @PutMapping("/executions/{executionId}/tournaments/{tournamentId}")
    @PreAuthorize("hasRole('ROLE_STUDENT') and hasPermission(#executionId, 'EXECUTION.ACCESS')")
    public void joinTournament(@PathVariable int executionId, @PathVariable int tournamentId, @RequestParam int userId, Principal principal) {
        this.tournamentService.joinTournament(tournamentId, executionId, userId);
    }


    @DeleteMapping("/tournaments/{tournamentId}")
    @PreAuthorize("hasRole('ROLE_STUDENT') and hasPermission(#tournamentId, 'TOURNAMENT.CANCEL')")
    public ResponseEntity deleteTournament(@PathVariable Integer tournamentId, Principal principal) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if (user == null) {
            throw new TutorException(AUTHENTICATION_ERROR);
        }

        tournamentService.cancelTournament(tournamentId, user.getId());

        return ResponseEntity.ok().build();
    }
  
    @GetMapping("/executions/{executionId}/tournaments")
    @PreAuthorize("hasPermission(#executionId, 'EXECUTION.ACCESS')")
    public List<TournamentDto> getOpenTournaments(@PathVariable int executionId) {
        return this.tournamentService.getOpenTournaments(executionId);
    }

}