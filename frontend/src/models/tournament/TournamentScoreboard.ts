import TournamentScore from './TournamentScore';

export default class TournamentScoreboard {
  averageScore!: number;
  tournamentTitle!: string;
  numberOfQuestions!: number;
  numberOfParticipants!: number;

  scores: TournamentScore[] = [];

  constructor(jsonObj?: TournamentScoreboard) {
    if (jsonObj) {
      this.averageScore = jsonObj.averageScore;
      this.tournamentTitle = jsonObj.tournamentTitle;
      this.numberOfQuestions = jsonObj.numberOfQuestions;
      this.numberOfParticipants = jsonObj.numberOfParticipants;

      this.scores = jsonObj.scores.map((score: TournamentScore) => new TournamentScore(score));
    }
    console.log(jsonObj)
  }
}