# book-selling-J2EE

Ung dung Spring Boot cho bai toan ban sach (Book Selling).

## Quick Start

### Chay du an
```bash
.\\mvnw.cmd spring-boot:run
```

### Chay test
```bash
.\\mvnw.cmd test
```

## CRUD API co ban

- `/api/authors`
- `/api/categories`
- `/api/publishers`
- `/api/books`

## Tai lieu tieng Viet

- Tai lieu tong hop: `JAVA_DOCUMENTATION_VI.md`
- Tai lieu JavaDoc package: `src/main/java/com/group/book_selling/package-info.java`

## Ghi chu

- Cac endpoint `/api/**` hien tai duoc bao ve boi cau hinh `SecurityConfig` (yeu cau xac thuc).
- Truoc khi production, can bo sung service layer, xu ly exception tap trung va hoan thien chinh sach bao mat chat hon.