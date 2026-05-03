# Test Suite — Complete Explanation

This document explains every unit test file in the repository `app/src/test/java/com/example/debugz/`. For each test file I provide:
- Purpose and coverage
- Details on test setup
- A method-by-method explanation describing the actions, the intermediate state, and exactly why each assertion is valid
- Notes on limitations and suggested additional tests

Test files covered
- `EventTest.java`
- `StudentTest.java`
- `OrganizerTest.java`
- `RegistrationTest.java`
- `ModelIntegrationTest.java`
- `ExampleUnitTest.java`

All tests are plain JUnit host-side unit tests that operate in-memory (no Android instrumentation) and are focused mainly on model behavior. They intentionally avoid persistence and controller/integration concerns except for a few tests that simulate Firestore deserialization patterns (default constructor + setters).

---

## 1) EventTest.java
Path: `app/src/test/java/com/example/debugz/EventTest.java`

Purpose
- Unit tests for the `Event` model covering constructor behavior, getters/setters, attendee management, and capacity-related calculations.
- Also includes tests that simulate Firestore deserialization.

Test setup
- `@Before setUp()` constructs an `Event` instance named `event` used by most tests. There are two merged variants in the file; the active tests expect an `Event` constructed with event id `event_001` and `maxCapacity` 200 (and ticket price 0.0) — the rest of the suite is written against that assumption.

Test-by-test explanation

- testParameterizedConstructor_setsAllFields()
  - Verifies the parameterized constructor correctly assigned: eventId, title, description, location, date, time, organizerId, maxCapacity, and ticketPrice.
  - Assertions check equality for each field and a floating-point delta for `ticketPrice`.

- testParameterizedConstructor_initializesEmptyAttendeeList()
  - Ensures that the `attendeeIds` list is initialized and not null, and begins empty.
  - This verifies the constructor's defensive initialization to avoid NPEs.

- testDefaultConstructor_initializesEmptyAttendeeList()
  - Creates `new Event()` and asserts its `attendeeIds` list is non-null and empty — used to confirm Firestore-friendly default construction.

- testDefaultConstructor_fieldsAreDefaults() (HEAD variant)
  - Confirms that fields are null/zero defaults when using the no-arg constructor: IDs and strings null, capacity and ticketPrice zero.

- testGettersAndSetters() (pr-15 variant)
  - Mutates `event` using setters and verifies getters reflect updated values. Tests `setTitle`, `setMaxCapacity`, `setPrice`.

- testSetAndGetTicketPrice()
  - Calls `event.setTicketPrice(500.0)` and asserts the getter returns the expected numeric price.

- testSetAndGetEventId(), testSetAndGetTitle(), testSetAndGetDescription(), testSetAndGetLocation(), testSetAndGetDate(), testSetAndGetTime(), testSetAndGetOrganizerId(), testSetAndGetMaxCapacity()
  - Each is a small canonical test that sets a single property and asserts it changed correctly. These are straightforward getters/setters validation ensuring no accidental side effects.

- testAddAttendee_addsNewStudent()
  - Calls `event.addAttendee("stu_001")`; verifies `attendeeIds` contains the id and size is 1.
  - Confirms basic add behavior.

- testAddAttendee_preventsDuplicates()
  - Calls `addAttendee("stu_001")` twice and asserts the list size remains 1 — demonstrates the duplicate-prevention code path.

- testAddAttendee_multipleDistinctStudents()
  - Adds three distinct IDs and asserts size equals 3.

- testRemoveAttendee_removesExistingStudent()
  - Adds two IDs, removes one, asserts the removed id is absent and size decreased to 1.

- testRemoveAttendee_nonExistentId_doesNothing()
  - Adds one id, calls remove on a non-existent id, asserts size remains unchanged.

- testSetAttendeeIds_replacesEntireList()
  - Adds an "old_stu", calls `setAttendeeIds` passing a new list, asserts the old id is gone and new ids are present.
  - Tests the setter's replacement behavior and null-safety indirectly.

- testSpotsRemaining_whenEmpty(), testSpotsRemaining_afterSomeRSVPs(), testSpotsRemaining_atFullCapacity()
  - Compute remaining spots as `maxCapacity - attendeeIds.size()` in different scenarios.
  - These tests demonstrate capacity accounting assuming `maxCapacity` is correct and `attendeeIds` size reflects RSVPs.

- testCapacityPercentage_calculation()
  - Adds 160 attendees then computes integer percentage `((current / max) * 100)` and asserts 80.
  - Tests a derived calculation used by UI progress indicators.

- testRealisticEvent_LUMUN(), testRealisticEvent_smallStudyCircle()
  - Create larger realistic events (`lumun` with 650 capacity and ticket price 1500.0; `studyCircle` with capacity 20) and add attendees to them.
  - Assert title and attendee counts and that remaining spots equal expected values.

- testFirestoreMapping_defaultConstructorThenSetFields()
  - Simulates Firestore deserialization: create `new Event()`, set fields with setters, and then assert the fields via getters.
  - Useful to ensure the POJO shape maps back/forth with Firestore's `toObject` behavior.

- testEditEventDetails_US12()
  - Mutates several event fields (title, location, date, time, maxCapacity) and asserts they changed as intended.
  - This mirrors organizer edit flows in the app.

Notes & suggestions for EventTest
- These tests are thorough for in-memory behavior and getters/setters. They do not test Firestore interactions (beyond simulating object mapping) or controller-level capacity enforcement.
- Add tests for invalid inputs (e.g., negative capacities) and for boundary conditions (adding beyond capacity when higher-level code allows it).

---

## 2) StudentTest.java
Path: `app/src/test/java/com/example/debugz/StudentTest.java`

Purpose
- Unit tests for `Student` model verifying construction, profile fields, preference management, and registration list behavior.

Test setup
- `@Before` builds a `Student student` instance with id `stu_001` and profile data.

Test-by-test explanation

- testParameterizedConstructor_setsAllFields()
  - Confirms constructor assigns studentId, name, email, school, and year correctly.

- testParameterizedConstructor_initializesEmptyLists() and testDefaultConstructor_initializesEmptyLists()
  - Verify the lists `preferences` and `registrationIds` are non-null and empty for both parameterized and default constructors.

- testDefaultConstructor_fieldsAreNull()
  - New `Student()` should have null profile fields. This ensures Firestore-friendly deserialization behavior.

- testSetAndGetStudentId(), testSetAndGetName(), testSetAndGetEmail(), testSetAndGetSchool(), testSetAndGetYear()
  - Basic setter/getter checks for profile fields.

- testAddPreference_addsNewTag(), testAddPreference_preventsDuplicates(), testAddPreference_multipleDistinctTags()
  - Verify that `addPreference` appends a tag only if not present and supports multiple distinct tags.

- testSetPreferences_replacesEntireList()
  - Ensures `setPreferences` replaces the list rather than appending.

- testAddRegistration_addsNewId(), testAddRegistration_preventsDuplicates(), testAddRegistration_multipleDistinctRegistrations()
  - Check `addRegistration` adds registration ids and prevents duplicates.

- testRemoveRegistration_removesExistingId(), testRemoveRegistration_nonExistentId_doesNothing()
  - Verify removal behavior for registration ids, including no-op on non-existent id.

- testSetRegistrationIds_replacesEntireList()
  - Ensures `setRegistrationIds` replaces the student's registration list.

- testRealisticStudentProfile()
  - Builds a realistic `Student` for test purposes, adds preferences and registration ids, then asserts profile and list sizes.

Notes & suggestions for StudentTest
- Tests cover the model API thoroughly. They do not validate integration concerns: e.g., that registration ids refer to existing events or that email formats are valid. Consider adding tests for defensive behavior when nulls are passed into `addPreference` or `addRegistration`.

---

## 3) OrganizerTest.java
Path: `app/src/test/java/com/example/debugz/OrganizerTest.java`

Purpose
- Unit tests for the `Organizer` model covering constructors, getters/setters, and tracking of created event IDs.

Test setup
- `@Before` creates an `Organizer` instance `organizer` with id `org_001`, name "LUMS Drama Club", and an email.

Test-by-test explanation

- testParameterizedConstructor_setsAllFields()
  - Verifies id, name, and email are set by the constructor.

- testParameterizedConstructor_initializesEmptyEventList() and testDefaultConstructor_initializesEmptyEventList()
  - Ensure `createdEventIds` is non-null and empty in both constructors.

- testDefaultConstructor_fieldsAreNull()
  - Checks default constructor leaves profile fields null.

- testSetAndGetOrganizerId(), testSetAndGetName(), testSetAndGetEmail()
  - Standard setter/getter checks.

- testAddCreatedEvent_addsNewEventId(), testAddCreatedEvent_preventsDuplicates(), testAddCreatedEvent_multipleDistinctEvents()
  - Check `addCreatedEvent` behavior: adding event ids, preventing duplicates, and supporting multiple ids.

- testRemoveCreatedEvent_removesExistingId(), testRemoveCreatedEvent_nonExistentId_doesNothing()
  - Verify remove behavior including no-op on missing id.

- testSetCreatedEventIds_replacesEntireList()
  - Ensures `setCreatedEventIds` replaces the list of created event IDs.

- testRealisticOrganizer_LUMSCareerServicesOffice(), testRealisticOrganizer_studentSociety()
  - Create realistic organizer instances, add event ids, and assert expected sizes and names.

Notes & suggestions for OrganizerTest
- Good coverage for in-memory organizer bookkeeping. No Firestore/access control tests (those belong to integration/controller layers).

---

## 4) RegistrationTest.java
Path: `app/src/test/java/com/example/debugz/RegistrationTest.java`

Purpose
- Unit tests for `Registration` model verifying constructors, getters/setters, realistic scenarios, and simulated Firestore mapping.
- Focuses on `status` transitions and timestamp handling.

Test setup
- `@Before` creates a registration instance `registration` with id `reg_001`, student `stu_001`, event `event_001`, status `Confirmed`, and `testTimestamp`.

Test-by-test explanation

- testParameterizedConstructor_setsAllFields()
  - Asserts every field supplied to the constructor is retrievable via getters.

- testDefaultConstructor_fieldsAreDefaults()
  - Constructs `new Registration()` and asserts all string fields are null and timestamp 0 — simulates Firestore default constructor behavior.

- testSetAndGetRegistrationId(), testSetAndGetStudentId(), testSetAndGetEventId()
  - Basic setter/getter tests.

- testSetAndGetStatus_confirmed(), testSetAndGetStatus_waitlisted(), testSetAndGetStatus_checkedIn(), testSetAndGetStatus_cancelled()
  - Mutate `status` to different strings and assert correctness. Demonstrates the model allows free-form status strings.

- testSetAndGetTimestamp()
  - Sets a new timestamp and asserts correctness.

- testRealisticRSVP_careerFair(), testRealisticRSVP_waitlisted()
  - Create realistic `Registration` objects used in prototype flows, assert fields, and check timestamp positivity in the runtime-created case.

- testStatusTransition_waitlistedToConfirmed()
  - Start with a `Registration` in `Waitlisted` status, call `setStatus("Confirmed")` to simulate promotion, and assert the new status.
  - Demonstrates application-level status mutation; there is no built-in validation of allowed transitions.

- testStatusTransition_confirmedToCheckedIn()
  - Sets registration status to `Checked-in` and verifies it — simulates on-site check-in flow.

- testFirestoreMapping_defaultConstructorThenSetFields()
  - Simulates Firestore deserialization by creating `new Registration()` and setting fields with setters, then asserting values — a common pattern used by Firestore's POJO mapping.

Notes & suggestions for RegistrationTest
- Tests cover raw status mutation but do not validate allowed status transitions or invalid states. Consider adding an enum-based approach and tests for illegal transitions, or at least tests that demonstrate how the app should behave for unknown statuses.

---

## 5) ModelIntegrationTest.java
Path: `app/src/test/java/com/example/debugz/ModelIntegrationTest.java`

Purpose
- Integration-style unit tests that exercise collaboration between `Event`, `Student`, `Organizer`, and `Registration` in-memory.
- These tests simulate end-to-end flows: event creation by organizer, student RSVP, capacity changes, waitlist promotion, duplicate prevention, and cancellation.

Note: This file was previously documented in detail in `Docs/Models-and-Tests.md`. The tests here are higher-level than the single-model unit tests and are useful to validate interaction patterns.

Summary of tests and intent
- `testOrganizerCreatesEvent_andTracksList()` — organizer bookkeeping and event ownership.
- `testStudentRSVP_createsRegistrationAndUpdatesEventAndStudent()` — three-way update (Registration, Event, Student) in-memory.
- `testSpotsRemaining_decreasesWithEachRSVP()` — capacity accounting.
- `testWaitlistFlow_eventAtCapacityThenSpotOpens()` — simulate waitlist promotion (status change + attendee addition).
- `testStudentTracksMultipleRSVPs()` — many-to-many behavior between students and events.
- `testOrganizerEditsEvent_updatesDetails()` — editing event details via setters.
- `testOrganizerViewsAttendeeList()` — retrieving attendee lists.
- `testCapacityLimit_canBeUpdatedByOrganizer()` — capacity updates.
- `testDuplicateRSVP_doesNotDuplicateAttendee()` — duplicate prevention.
- `testRemoveNonExistentAttendee_doesNotAffectList()` — safe removal.
- `testStudentCancelsRSVP_bothListsUpdated()` — cancellation reflecting in both event and student lists.

These tests assume simple in-memory operations and intentionally do not involve persistence.

---

## 6) ExampleUnitTest.java
Path: `app/src/test/java/com/example/debugz/ExampleUnitTest.java`

Purpose
- The standard template smoke test generated by the Android project. It verifies the JUnit harness is working.

Test(s)
- addition_isCorrect() — asserts 2 + 2 == 4. Does not test app logic.

---

## Global observations about the test suite
- Coverage focus: the tests thoroughly verify model classes' public APIs and in-memory interactions. They are well organized by model.
- Firestore interaction: tests intentionally avoid network/persistence and instead simulate Firestore mapping by using default constructors followed by setter calls in several tests (e.g., `testFirestoreMapping_defaultConstructorThenSetFields` in `EventTest` and `RegistrationTest`). This is the recommended pattern to assert POJO mapping but does not replace real integration tests.
- Merge markers: a number of files (EventTest, MainActivity, EventDetailActivity, etc.) contain Git merge conflict markers (e.g., `<<<<<<< HEAD`, `>>>>>>> pr-15`). This indicates the repository is in a conflicted state and should be resolved. Tests may contain multiple variants — ensure you pick the intended variant and clean the file before running tests.
- Missing tests: there are no tests for controller behavior (`EventController`), Firestore transactions, or UI adapters. The tests also do not assert negative inputs or invalid-state handling. Consider adding:
  - Tests for `EventController.fetchAllEvents()` using a local mock of Firestore (or use Robolectric/Mockito to simulate db responses).
  - Tests for `Event` behavior when nulls are passed into setters or when negative capacities are used.
  - Tests for concurrency-related behavior (e.g., multiple promotions from waitlist) — though such tests are harder to reason about without integration infrastructure.

---

If you want, I can:
- Generate the cleaned, conflict-free versions of the files (resolving merge markers) and run the unit tests locally (if you want me to attempt compiling and running tests in this environment).
- Add extra tests for missing edge cases (negative capacity, invalid statuses, race conditions) under `app/src/test/java/com/example/debugz/`.
- Create short README entries describing how to run the unit tests locally using Gradle on Windows PowerShell.

Which follow-up would you like me to do?
