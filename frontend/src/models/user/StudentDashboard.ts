export default class StudentDashboard {
    numberQuestionsSubmitted!: number;
    numberQuestionsApproved!: number ;
    createdTournaments!: number;
    participatedTournamentsNumber!: number;
    notYetParticipatedTournamentsNumber!: number;
    averageTournamentScore!: number;

    constructor(jsonObj?: StudentDashboard) {
        if (jsonObj) {
            this.numberQuestionsSubmitted = jsonObj.numberQuestionsSubmitted;
            this.numberQuestionsApproved = jsonObj.numberQuestionsApproved;
            this.createdTournaments = jsonObj.createdTournaments;
            this.participatedTournamentsNumber = jsonObj.participatedTournamentsNumber;
            this.notYetParticipatedTournamentsNumber = jsonObj.notYetParticipatedTournamentsNumber;
            this.averageTournamentScore = jsonObj.averageTournamentScore;

            console.log(jsonObj);
        }
    }
}