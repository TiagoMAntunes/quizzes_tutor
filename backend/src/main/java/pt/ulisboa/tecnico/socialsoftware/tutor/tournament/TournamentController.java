package pt.ulisboa.tecnico.socialsoftware.tutor.tournament;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;

import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.domain.Tournament;
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto.TournamentDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto.TournamentScoreboardDto;
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

    @PutMapping("/tournaments/{tournamentId}")
    @PreAuthorize("hasRole('ROLE_STUDENT') and hasPermission(#tournamentId, 'TOURNAMENT.ACCESS')")
    public TournamentDto joinTournament(@PathVariable int tournamentId, @RequestParam(required = false) Integer userId, Principal principal) {
        if(userId == null){
            User user = (User) ((Authentication) principal).getPrincipal();

            if (user == null) {
                throw new TutorException(AUTHENTICATION_ERROR);
            }

            userId = user.getId();
        }
        return this.tournamentService.joinTournament(tournamentId, userId);
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
    @PreAuthorize("hasRole('ROLE_STUDENT') and hasPermission(#executionId, 'EXECUTION.ACCESS')")
    public List<TournamentDto> getOpenTournaments(@PathVariable int executionId, Principal principal) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if (user == null) {
            throw new TutorException(AUTHENTICATION_ERROR);
        }

        return this.tournamentService.getOpenTournaments(executionId, user.getId());
    }

    @GetMapping("/tournaments/scoreboard/{tournamentId}")
    @PreAuthorize("hasRole('ROLE_STUDENT') and hasPermission(#tournamentId, 'TOURNAMENT.ACCESS')")
    public TournamentScoreboardDto getTournamentScoreboard(@PathVariable Integer tournamentId, Principal principal) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if (user == null) {
            throw new TutorException(AUTHENTICATION_ERROR);
        }

        return tournamentService.getTournamentScoreboard(tournamentId);
    }

}