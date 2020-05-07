import Option from '@/models/management/Option';
import Image from '@/models/management/Image';
import Topic from '@/models/management/Topic';
import {ISOtoString} from "@/services/ConvertDateService";

export default class StudentQuestion {
  id: number | null = null;
  title: string = '';
  questionStatus: string = 'PENDING';
  numberOfCorrect!: number;
  difficulty!: number;
  content: string = '';
  rejectionExplanation: string = '';
  creationDate!: string | null;
  image: Image | null = null;
  sequence: number | null = null;

  options: Option[] = [new Option(), new Option(), new Option(), new Option()];
  topics: Topic[] = [];

  constructor(jsonObj?: StudentQuestion) {
    if (jsonObj) {
      this.id = jsonObj.id;
      this.title = jsonObj.title;
      this.questionStatus = jsonObj.questionStatus;
      this.numberOfCorrect = jsonObj.numberOfCorrect;
      this.difficulty = jsonObj.difficulty;
      this.content = jsonObj.content;
      this.rejectionExplanation = jsonObj.rejectionExplanation;
      this.image = jsonObj.image;
      this.creationDate = ISOtoString(jsonObj.creationDate);

      this.options = jsonObj.options.map(
        (option: Option) => new Option(option)
      );

      this.topics = jsonObj.topics.map((topic: Topic) => new Topic(topic));
    }
  }
}
