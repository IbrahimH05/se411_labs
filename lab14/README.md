# Lab 14 — Spring CRUD

SE411, Fall 2026/2027. A full CRUD REST API for `Course`, with the exact status codes
the handout's route table specifies, and Bean Validation on the create endpoint.

## Files

| File | What it is |
| --- | --- |
| [`model/Course.java`](src/main/java/psu/lab14/model/Course.java) | The record, carrying the handout's four validation rules |
| [`repositories/CourseRepository.java`](src/main/java/psu/lab14/repositories/CourseRepository.java) | Spring Data JDBC interface |
| [`services/CourseService.java`](src/main/java/psu/lab14/services/CourseService.java) | Create / read / update / delete logic |
| [`controllers/CourseController.java`](src/main/java/psu/lab14/controllers/CourseController.java) | The five endpoints and their status codes |
| [`web/ApiExceptionHandler.java`](src/main/java/psu/lab14/web/ApiExceptionHandler.java) | Validation → 400, duplicate key → 422 |
| [`schema.sql`](src/main/resources/schema.sql) · [`data.sql`](src/main/resources/data.sql) | Table and six seed courses |
| [`CourseCrudTest.java`](src/test/java/psu/lab14/CourseCrudTest.java) | 16 tests |

## Running it

```bash
mvn clean spring-boot:run
```

```bash
curl http://localhost:8080/api/v1/courses
```

```bash
mvn test
```

Spring Boot 4.0.5, Java 17 target, built on OpenJDK 26 with Maven 3.9.16.

---

## The API

| Operation | Route | Method | Codes |
| --- | --- | --- | --- |
| Create | `/api/v1/courses` + JSON body | POST | **201** + `Location` · 400 invalid · **422** id taken |
| Retrieve all | `/api/v1/courses` | GET | 200 |
| Retrieve by id | `/api/v1/courses/{id}` | GET | 200 · 404 |
| Update | `/api/v1/courses/{id}` + JSON body | PUT | **204** · 404 |
| Delete | `/api/v1/courses/{id}` | DELETE | **204** · 404 |

### Verified against the running server

```text
ROUTE                                   EXPECTED   ACTUAL
GET    /courses                         200        200
GET    /courses/1                       200        200
GET    /courses/999                     404        404
POST   /courses (id 7)                  201        201
POST   /courses (id 7 again)            422        422
GET    /courses/7                       200        200
PUT    /courses/7                       204        204
PUT    /courses/999                     404        404
DELETE /courses/7                       204        204
DELETE /courses/7 again                 404        404
POST   /courses (no id)                 201        201

--- validation (handout's four rules) ---
POST negative id                        400        400
POST empty code                         400        400
POST 1-character name                   400        400
POST credits = 0                        400        400
POST credits = 11                       400        400

--- a validation error body ---
{"status":400,"error":"Bad Request","errors":{"code":"code cannot be empty","credits":"credits must be at most 10","id":"id cannot be negative","name":"name must be at least 2 characters"}}```

### Why those codes

**201 + `Location`** — the response says *where the thing now lives*, so the client
does not have to guess the URL it just created. Built with
`UriComponentsBuilder` from the saved id rather than string concatenation.

**422, not 400, for a taken id** — 400 means "I could not parse this". The request was
perfectly well-formed; it just conflicts with state that already exists. Saying 400
would send the client off hunting for a syntax error that is not there.

**204 for update and delete** — the operation succeeded and there is deliberately no
body. Returning 200 with the updated object is a defensible alternative, but the
handout specifies 204 and it is the more honest answer for a request whose result is
"done".

**404 before doing anything** — update and delete both check existence first, so
deleting a course that was never there is a 404 rather than a silent 204. "Nothing
happened" and "the thing you asked about does not exist" are different answers.

---

## Two real bugs found by testing the endpoints

Both passed compilation and both would have passed a casual demo.

### 1. `save()` silently discarded every created course

The first version used `courseRepository.save(course)` for create. POSTing a course
with id 7 returned **201 with `Location: /api/v1/courses/7`** — and then
`GET /api/v1/courses/7` returned **404**. The row was never written.

**Spring Data JDBC's `save()` is an upsert that decides INSERT or UPDATE by whether
the id is null.** A new course arriving *with* an id is therefore issued as an UPDATE,
matches zero rows, and vanishes. `save()` returns the object either way, so the
controller sees success and reports 201 over an empty database.

The fix is to say what was meant:

```java
public Course create(Course course) {
    return jdbcTemplate.insert(course);   // always an INSERT
}
```

`update()` still uses `save()`, which is correct there because the id exists.

### 2. Seeding explicit ids left the IDENTITY sequence at zero

POSTing *without* an id — asking the database to assign one — returned **500**:

```text
DuplicateKeyException: Unique index or primary key violation:
  PRIMARY KEY ON PUBLIC.COURSE(ID) ( /* key:1 */ CAST(1 AS BIGINT), 'SE411', ... )
```

`data.sql` inserts ids 1–6 explicitly, and an explicit insert into a
`GENERATED BY DEFAULT AS IDENTITY` column **does not advance the sequence**. The
generator still believed the next free id was 1, straight into SE411.

```sql
ALTER TABLE course ALTER COLUMN id RESTART WITH 7;
```

Both bugs have a regression test, because both are invisible from the response alone —
the first reports success, and the second only appears when a client omits the id.

---

## Validation

The handout's four rules, as annotations on the record:

| Rule | Annotation |
| --- | --- |
| The id cannot be negative | `@PositiveOrZero` |
| The code cannot be empty | `@NotBlank` |
| The name cannot be shorter than two characters | `@Size(min = 2)` |
| The credits should be from 1 to 10 | `@Min(1) @Max(10)` |

`@Valid` on the `@RequestBody` runs them. `@PositiveOrZero` rather than `@Positive`
because the stated rule is "cannot be negative", and it also permits a **null** id —
which is how a client asks the database to assign one.

Every violation is reported at once rather than one per round trip:

```json
{"status":400,"error":"Bad Request","errors":{
  "code":"code cannot be empty",
  "credits":"credits must be at most 10",
  "id":"id cannot be negative",
  "name":"name must be at least 2 characters"}}
```

As in Lab 13, this needs a `@RestControllerAdvice`. Without one, a body violation
produces Spring's default error shape and a *path variable* violation escapes as a
500 — the server blaming itself for the client's bad request.

---

## Tests

```text
Tests run: 16, Failures: 0, Errors: 0, Skipped: 0
```

| Group | Tests |
| --- | --- |
| Read | All courses; by id; unknown id is 404 |
| Create | **201 + `Location`, and the row really exists**; duplicate id is 422; **no id lets the database assign one** |
| Update | 204 and the change is visible; absent course is 404 |
| Delete | 204 then the course is gone; absent course is 404 |
| Validation | Each of the four rules; **both credit boundaries (1 and 10) accepted**, 0 and 11 rejected; all violations reported together |

The create tests do not stop at the status code — they follow up with a `GET`. A test
that only asserted `201` would have passed against the broken `save()` version, since
that is exactly what the bug looked like from outside.

The boundary test matters for the same reason as every other `<` versus `<=` in these
labs: `@Min(1) @Max(10)` is inclusive, the handout says "from 1 to 10", and nothing in
the response would reveal an off-by-one.

---

## Notes

**1. The handout says Spring Boot 4.0.5; Lab 13 says 4.1.2.** Each project uses the
version its own handout names. The `AutoConfigureMockMvc` package move applies to both
— see Lab 13's notes.

**2. The update ignores the id in the body** and uses the one from the URL. The URL
identifies the resource being addressed; a body claiming a different id is at best
redundant and at worst an attempt to rename a row by PUTting at the wrong address.

**3. `existsByCodeIgnoreCase` is declared but unused.** Spring Data derives the query
from the method name alone. It is left in place because a course code is the obvious
real-world uniqueness rule, but the handout only asks for id collisions to be
rejected, so adding a second 422 path would go beyond the specified behaviour.

**4. `schema.sql` drops the table first**, for the same reason as Lab 13 — Spring runs
it on every start and the H2 instance can outlive a restart.
