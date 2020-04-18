describe('Questions walkthrough', () => {
    beforeEach(() => {
        cy.demoStudentLogin()
    })

    afterEach(() => {
        cy.contains('Logout').click()
    })

    it ('Get student questions and show a question', () => {
        cy.openAvailableQuestions();
        cy.showStudentQuestion('wtv');
    });

    it ('Get student questions and add a topic to a question', () => {
        cy.openAvailableQuestions();
        cy.addTopicStudentQuestion('wtv','Adventure Builder');
    });
});
