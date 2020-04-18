import Topic from '../management/Topic';

export default class Tournament {
  id: number | undefined;
  startTime: string | undefined;
  finishTime: string | undefined;
  numberOfQuestions: number | undefined;

  topics: Topic[] = [];

  constructor(jsonObj?: Tournament) {
    if (jsonObj) {
      this.id = jsonObj.id;
      this.startTime = jsonObj.startTime;
      this.finishTime = jsonObj.finishTime;
      this.numberOfQuestions = jsonObj.numberOfQuestions;

      this.topics = jsonObj.topics.map((topic: Topic) => new Topic(topic));
    }
  }
}
