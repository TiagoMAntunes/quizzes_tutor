

describe('Student changing privacy settings', () => {
    beforeEach(() => {
        cy.demoStudentLogin()
    })

    afterEach(() => {
        cy.contains('Logout').click();
    })

    it('Set tournament privacy as private', () => {
        cy.openSettings().wait(500) // time to load
        cy.setPrivacy('tournament', 'private')
    })

    it('Set tournament privacy as public', () => {
        cy.openSettings().wait(500) // time to load
        cy.setPrivacy('tournament', 'public')
    })
})