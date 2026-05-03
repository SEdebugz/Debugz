# Models and Tests — In-depth Explanation

This document explains, in detail, the model classes and the integration tests contained in this repository. It covers every field, constructor, and method in the core model classes (`Event`, `Student`, `Organizer`, `Registration`) and walks through each test case in `ModelIntegrationTest.java` step-by-step, describing the intent, intermediate state, and assertions.

Files referenced
- `app/src/main/java/com/example/debugz/models/Event.java`
- `app/src/main/java/com/example/debugz/models/Student.java`
- `app/src/main/java/com/example/debugz/models/Organizer.java`
- `app/src/main/java/com/example/debugz/models/Registration.java`
- `app/src/test/java/com/example/debugz/ModelIntegrationTest.java`

High-level plan used to produce this doc
1. Read the integration test (`ModelIntegrationTest.java`) to identify important flows and model usage.
2. Read each model class source to document fields, constructors, methods, behaviors, invariants, and null-safety.
3. Explain each test method in the integration test with explicit step-by-step state changes and verification reasoning.
4. Call out noteworthy implementation decisions, limitations, and suggestions for hardening (validation, enum usage, etc.).

----------------

Overview of the application model layer

The app uses a small set of plain Java model classes representing the domain for an event-discovery and RSVP system. The models are intentionally simple, intended to be used both in-memory for tests and to be serialized/deserialized by Firebase Firestore (the code comments mention Firestore). The four classes are:

- `Event` — stores event metadata and attendee tracking (IDs only).
- `Student` — stores a student profile and a list of registration IDs.
- `Organizer` — stores organizer profile and created event IDs.
- `Registration` — represents a join/RSVP record linking a `Student` and an `Event`, including status and timestamp.

These classes emphasize minimal responsibilities: holding data, providing getters/setters, and small helper methods for list manipulation (add/remove). More advanced logic (permission checks, validation, Firestore integration) is purposefully left to higher layers.

----------------

Detailed class-by-class explanation

1) `Event` (`app/src/main/java/com/example/debugz/models/Event.java`)

Purpose
- Represents an event that students can discover and RSVP to.
- Contains both display data (title, description, date, time, location, price) and runtime lists for attendees.

Fields (all private)
- `String eventId` — unique identifier for the event. Used as a primary key when storing/retrieving in Firestore.
- `String title` — display title for the event.
- `String description` — descriptive text.
- `String location` — venue or location label.
- `String date` — date label (stored as a string in the prototype).
- `String time` — time label.
- `double ticketPrice` — numeric ticket price; `0.0` indicates free in usage comments.
- `String organizerId` — the `Organizer` that owns or created the event.
- `int maxCapacity` — configured maximum number of attendees.
- `String price` — a display-string price label (e.g., "Free", "5000 PKR"). Note: both `ticketPrice` and `price` coexist so the UI can either show a formatted label or use numeric price for calculations.
- `List<String> attendeeIds` — list of student IDs who have been added as attendees. The model stores only IDs to avoid embedding student objects.

Constructors
- `Event()` — no-arg constructor that initializes `attendeeIds` to an empty `ArrayList`. This constructor is explicitly present to support Firestore deserialization (Firestore requires a public no-argument constructor for POJOs).
- `Event(String eventId, String title, String description, String location, String date, String time, String organizerId, int maxCapacity, String price)` — primary constructor used by tests and likely by app code to create an event with core metadata. It sets `ticketPrice` to `0.0` by default and initializes `attendeeIds`.

Getters/Setters
- Standard getters and setters for every field (`getEventId`/`setEventId`, `getTitle`/`setTitle`, etc.). They perform no validation — they are simple property accessors.

Attendee list helpers and null-safety
- `getAttendeeIds()` — returns `attendeeIds` if it's not null; otherwise returns a new empty `ArrayList`. This is a defensive null-safe getter so callers will not receive null references.
- `setAttendeeIds(List<String> attendeeIds)` — assigns the provided list or, if null, an empty `ArrayList`. This ensures the internal store does not remain null.
- `addAttendee(String studentId)` — ensures `attendeeIds` is non-null (creates a new `ArrayList` if necessary) and then adds `studentId` only if it is not already present (prevents duplicates).
- `removeAttendee(String studentId)` — removes `studentId` from `attendeeIds` if the list is non-null; no exception if not present.

Behavioral notes
- Duplicate prevention: the implementation of `addAttendee` uses `List.contains` to avoid adding the same ID twice. This means the attendee list preserves uniqueness at the level of string IDs, although it remains a `List` and not a `Set`.
- No capacity enforcement: `addAttendee` does not check `maxCapacity`. Higher-level code (controllers or services) must enforce capacity and waitlisting logic.
- Price: the class keeps both `ticketPrice` (numeric) and `price` (display label). Tests sometimes construct `Event` with a numeric price (see `ModelIntegrationTest` — note there is a constructor variant used with `1500.0` in one test; the code supports a numeric ticket price field but the primary constructor in the file above uses `String price` as the last parameter. Watch for small API inconsistencies in usage — further below we’ll explain how the test constructs an `Event` with a double last parameter: that indicates there is an overloaded or alternative constructor in the repository variant the test expects, but in the committed `Event.java` here, the primary constructor's last parameter is `String price`. In this code base the `Event` class eventually exposes `ticketPrice` via setter and getter.)

2) `Student` (`app/src/main/java/com/example/debugz/models/Student.java`)

Purpose
- Represents a student profile used for discovery and RSVP flows. Tracks the student's registrations via `registrationIds`.

Fields
- `String studentId` — unique id for student (in tests, the university roll number is used as the ID).
- `String name` — full name.
- `String email` — contact email.
- `String school` — school/department label.
- `String year` — academic year label.
- `List<String> preferences` — tags or preferences for event personalization.
- `List<String> registrationIds` — list of registration IDs referencing `Registration` objects.

Constructors
- `Student()` — no-arg sets `preferences` and `registrationIds` to new empty `ArrayList`s for safe deserialization.
- `Student(String studentId, String name, String email, String school, String year)` — convenience constructor that sets core profile fields and initializes lists.

Getters/Setters
- Standard getters/setters for fields. No validation (e.g., email format) is performed.

Helpers
- `addPreference(String tag)` — adds a preference tag only if it's not already present.
- `addRegistration(String registrationId)` — adds a registration ID only if not present.
- `removeRegistration(String registrationId)` — removes a registration ID from `registrationIds`. If the registrationId is not present, `List.remove` is a no-op.

Behavioral notes
- The model keeps only registration IDs rather than embedding `Registration` objects — this keeps models small and is friendly to Firestore references.
- No validation of academic year or school — callers must guarantee data consistency.

3) `Organizer` (`app/src/main/java/com/example/debugz/models/Organizer.java`)

Purpose
- Represents an organizing entity that creates events. Tracks event IDs that the organizer created.

Fields
- `String organizerId` — unique identifier.
- `String name` — display name for the organizer.
- `String email` — contact email.
- `List<String> createdEventIds` — list of event IDs the organizer created or manages.

Constructors
- `Organizer()` — no-arg constructor initializes `createdEventIds` to an empty `ArrayList`.
- `Organizer(String organizerId, String name, String email)` — convenience constructor for tests and app code.

Getters/Setters
- Standard getters/setters. No validation on email or event ids.

Helpers
- `addCreatedEvent(String eventId)` — adds event id if not already present.
- `removeCreatedEvent(String eventId)` — removes event id.

Behavioral notes
- Like other models, it leaves permission checks to higher layers. The class is a simple state holder that supports de/serialization.

4) `Registration` (`app/src/main/java/com/example/debugz/models/Registration.java`)

Purpose
- Join object that links a `Student` to an `Event`. Stores workflow status and timestamp of creation.

Fields
- `String registrationId` — unique registration ID.
- `String studentId` — id of the student.
- `String eventId` — id of the event.
- `String status` — state label like "Confirmed", "Waitlisted". This is stored as a raw string (no enum currently used).
- `long timestamp` — epoch milliseconds when the registration was made.

Constructors
- `Registration()` — no-arg constructor for Firestore.
- `Registration(String registrationId, String studentId, String eventId, String status, long timestamp)` — convenience constructor used by tests.

Getters/Setters
- Standard getters and setters. `status` is a `String` and is freely settable via `setStatus`.

Behavioral notes
- The code comments call out that storing `status` as raw `String` allows unsupported states to be persisted; callers should validate in higher layers. This is a common trade-off in simple prototypes.

----------------

In-depth: `ModelIntegrationTest.java` — step-by-step analysis

The test file `app/src/test/java/com/example/debugz/ModelIntegrationTest.java` contains a set of unit-tests meant to exercise how the model objects collaborate in-memory. These are not integration tests talking to Firestore; they validate in-memory state transitions. For each test below we describe the initial setup, key actions, expected state changes, and why each assertion is valid.

Shared test setup (`@Before setUp()`)
- An `Organizer` is created with ID `org_cso` and name "LUMS Career Services Office".
- An `Event` named `careerFair` is created with event ID `event_cf_2026`, organizer id `org_cso`, and `maxCapacity` = 3. The constructor in the test supplies `0.0` for ticket price (note: the `Event` class in this repo actually expects the last constructor parameter to be a `String price` per the source; the test passes `0.0` which in Java would require a matching constructor. In the code provided to you there is only the `String price` constructor; however the `Event` class also has a `ticketPrice` field with getters/setters. The important takeaway is that the test intent is to create an event with numeric price 0.0 and max capacity 3). In the repository we are working with, the `Event` object still supports `ticketPrice` and `price` fields.
- Three `Student` objects are created: `faneez`, `ahmed`, and `moeez` with their student IDs and simple profile fields.

Each test method

1. `testOrganizerCreatesEvent_andTracksList()`
- Action: `organizer.addCreatedEvent(careerFair.getEventId());`
- This appends the id `"event_cf_2026"` to `organizer.createdEventIds` if not already present.
- Assertions:
  - `assertTrue(organizer.getCreatedEventIds().contains("event_cf_2026"));` — verifies the event id was added.
  - `assertEquals(1, organizer.getCreatedEventIds().size());` — verifies that only one entry exists (guarding against duplicates).
  - `assertEquals("org_cso", careerFair.getOrganizerId());` — verifies the event's stored organizer id matches the organizer that created it.
- Why this matters: ensures both organizer bookkeeping and event metadata align.

2. `testStudentRSVP_createsRegistrationAndUpdatesEventAndStudent()`
- Purpose/intent: model the creation of a `Registration` object (the RSVP record) and then ensure the `Event` and `Student` models are updated consistently.
- Steps:
  1. Create a `Registration` instance `rsvp` with id `reg_cf_faneez`, student ID `faneez.getStudentId()` ("27100247"), event id `careerFair.getEventId()` ("event_cf_2026"), status "Confirmed", and timestamp `System.currentTimeMillis()`.
  2. Call `careerFair.addAttendee(faneez.getStudentId());` — adds student id into `careerFair.attendeeIds` unless already present.
  3. Call `faneez.addRegistration(regId);` — adds `reg_cf_faneez` to `faneez.registrationIds`.
- Assertions:
  - `assertEquals("Confirmed", rsvp.getStatus());` — checks the registration object's status as created.
  - `assertTrue(careerFair.getAttendeeIds().contains("27100247"));` — ensures the event knows the student is attending.
  - `assertTrue(faneez.getRegistrationIds().contains("reg_cf_faneez"));` — ensures the student tracks the registration reference.
  - `assertEquals(1, careerFair.getAttendeeIds().size());` — ensures no duplicate attendees were created.
- Comments: This test demonstrates the three-way collaboration (Registration, Event, Student) but does not assert global consistency (e.g., that `Registration.eventId` equals `careerFair.eventId`) because that is already defined at construction time.

3. `testSpotsRemaining_decreasesWithEachRSVP()`
- Intent: verify that remaining spots calculated as `maxCapacity - attendeeIds.size()` reduces as attendees are added.
- Steps and checks:
  - Initially, `careerFair.getMaxCapacity()` is 3 and `careerFair.getAttendeeIds().size()` is 0, so assert 3.
  - Add `faneez` → assert remaining 2.
  - Add `ahmed` → assert remaining 1.
  - Add `moeez` → assert remaining 0.
- Important note: there is no enforcement in `addAttendee` preventing addition beyond capacity. This test adds exactly up to capacity; if higher-level logic should prevent overbooking it must be implemented elsewhere.

4. `testWaitlistFlow_eventAtCapacityThenSpotOpens()`
- Purpose: model a waitlist scenario using only models — the test shows how an event can be full, then an attendee cancels, and a waitlisted registration is promoted.
- Steps:
  1. Add three attendees (`faneez`, `ahmed`, `moeez`) — event reaches capacity (3/3).
  2. Create a new `Student` `huzayfah` and create a `Registration` `waitlistReg` for her with status "Waitlisted".
  3. Assert that the `waitlistReg` status is "Waitlisted".
  4. Remove `ahmed` from `careerFair` (`careerFair.removeAttendee(ahmed.getStudentId());`) and remove the (hypothetical) registration `reg_cf_ahmed` from `ahmed`'s registration list (`ahmed.removeRegistration("reg_cf_ahmed")`). Note: the code removes the registration id string; `ahmed`'s registration list had not contained `reg_cf_ahmed` previously in setup, so `removeRegistration` is a no-op if the id doesn't exist. The main point is that `careerFair`'s `attendeeIds` list shrinks.
  5. Assert that the remaining spots are 1.
  6. Update `waitlistReg.setStatus("Confirmed");`, add `huzayfah` to attendees via `careerFair.addAttendee(huzayfah.getStudentId());`, and add the registration id to `huzayfah`.
- Assertions:
  - `assertEquals("Confirmed", waitlistReg.getStatus());` ensures the registration object's status was updated.
  - `assertTrue(careerFair.getAttendeeIds().contains("27100271"));` verifies the event now includes the promoted student.
  - `assertEquals(0, careerFair.getMaxCapacity() - careerFair.getAttendeeIds().size());` verifies capacity is back to full.
- Notes: This test simulates waitlisting entirely in-memory; it highlights that promotion from waitlist to confirmed is a multi-step process that the model supports (status mutation and adding an attendee) but doesn't automate (no atomic promote method).

5. `testStudentTracksMultipleRSVPs()`
- Intent: confirm that a student can maintain multiple registration references across different events.
- Steps:
  1. Create a new `Event` `lumun` (LUMUN 2026) with `maxCapacity` 650 and (in the test code) a numeric price `1500.0`. The test then adds two registration ids to `faneez` using `addRegistration`.
  2. Add `faneez` as an attendee in both `careerFair` and `lumun` via `addAttendee`.
- Assertions:
  - `assertEquals(2, faneez.getRegistrationIds().size());` — verifies both registrations are recorded.
  - `assertTrue(careerFair.getAttendeeIds().contains(faneez.getStudentId()));` — verifies `careerFair` lists the student.
  - `assertTrue(lumun.getAttendeeIds().contains(faneez.getStudentId()));` — verifies `lumun` lists the student.
- Note: This test demonstrates many-to-many behavior via the join object approach: students have lists of registration IDs and events keep lists of attendee IDs.

6. `testOrganizerEditsEvent_updatesDetails()`
- Intent: verify that event fields have working setters and that changes persist in-memory.
- Steps: call setters on `careerFair` to modify title, location, date, time, and max capacity.
- Assertions: check each field via getters to ensure changes applied.
- Note: No history or audit trail exists in `Event` — setters override previous values.

7. `testOrganizerViewsAttendeeList()`
- Intent: verify that the `getAttendeeIds` function returns the attendee list and that it reflects the added attendees.
- Steps: add three attendees and capture the returned `List<String> attendees = careerFair.getAttendeeIds();` then assert size and inclusion of IDs.
- Assertions: size == 3 and the individual IDs are present.
- Note: The returned `List` is the internal list reference — callers modifying the returned list will modify the `Event`’s internal state. The class does not defensively copy on `getAttendeeIds()`.

8. `testCapacityLimit_canBeUpdatedByOrganizer()`
- Intent: verify `setMaxCapacity` works and `getMaxCapacity` reflects updates.
- Steps: assert initial capacity (3), call `careerFair.setMaxCapacity(500)`, then assert capacity is 500.
- Note: There is no automatic rebalancing of attendee lists when capacity is reduced; reducing `maxCapacity` does not remove attendees.

9. `testDuplicateRSVP_doesNotDuplicateAttendee()`
- Purpose: confirm `addAttendee` prevents duplicates.
- Steps: call `careerFair.addAttendee(faneez.getStudentId())` twice.
- Assertion: `careerFair.getAttendeeIds().size()` equals 1.

10. `testRemoveNonExistentAttendee_doesNotAffectList()`
- Purpose: ensure removing an ID that doesn't exist is safe.
- Steps: add `faneez`, call `careerFair.removeAttendee("nonexistent_student_id")`.
- Assertion: size remains 1.

11. `testStudentCancelsRSVP_bothListsUpdated()`
- Intent: simulate cancellation where both the event and the student remove references.
- Steps:
  1. Set `regId = "reg_cf_faneez"`, add `faneez` to `careerFair` and add registration id to `faneez`.
  2. Call `careerFair.removeAttendee(faneez.getStudentId());` and `faneez.removeRegistration(regId);`.
- Assertions:
  - `assertFalse(careerFair.getAttendeeIds().contains(faneez.getStudentId()));` ensures the event no longer lists the student.
  - `assertFalse(faneez.getRegistrationIds().contains(regId));` ensures the student's registration list no longer contains the id.
  - `assertEquals(0, careerFair.getAttendeeIds().size());` and `assertEquals(0, faneez.getRegistrationIds().size());` ensure both lists are empty.
- Note: Since no cross-linked registration store exists, this is a local consistency exercise: higher-level code (controllers or transactional datastore logic) must persist and enforce atomicity in the real app.

----------------

Cross-cutting observations and important design notes

1. Defensive null handling
- Most model classes initialize list fields in no-arg constructors and guard against null in setters and getters (e.g., `Event.getAttendeeIds` returns an empty `ArrayList` if `attendeeIds` is null). This reduces the chance of NPEs when models are constructed by Firebase deserialization.

2. Use of raw strings for statuses and IDs
- `Registration.status` is a `String` (e.g., "Confirmed", "Waitlisted"). For robustness consider switching to an enum type (e.g., `RegistrationStatus`) or at minimum centralizing allowed values as constants to avoid typos and unsupported states in the datastore.

3. Collections vs Sets
- Uniqueness is enforced by checking `List.contains` before adding. This works but has O(n) complexity on insert and leaves the semantics as an ordered `List`. If uniqueness and faster membership tests are important, consider using `Set<String>` (e.g., `LinkedHashSet` to preserve insertion order) for attendee and registration id collections.

4. Capacity and business rules in models vs services
- The `Event` model does not enforce capacity or waitlist promotion. This keeps models simple but pushes business rules to higher layers. This is fine; it also means tests must explicitly perform promotion or cancellation steps.

5. Leaked internal list references
- `getAttendeeIds()` returns the actual internal `List` object. Mutating the returned list will mutate the model. If this is undesired, return an unmodifiable copy or make defensive copies.

6. Consistency across models
- There is no single transactional operation that simultaneously updates the `Registration`, `Event.attendeeIds`, and `Student.registrationIds`. In a multi-user app backed by Firestore this should be done using transactions or cloud functions to ensure consistency.

7. Constructor API mismatch (notes from code)
- The test constructs some `Event` objects with `double` ticket price as the last parameter, while `Event.java` in this repository shows a constructor whose last parameter is a `String price`. Depending on the compiled code in the repository versus test sources, Java will require corresponding constructors to match. For documentation purposes, the model exposes both `ticketPrice` and `price` fields and supports getters/setters for both. If you face compile issues, add an overloaded constructor to `Event` taking a `double ticketPrice` as the last argument and assign it to the `ticketPrice` field (and optionally set `price` to a formatted label).

Suggested fixes / improvements
- Introduce a `RegistrationStatus` enum to replace raw status strings and validate allowed transitions.
- Consider replacing `List<String>` collections with `Set<String>` (or `LinkedHashSet`) for uniqueness and faster membership checks. If order matters for UI, use `LinkedHashSet`.
- Add capacity enforcement helpers in a service layer: `boolean tryReserveSeat(Event event, Student student, Registration reg)` that atomically checks capacity and then updates `event.attendeeIds` and `student.registrationIds` within a transaction or with proper error handling.
- Defensive copies for getters returning collections (or return unmodifiable views) to prevent accidental external mutation.
- Add unit tests for edge cases: adding after capacity is full, reducing capacity below current attendees, concurrent promotions from waitlist, invalid status transitions.

Glossary of behaviors demonstrated by tests
- RSVP creation: a `Registration` object represents intent; adding to `Event.attendeeIds` and `Student.registrationIds` are separate actions.
- Capacity accounting: remaining spots are computed by `maxCapacity - attendeeIds.size()`.
- Waitlist promotion: represented by changing `Registration.status` and adding attendee+registration to event and student, respectively.
- Duplicate-prevention: `Event.addAttendee` and `Student.addRegistration` both check `contains` to avoid duplicate entries.

File map and where to look next
- Core models: `app/src/main/java/com/example/debugz/models/*.java` — documented above.
- Integration tests: `app/src/test/java/com/example/debugz/ModelIntegrationTest.java` — documented in-depth above. Other unit tests (if present) should be read to document edge conditions further.
- Docs and diagrams: `Docs/` contains UML and screenshots which can help map flows to UI screens.

Closing notes
- The models form a minimal, clear domain representation suitable for a small event/RVSP prototype. They purposefully avoid embedded complexity so that controllers or Firestore integration layers can implement business rules and persistence semantics.
- The `ModelIntegrationTest` suite demonstrates the intended in-memory flows and surface-level invariants but does not cover transactional or multi-layer consistency concerns — these are important next steps for a production-ready system.

If you'd like, I can:
- Convert this document into Javadoc-style reference pages per class.
- Add suggested small code fixes (e.g., add a `RegistrationStatus` enum, add an overloaded `Event` constructor that accepts `double ticketPrice`, or replace lists with sets) and run the test suite locally.
- Generate sequence diagrams (textual or rendered) for each test flow.

Which follow-up would you like me to do next?
