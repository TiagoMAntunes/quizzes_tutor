import StatementCorrectAnswer from '@/models/statement/StatementCorrectAnswer';
import RemoteServices from '@/services/RemoteServices';
import StatementQuiz from '@/models/statement/StatementQuiz';

export default class StatementManager {
  assessment!: number;
  numberOfQuestions: string = '5';
  statementQuiz: StatementQuiz | null = null;
  correctAnswers: StatementCorrectAnswer[] = [];

  private static _quiz: StatementManager = new StatementManager();

  static get getInstance(): StatementManager {
    return this._quiz;
  }

  async getQuizStatement() {
    let params = {
      assessment: this.assessment,
      numberOfQuestions: +this.numberOfQuestions
    };

    this.statementQuiz = await RemoteServices.generateStatementQuiz(params);
  }

  async concludeQuiz() {
    if (this.statementQuiz) {
      let answers = await RemoteServices.concludeQuiz(this.statementQuiz.id);
      if (answers) {
        this.correctAnswers = answers;
      }
    } else {
      throw Error('No quiz');
    }
  }

  reset() {
    this.statementQuiz = null;
    this.correctAnswers = [];
  }

  isEmpty(): boolean {
    return this.statementQuiz == null;
  }
}
