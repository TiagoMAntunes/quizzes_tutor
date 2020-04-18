describe('StudentQuestions walkthrough', () => {
    beforeEach(() => {
        cy.demoTeacherLogin()
    })

    afterEach(() => {
        cy.contains('Logout').click()
    })

    it ('Reject a student question and add an explanation', () => {
        cy.openTeacherStudentQuestions();
        cy.rejectQuestion('wtv');
        cy.addExplanation('wtv', 'not good');
    });
});
