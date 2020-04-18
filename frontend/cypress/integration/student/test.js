let OPTIONS = ['Option1', 'Option2', 'Option3', 'Option4']

describe('Questions walkthrough', () => {
    beforeEach(() => {
        cy.demoStudentLogin()
    })

    afterEach(() => {
        cy.contains('Logout').click()
    })

    it ('Get student questions and show a question', () => {
        //cy.createStudentQuestion('test30', 'What question?', OPTIONS)
        cy.openAvailableQuestions();
        cy.showStudentQuestion('question');
    });

    it ('Get student questions and add a topic to a question', () => {
        //cy.createStudentQuestion('test50', 'Whats a question?', OPTIONS)
        cy.openAvailableQuestions();
        cy.addTopicStudentQuestion('question','Adventure Builder');
    });
});
