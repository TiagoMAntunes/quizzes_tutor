import Topic from '../management/Topic';
import RemoteServices from '@/services/RemoteServices';
import moment from 'moment';
export default class TournamentSkeleton {
  title: string;
  topics: Topic[];
  startTime: string;
  finishTime: string;
  numberOfQuestions: number;

  private static FORMAT: string = 'YYYY-MM-DD HH:mm';

  private static _tournament: TournamentSkeleton = new TournamentSkeleton();

  constructor() {
    this.title = '';
    this.topics = [];
    this.startTime = '';
    this.finishTime = '';
    this.numberOfQuestions = 5;
  }

  async createTournament() {
    let params = {
      title: this.title,
      topics: this.topics,
      startTime: moment(this.startTime).format(TournamentSkeleton.FORMAT),
      finishTime: moment(this.finishTime).format(TournamentSkeleton.FORMAT),
      numberOfQuestions: this.numberOfQuestions
    };

    await RemoteServices.createTournament(params);
  }

  reset() {
    this.title = '';
    this.topics = [];
    this.startTime = '';
    this.finishTime = '';
    this.numberOfQuestions = 5;
  }
}
