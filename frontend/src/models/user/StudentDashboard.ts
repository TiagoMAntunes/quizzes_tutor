export default class StudentDashboard {
    numberQuestionsSubmitted!: number;
    numberQuestionsApproved!: number ;


    constructor(jsonObj?: StudentDashboard) {
        if (jsonObj) {
            this.numberQuestionsSubmitted = jsonObj.numberQuestionsSubmitted;
         this.numberQuestionsApproved = jsonObj.numberQuestionsApproved;
        }
    }
}