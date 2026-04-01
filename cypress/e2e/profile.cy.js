describe('FULL PROFILE TEST', () => {
const email = 'nguyenhuumanh20072004@gmail.com'
const password = 'Manh1234'

beforeEach(() => {
    // ===== LOGIN =====
    cy.visit('http://localhost:8080/login')

    cy.get('input[name="username"]').should('be.visible').type(email)
    cy.get('input[name="password"]').type(password)

    cy.get('button[type="submit"]').click()

    // đợi login xong
    cy.url().should('not.include', '/login')

    // vào profile
    cy.visit('http://localhost:8080/profile')
})

// ================= UI =================
it('Hiển thị đầy đủ tab', () => {
    cy.contains(/Thông tin cá nhân/i).should('exist')
    cy.contains(/Địa chỉ/i).should('exist')
    cy.contains(/Đổi mật khẩu/i).should('exist')
})

// ================= UPDATE PROFILE =================
it('Cập nhật thông tin cá nhân', () => {
    cy.get('input[name="firstName"]').clear().type('Test')
    cy.get('input[name="lastName"]').clear().type('User')

    cy.contains('Cập nhật').click()

    cy.contains(/thành công/i).should('exist')
})

it('Không cho cập nhật thông tin cá nhân khi thiếu họ', () => {
    cy.get('input[name="firstName"]').clear()
    cy.get('input[name="lastName"]').clear().type('User')

    cy.contains('Cập nhật').click()

    cy.contains(/Họ không được để trống/i).should('exist')
})

// ================= ADDRESS =================
it('Thêm địa chỉ', () => {
    cy.contains('Địa chỉ').click()

    cy.contains('Thêm địa chỉ').click()

    cy.get('#newRecipientName').type('Test User')
    cy.get('#newPhoneNumber').type('0123456789')

    cy.get('#newProvinceOrCity').select(1)
    cy.wait(500)

    cy.get('#newDistrict').select(1)
    cy.wait(500)

    cy.get('#newWard').select(1)

    cy.get('#newStreetDetails').type('123 Test Street')

    cy.contains('Thêm').click()

    cy.contains('Test User').should('exist')
})

it('Không cho thêm địa chỉ khi thiếu số điện thoại', () => {
    cy.contains('Địa chỉ').click()
    cy.contains('Thêm địa chỉ').click()

    cy.get('#newRecipientName').type('Test User')
    cy.get('#newStreetDetails').type('123 Test Street')

    cy.contains('#addAddressModal button', 'Thêm').click()
    cy.contains(/Số điện thoại không được để trống/i).should('exist')
})

// ================= DELETE ADDRESS =================
it('Xóa địa chỉ', () => {
    cy.contains('Địa chỉ').click()

    cy.get('button[title="Xóa"]').first().click()

    cy.on('window:confirm', () => true)

    cy.reload()
})

// ================= CHANGE PASSWORD =================
it('Đổi mật khẩu', () => {
    cy.contains('Đổi mật khẩu').click()

    cy.get('#currentPassword').type(password)
    cy.get('#newPassword').type('Newpass123')
    cy.get('#confirmPassword').type('Newpass123')

    cy.contains('Đổi mật khẩu').click()

    cy.contains(/thành công/i).should('exist')
})

it('Không đổi mật khẩu khi xác nhận không khớp', () => {
    cy.contains('Đổi mật khẩu').click()

    cy.get('#currentPassword').type(password)
    cy.get('#newPassword').type('Newpass123')
    cy.get('#confirmPassword').type('NotMatch123')

    cy.contains('button', 'Đổi mật khẩu').click()
    cy.contains(/Mật khẩu xác nhận không khớp/i).should('exist')
})

// ================= DELETE ACCOUNT =================
it('Xóa tài khoản', () => {
    cy.contains('Xóa tài khoản').click()

    cy.get('input[name="password"]').type(password)

    cy.contains('Xóa vĩnh viễn').click()

    cy.url().should('include', '/login')
})
})
