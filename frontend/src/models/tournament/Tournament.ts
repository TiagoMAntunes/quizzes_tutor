import Topic from '../management/Topic';

export default class Tournament {
  id: number | undefined;
  hasSignedUp: boolean | undefined;
  isCreator: boolean | undefined;
  startTime: string | undefined;
  finishTime: string | undefined;
  numberOfQuestions: number | undefined;
  numberOfParticipants: number | undefined;

  topics: Topic[] = [];

  constructor(jsonObj?: Tournament) {
    if (jsonObj) {
      this.id = jsonObj.id;
      this.hasSignedUp = jsonObj.hasSignedUp;
      this.isCreator = jsonObj.isCreator;
      this.startTime = jsonObj.startTime;
      this.finishTime = jsonObj.finishTime;
      this.numberOfQuestions = jsonObj.numberOfQuestions;
      this.numberOfParticipants = jsonObj.numberOfParticipants;

      this.topics = jsonObj.topics.map((topic: Topic) => new Topic(topic));

      console.log(jsonObj);
    }
  }
}
