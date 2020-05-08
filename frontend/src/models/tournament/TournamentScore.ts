export default class TournamentScore {
    name!: string;
    score!: number;

  constructor(jsonObj?: TournamentScore) {
    if (jsonObj) {
      this.name = jsonObj.name;
      this.score = jsonObj.score;
    }
  }
}