let TITLE = 'Test Title'
let QUESTION = 'Test Question'
let OPTIONS =['Option1','Option2','Option3', 'Option4']

describe('Student Questions walkthrough', () => {
    beforeEach( () => {
      cy.demoStudentLogin()
    })

    afterEach(() => {
        cy.logout();
    })

    it('Verify dashboard question submitted', () => {
        cy.goToStats()
        cy.createStudentQuestion(TITLE, QUESTION, OPTIONS)
        cy.goToStats()
    });

    it('Verify dashboard question approved', () => {
        cy.goToStats()
        cy.logout()
        cy.demoTeacherLogin()
        cy.statusQuestion(TITLE, 'APPROVED')
        cy.logout()
        cy.goToStats()
    });
});