import Topic from '../management/Topic';

export default class Tournament {
    id : number | undefined;
    topics: Topic[] | undefined;
    startTime: string | undefined;
    finishTime: string | undefined;
    numberOfQuestions: number | undefined;
  
    constructor(jsonObj?: Tournament) {
      if (jsonObj) {
        this.id = jsonObj.id;
        this.topics = jsonObj.topics;
        this.startTime = jsonObj.startTime;
        this.finishTime = jsonObj.finishTime;
        this.numberOfQuestions = jsonObj.numberOfQuestions;
      }
    }
  }