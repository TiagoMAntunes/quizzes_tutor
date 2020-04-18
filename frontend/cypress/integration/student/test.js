let OPTIONS = ['Option1', 'Option2', 'Option3', 'Option4']

describe('Questions walkthrough', () => {
    beforeEach(() => {
        cy.demoStudentLogin()
    })

    afterEach(() => {
        cy.contains('Logout').click()
    })

    it ('Create a question, get the questions and show a question', () => {
        cy.createStudentQuestion('test30', 'What question?', OPTIONS)
        cy.openAvailableQuestions();
        cy.showStudentQuestion('test30');
    });

    it ('Create a question, get the questions and add a topic to the question', () => {
        cy.createStudentQuestion('test50', 'Whats a question?', OPTIONS)
        cy.openAvailableQuestions();
        cy.addTopicStudentQuestion('test50','Adventure Builder');
    });
});
