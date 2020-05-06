import Topic from '../management/Topic';
import { ISOtoString } from '@/services/ConvertDateService';

export default class Tournament {
  id: number | undefined;
  title: string | undefined;
  hasSignedUp: boolean | undefined;
  isCreator: boolean | undefined;
  startTime!: string;
  finishTime!: string;
  numberOfQuestions: number | undefined;
  numberOfParticipants: number | undefined;

  topics: Topic[] = [];

  constructor(jsonObj?: Tournament) {
    if (jsonObj) {
      this.id = jsonObj.id;
      this.title = jsonObj.title;
      this.hasSignedUp = jsonObj.hasSignedUp;
      this.isCreator = jsonObj.isCreator;
      this.startTime = ISOtoString(jsonObj.startTime);
      this.finishTime = ISOtoString(jsonObj.finishTime);
      this.numberOfQuestions = jsonObj.numberOfQuestions;
      this.numberOfParticipants = jsonObj.numberOfParticipants;

      this.topics = jsonObj.topics.map((topic: Topic) => new Topic(topic));
    }
  }
}
