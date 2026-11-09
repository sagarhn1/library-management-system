# E-Library Frontend Planning Notes

This file records the backend API analysis and the proposed React frontend architecture. The backend is treated as the source of truth. No backend behavior should be duplicated or reimplemented in React.

## Current Project State

The project contains an existing Spring Boot backend under `src/` and an existing Vite React frontend under `frontend/`.

The frontend currently appears to be the Vite starter app:

- `frontend/src/App.jsx`
- `frontend/src/main.jsx`
- `frontend/src/App.css`
- `frontend/src/index.css`

The frontend currently has no app-specific routing, API layer, authentication context, protected routes, or e-library UI.

## Backend Security Model

The backend uses Spring Security with stateless JWT authentication.

Public endpoints:

- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/logout`
- OAuth2 paths under `/oauth2/**` and `/login/oauth2/**`
- `/test-payment.html`

All other API endpoints require authentication unless their controller or security configuration changes later.

The frontend must send access tokens like this:

```http
Authorization: Bearer <accessToken>
```

The JWT subject is the user's email. The JWT also includes:

- `userId`
- `role`

Roles used by the backend:

- `ADMIN`
- `LIBRARIAN`
- `MEMBER`

The frontend may decode the JWT for client-side display and route gating, but server authorization remains authoritative.

## Authentication Endpoints

Base path: `/api/v1/auth`

### Register

`POST /api/v1/auth/register`

Request:

```json
{
  "firstName": "Sagar",
  "lastName": "H N",
  "email": "user@example.com",
  "phone": "9876543210",
  "password": "password123"
}
```

Validation rules:

- `firstName`: required, max 50
- `lastName`: required, max 50
- `email`: required, valid email, max 100, unique
- `phone`: required, Indian mobile format `^[6-9]\d{9}$`, unique
- `password`: required, 8 to 100 chars

Response: `TokenResponse`

```json
{
  "accessToken": "...",
  "refreshToken": "...",
  "tokenType": "Bearer",
  "expiresIn": 900
}
```

Self-registered users are created as `MEMBER`.

### Login

`POST /api/v1/auth/login`

Request:

```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

Response: `TokenResponse`

### Refresh

`POST /api/v1/auth/refresh`

Request:

```json
{
  "refreshToken": "..."
}
```

Response: new `TokenResponse`. The backend rotates refresh tokens by revoking the old refresh token and issuing a new pair.

### Logout

`POST /api/v1/auth/logout`

Request:

```json
{
  "refreshToken": "..."
}
```

Response: HTTP `204 No Content`.

## Error Response Format

The backend uses `ApiErrorResponse`:

```json
{
  "timestamp": "2026-09-12T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "error_message": ["Email is mandatory"],
  "path": "/api/v1/auth/register"
}
```

Frontend API handling should prefer:

1. `error_message` joined when present.
2. `message` when present.
3. HTTP status fallback.

## Book APIs

Base path: `/api/books`

### List Books

`GET /api/books`

Auth: authenticated users.

Supports Spring pageable query parameters such as:

- `page`
- `size`
- `sort`

Response: Spring `Page<BookResponse>`.

### Active Books

`GET /api/books/active`

Auth: authenticated users.

Response: `BookResponse[]`.

### Book By ID

`GET /api/books/{bookId}`

Auth: authenticated users.

Response: `BookResponse`.

### Book By ISBN

`GET /api/books/isbn/{isbn}`

Auth: authenticated users.

Response: `BookResponse`.

### Search Books

`POST /api/books/search?page=&size=&sort=`

Auth: authenticated users.

Request: `BookSearchRequest`

```json
{
  "keyword": "java",
  "authorId": 1,
  "genreId": 2,
  "publisherId": 3,
  "language": "English",
  "accessType": "FREE",
  "active": true,
  "minPrice": 0,
  "maxPrice": 500
}
```

All filters are optional. `accessType` is either `FREE` or `PAID`.

Response: Spring `Page<BookResponse>`.

### Create Book

`POST /api/books`

Auth: `ADMIN` or `LIBRARIAN`.

Request: `CreateBookRequest`

```json
{
  "isbn": "9781234567890",
  "title": "Book Title",
  "authorId": 1,
  "genreId": 2,
  "publisherId": 3,
  "publishDate": "2026-09-12",
  "language": "English",
  "description": "Description",
  "accessType": "PAID",
  "price": 299.00,
  "accessDurationDays": 30
}
```

Validation rules:

- `isbn`: required
- `title`: required
- `authorId`: required
- `genreId`: required
- `publisherId`: required
- `language`: required
- `accessType`: required, `FREE` or `PAID`
- `price`: cannot be negative
- `accessDurationDays`: required, minimum 1
- `FREE` books must have zero or null price
- `PAID` books must have price greater than zero

Response: `BookResponse`.

### Update Book

`PUT /api/books/{bookId}`

Auth: `ADMIN` or `LIBRARIAN`.

Request: `UpdateBookRequest`, same as create plus:

```json
{
  "active": true
}
```

The backend rejects activation through update if no e-book file has been uploaded.

### Delete Book

`DELETE /api/books/{bookId}`

Auth: `ADMIN`.

Response: success string.

### Activate / Deactivate Book

- `PATCH /api/books/{bookId}/activate`
- `PATCH /api/books/{bookId}/deactivate`

Auth: `ADMIN` or `LIBRARIAN`.

Response: success string.

### Upload E-book PDF

`POST /api/books/{bookId}/ebook`

Auth: `ADMIN`.

Content type: `multipart/form-data`

Form field:

- `file`

Only PDF files are accepted. A successful upload activates the book.

### Read E-book

`GET /api/books/{bookId}/read`

Auth: authenticated users.

Response: PDF resource with `Content-Type: application/pdf` and inline content disposition.

Rules:

- Book must be active.
- Book must have an uploaded e-book file.
- `ADMIN` can read without user book access validation.
- Non-admin users need valid `UserBookAccess`.
- Access must not be revoked.
- Access must not be expired.

The frontend must use this secure endpoint for reading. It must not create a permanent PDF download feature.

### Upload Cover

`POST /api/books/{bookId}/cover`

Auth: `ADMIN`.

Content type: `multipart/form-data`

Form field:

- `file`

Only image content types are accepted.

### Get Cover

`GET /api/books/{bookId}/cover`

Auth: authenticated users.

Response: image resource.

Because this endpoint is authenticated, the frontend should fetch the cover as a blob with the JWT header and render it via an object URL.

## Book Response DTO

`BookResponse`:

```json
{
  "id": 1,
  "isbn": "9781234567890",
  "title": "Book Title",
  "authorId": 1,
  "authorName": "Author Name",
  "genreId": 2,
  "genreName": "Genre Name",
  "publisherId": 3,
  "publisherName": "Publisher Name",
  "publishDate": "2026-09-12",
  "language": "English",
  "description": "Description",
  "accessType": "FREE",
  "price": 0,
  "accessDurationDays": 30,
  "coverImageUrl": "/api/books/1/cover",
  "userBookAction": "GET_FREE",
  "active": true,
  "createdAt": "2026-09-12T10:00:00",
  "updatedAt": "2026-09-12T10:00:00"
}
```

`userBookAction` is central to the frontend:

- `READ`: user can open the secure reader.
- `GET_FREE`: user can request free access.
- `BUY`: user must pay through Razorpay.
- `EXPIRED`: prior access exists but is expired.

The frontend should not recalculate this state when the backend already provides it.

## User Book Access APIs

Base path: `/api/book-access`

Auth: authenticated users.

### Request Free Book Access

`POST /api/book-access/books/{bookId}`

Returns: `UserBookAccessResponse`.

This endpoint grants or renews access for a free book, subject to backend rules.

### Get Access For Book

`GET /api/book-access/books/{bookId}`

Returns: `UserBookAccessResponse`.

### My Accessible Books

- `GET /api/book-access/my-books`
- `GET /api/book-access/my-library`

Both currently call the same service method and return active, non-revoked, non-expired access records.

Response: `UserBookAccessResponse[]`.

### Tested Free Access Endpoint

`POST /api/book-access/books/{bookId}/free-access`

Returns: success string.

For frontend implementation, use this tested endpoint. Do not replace it with `POST /api/book-access/books/{bookId}` unless the backend source and project tests are intentionally changed later.

## User Book Access Response DTO

```json
{
  "id": 1,
  "userId": 5,
  "bookId": 10,
  "bookTitle": "Book Title",
  "accessGrantedAt": "2026-09-12T10:00:00",
  "accessExpiresAt": "2026-10-12T10:00:00",
  "revoked": false,
  "active": true
}
```

## FREE Book Flow

Frontend flow:

1. User opens catalog or book detail.
2. Backend returns `BookResponse.userBookAction`.
3. If action is `GET_FREE`, show a single "Get free access" action.
4. On click, call `POST /api/book-access/books/{bookId}/free-access` with JWT.
5. On success, refetch `GET /api/books/{bookId}` or update local state.
6. Backend should then return `userBookAction: "READ"`.
7. User clicks read.
8. Frontend fetches `GET /api/books/{bookId}/read` with JWT and renders PDF inline.

Do not make the user manually call both access and read endpoints.

## PAID Razorpay Flow

Payment APIs are for `MEMBER` users.

### Create Payment

`POST /api/payments/create`

Request:

```json
{
  "bookId": 10
}
```

Response:

```json
{
  "paymentId": 1,
  "razorpayOrderId": "order_xxx",
  "razorpayKeyId": "rzp_xxx",
  "amount": 299.00,
  "currency": "INR",
  "bookTitle": "Book Title"
}
```

Backend rules:

- Book must be active.
- E-book file must be available.
- Book must be `PAID`.
- User must not already have active access.
- Price must be greater than zero.
- Amount sent to Razorpay is converted to paise on the backend.

### Verify Payment

`POST /api/payments/verify`

Request:

```json
{
  "razorpayPaymentId": "pay_xxx",
  "razorpayOrderId": "order_xxx",
  "razorpaySignature": "signature_xxx"
}
```

Response: success string.

On successful verification, the backend marks the payment as `SUCCESS` and grants book access.

### Mark Failed Payment

`POST /api/payments/failed`

Request:

```json
{
  "razorpayOrderId": "order_xxx"
}
```

Response: success string.

Frontend flow:

1. User clicks Buy.
2. Frontend calls `POST /api/payments/create`.
3. Backend returns `razorpayOrderId`, `razorpayKeyId`, `amount`, `currency`, and `bookTitle`.
4. Frontend opens Razorpay Checkout using only the public key ID from the response.
5. On Razorpay success callback, frontend calls `POST /api/payments/verify`.
6. On verification success, frontend refetches the book.
7. Backend should then return `userBookAction: "READ"`.
8. User can open the secure reader.
9. On Razorpay failure/dismiss where an order exists, frontend calls `POST /api/payments/failed`.

The frontend must not expose or store Razorpay secret keys.

### Razorpay Webhook

`POST /api/payments/webhook`

This is server-to-server. The frontend must not call it.

## Author APIs

Base path: `/api/authors`

- `POST /api/authors`: `ADMIN`, `LIBRARIAN`
- `GET /api/authors`: `ADMIN`, `LIBRARIAN`, `MEMBER`
- `GET /api/authors/{authorId}`: `ADMIN`, `LIBRARIAN`, `MEMBER`
- `PUT /api/authors/{authorId}`: `ADMIN`, `LIBRARIAN`
- `DELETE /api/authors/{authorId}`: `ADMIN`
- `PATCH /api/authors/{authorId}/activate`: `ADMIN`, `LIBRARIAN`
- `PATCH /api/authors/{authorId}/deactivate`: `ADMIN`, `LIBRARIAN`
- `GET /api/authors/active`: `ADMIN`, `LIBRARIAN`, `MEMBER`

Create request:

```json
{
  "name": "Author Name",
  "biography": "Biography",
  "dateOfBirth": "1990-01-01",
  "nationality": "Indian",
  "email": "author@example.com"
}
```

Update request adds:

```json
{
  "active": true
}
```

## Genre APIs

Base path: `/api/genres`

- `POST /api/genres`: `ADMIN`, `LIBRARIAN`
- `GET /api/genres`: `ADMIN`, `LIBRARIAN`, `MEMBER`
- `GET /api/genres/{genreId}`: `ADMIN`, `LIBRARIAN`, `MEMBER`
- `PUT /api/genres/{genreId}`: `ADMIN`, `LIBRARIAN`
- `DELETE /api/genres/{genreId}`: `ADMIN`
- `GET /api/genres/top-level`: `ADMIN`, `LIBRARIAN`, `MEMBER`
- `GET /api/genres/{parentGenreId}/subgenres`: `ADMIN`, `LIBRARIAN`, `MEMBER`
- `GET /api/genres/active`: `ADMIN`, `LIBRARIAN`, `MEMBER`
- `PATCH /api/genres/{genreId}/deactivate`: `ADMIN`, `LIBRARIAN`
- `PATCH /api/genres/{genreId}/activate`: `ADMIN`, `LIBRARIAN`

Create request:

```json
{
  "code": "FIC",
  "name": "Fiction",
  "description": "Fiction books",
  "active": true,
  "parentGenreId": null
}
```

Response:

```json
{
  "id": 1,
  "code": "FIC",
  "name": "Fiction",
  "description": "Fiction books",
  "displayOrder": 1,
  "active": true,
  "parentGenreId": null,
  "subGenreIds": [],
  "createdAt": "2026-09-12T10:00:00",
  "updatedAt": "2026-09-12T10:00:00"
}
```

## Publisher APIs

Base path: `/api/publishers`

- `POST /api/publishers`: `ADMIN`, `LIBRARIAN`
- `GET /api/publishers`: `ADMIN`, `LIBRARIAN`, `MEMBER`
- `GET /api/publishers/{publisherId}`: `ADMIN`, `LIBRARIAN`, `MEMBER`
- `PUT /api/publishers/{publisherId}`: `ADMIN`, `LIBRARIAN`
- `DELETE /api/publishers/{publisherId}`: `ADMIN`
- `PATCH /api/publishers/{publisherId}/activate`: `ADMIN`, `LIBRARIAN`
- `PATCH /api/publishers/{publisherId}/deactivate`: `ADMIN`, `LIBRARIAN`
- `GET /api/publishers/active`: `ADMIN`, `LIBRARIAN`, `MEMBER`

Create request:

```json
{
  "name": "Publisher Name",
  "address": "Address",
  "email": "publisher@example.com",
  "phone": "9876543210",
  "website": "https://example.com"
}
```

Update request adds:

```json
{
  "active": true
}
```

## User Admin APIs

Base path: `/api/v1/users`

Controller-level auth: `ADMIN`.

- `POST /api/v1/users`
- `GET /api/v1/users/{id}`
- `GET /api/v1/users`
- `PUT /api/v1/users/{id}`
- `DELETE /api/v1/users/{id}`

List filters:

- `role`
- `active`
- `firstName`
- `lastName`
- `email`
- `page`
- `size`
- `sortBy`
- `direction`

The frontend should expose these only in admin UI.

## Important Frontend Integration Notes

### Authenticated Images And PDFs

The cover and PDF endpoints require authentication. Plain browser tags cannot attach `Authorization` headers:

- `<img src="/api/books/1/cover">`
- `<iframe src="/api/books/1/read">`

Those may fail unless the app uses cookies or a proxy-auth mechanism, which the backend does not currently provide.

Recommended approach:

- Use authenticated `fetch`.
- Convert response to `Blob`.
- Create an object URL via `URL.createObjectURL(blob)`.
- Use the object URL in `img`, `iframe`, `embed`, or a PDF viewer.
- Revoke object URLs when components unmount.

### CORS / Proxy

The backend inspection did not show a CORS configuration. Since the React app runs on `http://localhost:5173` and Spring Boot commonly runs on `http://localhost:8080`, the clean frontend-side setup is a Vite dev proxy:

```js
server: {
  proxy: {
    '/api': 'http://localhost:8080',
    '/oauth2': 'http://localhost:8080',
    '/login': 'http://localhost:8080'
  }
}
```

This avoids changing backend CORS configuration.

## Proposed Frontend Architecture

Recommended structure inside `frontend/src`:

```txt
src/
  app/
    App.jsx
    routes.jsx
  api/
    http.js
    authApi.js
    booksApi.js
    accessApi.js
    paymentsApi.js
    authorsApi.js
    genresApi.js
    publishersApi.js
    usersApi.js
  auth/
    AuthContext.jsx
    RequireAuth.jsx
    RequireRole.jsx
    tokenStorage.js
    jwt.js
  pages/
    LoginPage.jsx
    RegisterPage.jsx
    CatalogPage.jsx
    BookDetailsPage.jsx
    MyLibraryPage.jsx
    ReaderPage.jsx
    PaymentResultPage.jsx
    AdminDashboardPage.jsx
    ManageBooksPage.jsx
    ManageAuthorsPage.jsx
    ManageGenresPage.jsx
    ManagePublishersPage.jsx
    ManageUsersPage.jsx
  components/
    layout/
      AppShell.jsx
      Navbar.jsx
      Sidebar.jsx
    books/
      BookCard.jsx
      BookGrid.jsx
      BookFilters.jsx
      BookActionButton.jsx
      CoverImage.jsx
    forms/
      BookForm.jsx
      AuthorForm.jsx
      GenreForm.jsx
      PublisherForm.jsx
    common/
      Button.jsx
      Input.jsx
      Select.jsx
      Modal.jsx
      Pagination.jsx
      EmptyState.jsx
      Spinner.jsx
      ErrorMessage.jsx
  hooks/
    useAsync.js
    useBlobUrl.js
  utils/
    apiError.js
    money.js
    dates.js
```

## Proposed Services

### `api/http.js`

Responsibilities:

- Set base URL or use relative `/api` URLs.
- Attach `Authorization: Bearer <accessToken>`.
- Parse JSON responses.
- Handle string responses.
- Handle `204 No Content`.
- Parse `ApiErrorResponse`.
- Optionally attempt refresh once on `401`.

### `api/authApi.js`

Methods:

- `register(payload)`
- `login(payload)`
- `refresh(refreshToken)`
- `logout(refreshToken)`

### `api/booksApi.js`

Methods:

- `listBooks(params)`
- `searchBooks(filters, params)`
- `getBook(bookId)`
- `getBookByIsbn(isbn)`
- `getActiveBooks()`
- `createBook(payload)`
- `updateBook(bookId, payload)`
- `deleteBook(bookId)`
- `activateBook(bookId)`
- `deactivateBook(bookId)`
- `uploadEbook(bookId, file)`
- `uploadCover(bookId, file)`
- `fetchCoverBlob(bookId)`
- `fetchReadPdfBlob(bookId)`

### `api/accessApi.js`

Methods:

- `requestFreeBookAccess(bookId)`
- `getBookAccess(bookId)`
- `getMyBooks()`
- `getMyLibrary()`

### `api/paymentsApi.js`

Methods:

- `createPayment(bookId)`
- `verifyPayment(payload)`
- `markPaymentFailed(razorpayOrderId)`

### Metadata APIs

Authors:

- `listAuthors()`
- `listActiveAuthors()`
- `createAuthor(payload)`
- `updateAuthor(id, payload)`
- `deleteAuthor(id)`
- `activateAuthor(id)`
- `deactivateAuthor(id)`

Genres:

- `listGenres()`
- `listActiveGenres()`
- `listTopLevelGenres()`
- `listSubgenres(parentGenreId)`
- CRUD/status methods

Publishers:

- `listPublishers()`
- `listActivePublishers()`
- CRUD/status methods

Users:

- Admin-only user CRUD and filtering.

## Proposed Pages And Backend Communication

### Login Page

Calls:

- `POST /api/v1/auth/login`

Behavior:

- Store access token and refresh token.
- Decode role from JWT.
- Redirect `MEMBER` to catalog or my library.
- Redirect `ADMIN`/`LIBRARIAN` to dashboard or catalog.

### Register Page

Calls:

- `POST /api/v1/auth/register`

Behavior:

- Register a `MEMBER`.
- Store returned tokens.
- Redirect to catalog.

### Catalog Page

Calls:

- `POST /api/books/search`
- `GET /api/authors/active`
- `GET /api/genres/active`
- `GET /api/publishers/active`

Behavior:

- Show searchable, filterable, paginated books.
- Display access type, price, cover, author, genre, publisher.
- Render CTA based on `userBookAction`.

### Book Details Page

Calls:

- `GET /api/books/{bookId}`
- action-dependent calls below

Behavior by `userBookAction`:

- `READ`: route to `/reader/:bookId`.
- `GET_FREE`: call `POST /api/book-access/books/{bookId}/free-access`, then refetch book.
- `BUY`: start Razorpay flow.
- `EXPIRED`: renew using free or paid flow depending on `accessType`.

### My Library Page

Calls:

- `GET /api/book-access/my-library`

Behavior:

- Show active accessible books.
- Each item links to reader.
- Since the access response only includes `bookId` and `bookTitle`, the UI may fetch book details per item if cover/metadata is required.

### Reader Page

Calls:

- `GET /api/books/{bookId}/read`

Behavior:

- Fetch PDF as authenticated blob.
- Render inline.
- No permanent download button.
- Handle access expired/revoked/not-found errors using backend messages.

### Payment Flow Component

Calls:

- `POST /api/payments/create`
- Razorpay Checkout script
- `POST /api/payments/verify`
- `POST /api/payments/failed`

Behavior:

- Use `razorpayKeyId` from backend response.
- Do not store or expose secret keys.
- Refetch book after payment verification.

### Admin Dashboard Page

Calls:

- Summary can initially use existing list endpoints.

Visible to:

- `ADMIN`
- optionally `LIBRARIAN` with reduced navigation

### Manage Books Page

Calls:

- `GET /api/books`
- `POST /api/books`
- `PUT /api/books/{bookId}`
- `PATCH /api/books/{bookId}/activate`
- `PATCH /api/books/{bookId}/deactivate`
- `DELETE /api/books/{bookId}` for `ADMIN`
- `POST /api/books/{bookId}/ebook` for `ADMIN`
- `POST /api/books/{bookId}/cover` for `ADMIN`
- metadata endpoints for authors, genres, publishers

Behavior:

- Enforce frontend validation consistent with backend.
- Allow PDF upload only where permitted.
- Allow cover image upload only where permitted.

### Manage Authors Page

Calls:

- Author CRUD/status endpoints.

Visible to:

- `ADMIN`
- `LIBRARIAN` for create/update/status
- Delete only for `ADMIN`

### Manage Genres Page

Calls:

- Genre CRUD/status endpoints.
- Top-level/subgenre endpoints where useful.

Visible to:

- `ADMIN`
- `LIBRARIAN` for create/update/status
- Delete only for `ADMIN`

### Manage Publishers Page

Calls:

- Publisher CRUD/status endpoints.

Visible to:

- `ADMIN`
- `LIBRARIAN` for create/update/status
- Delete only for `ADMIN`

### Manage Users Page

Calls:

- `/api/v1/users`

Visible to:

- `ADMIN` only.

## Routing Plan

Suggested routes:

```txt
/login
/register
/catalog
/books/:bookId
/my-library
/reader/:bookId
/admin
/admin/books
/admin/authors
/admin/genres
/admin/publishers
/admin/users
```

Protected routing:

- All app pages except login/register require authentication.
- Admin routes require `ADMIN`.
- Librarian management routes require `ADMIN` or `LIBRARIAN`, except users and destructive deletes.

## Recommended Implementation Order

1. Add routing and app shell.
2. Add API client and error parser.
3. Add auth context, token storage, JWT role decoding, protected routes.
4. Implement login/register.
5. Implement catalog search/list with metadata filters.
6. Implement book detail with `userBookAction` handling.
7. Implement free access flow.
8. Implement secure reader using authenticated PDF blob.
9. Implement Razorpay create/verify/failed flow.
10. Implement my library.
11. Implement admin/librarian management pages.
12. Add build/lint verification.

## Notes And Risks

- `GET /api/books/{bookId}/cover` and `GET /api/books/{bookId}/read` require authenticated fetch; direct `src` URLs are likely insufficient.
- Vite proxy is recommended unless backend CORS is later configured.
- The frontend should trust `userBookAction` from `BookResponse`.
- The frontend should not create any permanent PDF download feature.
- Razorpay webhook is backend-only.
- Backend files should not be modified during frontend implementation unless explicitly requested.
