# Tai lieu Java co ban (Tieng Viet)

## 1. Muc tieu setup
Du an hien da duoc setup phan nen tang de bat dau phat trien he thong ban sach:
- Spring Boot + Spring Data JPA + Validation + Security.
- Model JPA cho cac doi tuong chinh.
- CRUD REST API co ban cho:
  - Author: `/api/authors`
  - Category: `/api/categories`
  - Publisher: `/api/publishers`
  - Book: `/api/books`

## 2. Cau truc package chinh
- `com.group.book_selling.models`: Entity va enum.
- `com.group.book_selling.repository`: Interface JPA Repository.
- `com.group.book_selling.controllers`: REST controller CRUD co ban + DTO request.
- `com.group.book_selling.utils`: Cau hinh bao mat, helper slug.
- `com.group.book_selling.validators`: Annotation/validator custom.

## 3. Cac endpoint CRUD co ban
### Author
- `GET /api/authors`: Danh sach tac gia.
- `GET /api/authors/{id}`: Chi tiet tac gia.
- `POST /api/authors`: Tao tac gia.
- `PUT /api/authors/{id}`: Cap nhat tac gia.
- `DELETE /api/authors/{id}`: Xoa tac gia.

### Category
- `GET /api/categories`
- `GET /api/categories/{id}`
- `POST /api/categories`
- `PUT /api/categories/{id}`
- `DELETE /api/categories/{id}`

### Publisher
- `GET /api/publishers`
- `GET /api/publishers/{id}`
- `POST /api/publishers`
- `PUT /api/publishers/{id}`
- `DELETE /api/publishers/{id}`

### Book
- `GET /api/books`
- `GET /api/books/{id}`
- `POST /api/books`
- `PUT /api/books/{id}`
- `DELETE /api/books/{id}`

`Book` su dung DTO `BookRequest`, trong do:
- `authorIds`, `categoryIds`, `publisherId` la ID tham chieu sang bang lien quan.
- `formats` la danh sach `BookFormat` de luu cac phien ban (physical/digital/audiobook).

## 4. Bao mat co ban
Trong `SecurityConfig` da mo endpoint `/api/**` de dev/test nhanh.
- Da tat CSRF cho setup API JSON co ban.
- Khi len production, can bat lai CSRF (neu dung session/form) hoac chuyen sang JWT + stateless.

## 5. Cach chay va test
### Chay ung dung
```bash
.\\mvnw.cmd spring-boot:run
```

### Chay test
```bash
.\\mvnw.cmd test
```

## 6. Huong mo rong tiep theo (goi y)
- Them service layer (`services`) de tach business logic khoi controller.
- Them DTO response rieng de tranh tra thang entity.
- Them xu ly loi tap trung bang `@ControllerAdvice`.
- Them pagination/search cho danh sach sach.
- Them OpenAPI/Swagger de tai lieu hoa API tu dong.
