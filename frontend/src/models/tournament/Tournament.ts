import Topic from '../management/Topic';

export default class Tournament {
  id: number | undefined;
  isCreator: boolean | undefined;
  startTime: string | undefined;
  finishTime: string | undefined;
  numberOfQuestions: number | undefined;

  topics: Topic[] = [];

  constructor(jsonObj?: Tournament) {
    if (jsonObj) {
      this.id = jsonObj.id;
      this.isCreator = jsonObj.isCreator;
      this.startTime = jsonObj.startTime;
      this.finishTime = jsonObj.finishTime;
      this.numberOfQuestions = jsonObj.numberOfQuestions;

      this.topics = jsonObj.topics.map((topic: Topic) => new Topic(topic));
    }
  }
}
