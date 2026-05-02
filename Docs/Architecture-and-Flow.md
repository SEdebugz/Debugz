# Architecture & Runtime Flow — Models, Controller, Views

This document explains how the pieces of the prototype app fit together at runtime. It maps the domain models to controller responsibilities and to view (Activity) behavior, and walks through the primary user flows (Discover → Details → RSVP and My Events). It also documents data-shape expectations in Firestore, where the app uses transactions, and important prototype caveats.

Files and components referenced
- Models: `app/src/main/java/com/example/debugz/models/{Event,Student,Organizer,Registration}.java`
- Controller: `app/src/main/java/com/example/debugz/controller/EventController.java`
- Views / Activities: `app/src/main/java/com/example/debugz/view/{MainActivity,EventDetailActivity,MyEventsActivity}.java`
- Tests: `app/src/test/java/com/example/debugz/*.java` (model unit tests and `ModelIntegrationTest.java`)

High-level architecture (layers)
- Model layer: plain Java POJOs (Event, Student, Organizer, Registration). These hold data, provide getters/setters, and small helper methods for list manipulation. They are Firestore-friendly (public no-arg constructors and simple field types).
- Controller layer: `EventController` encapsulates Firestore reads/writes for events and provides a small search helper. It also contains prototype demo-seeding logic.
- View layer: Android Activities and Adapters (MainActivity, EventDetailActivity, MyEventsActivity, EventAdapter) provide UI, listen for user actions, and call controller methods or execute Firestore operations directly.
- Persistence: Cloud Firestore is the app's backing store. Models are serialized/deserialized by Firestore using POJO mapping.

Firestore collections and document shape
- `events` collection — documents map to `Event` POJOs. Example fields (from `Event`):
  - `eventId` (string) — usually redundant with the document ID (the code sets eventId to doc.getId()).
  - `title`, `description`, `location`, `date`, `time` (strings)
  - `ticketPrice` (double) and/or `price` (string)
  - `organizerId` (string)
  - `maxCapacity` (int)
  - `attendeeIds` (array of strings) — stores student IDs for attendees

- `registrations` collection — documents map to `Registration` POJOs. Example fields:
  - `registrationId` (string) — sometimes computed (e.g., eventId_demoStudent)
  - `studentId` (string)
  - `eventId` (string)
  - `status` (string) — e.g., "Confirmed", "Waitlisted", "Checked-in"
  - `timestamp` (long)

- There is no dedicated `students` or `organizers` collection usage in the view/controller code shown; students/organizers exist as models and are used in tests. The prototype uses a hardcoded demo student id in views.

Primary runtime flows (step-by-step)

1) Discover events (MainActivity)
- Start: `MainActivity.onCreate()` creates an `EventController` and initializes `RecyclerView` + adapter.
- Loading events (two variants present in the repo):
  - Via `EventController.fetchAllEvents(...)`: the controller calls `db.collection("events").get()`; on success it maps documents to `Event` POJOs and returns the list via callback. If the collection is empty, `onDatabaseEmpty()` is invoked so the caller can seed demo data and retry loading.
  - Direct Firestore approach (alternate code in `MainActivity`): `db.collection("events").get()` is performed inline; if empty, it calls `seedDemoData()` which writes a few demo `Event` documents and then re-fetches.
- Filtering: `MainActivity` calls `eventController.searchEvents(query, allEvents)` to filter events by title/description.
- User selects an event card → adapter invokes click listener that starts `EventDetailActivity`, passing event fields via `Intent` extras.

2) View event details (EventDetailActivity)
- `EventDetailActivity` receives event metadata via Intent extras and binds them to UI views.
- It also calls `refreshEventData()` (or relies on controller) to fetch the live event document from Firestore and show current capacity / attendee counts.

3) RSVP flow (EventDetailActivity)
- Two prototype implementations exist in the codebase:
  - Simple non-transactional flow (older variant):
    1. Create a `Registration` object for a hardcoded student id (e.g., `demo_student_123`) and write it to `registrations` as a new document.
    2. Update the event document's `attendeeIds` array using `FieldValue.arrayUnion(studentId)` to add the student id.
    3. Notify user and return.
    - This approach is straightforward but not atomic: a failure between the registration write and the event update can leave the system in an inconsistent state.

  - Transactional flow (recommended / present in newer variant):
    1. `EventDetailActivity` runs a Firestore transaction via `db.runTransaction(...)`.
    2. Within the transaction: read event document snapshot, check preconditions (student not already in attendeeIds, attendeeIds.size() < maxCapacity), add student ID to event's attendeeIds in-memory and call `transaction.update(eventRef, "attendeeIds", event.getAttendeeIds())`, then `transaction.set()` a `Registration` document.
    3. Commit the transaction. On success show success message and refresh UI. On failure show error.
    - This ensures atomicity for the two updates (event and registration) and enforces capacity and duplicate-checking server-side in a single transaction.

4) My Events screen (MyEventsActivity)
- Shows events the demo student has RSVP'd to.
- Steps:
  1. Query `registrations` where `studentId == demo_student_123`.
  2. Build a list of `eventId`s found in registration documents.
  3. Query `events` where `eventId in eventIds` (the code attempts a `whereIn` query) and map documents to `Event` objects.
  4. Display the events in a RecyclerView using `EventAdapter`.

Controller responsibilities (EventController)
- `fetchAllEvents(OnEventsFetchedListener)`: query Firestore, map documents into `Event` objects, and surface results via the callback.
- `seedDemoData(Runnable)`: write a few demo `Event` objects to Firestore for prototype/demo convenience. `EventController` contains demo events and writes them directly to Firestore.
- `searchEvents(String, List<Event>)`: a small client-side filter used by the main discovery UI to filter the loaded events list.

Model responsibilities
- Models are intentionally simple POJOs with the following responsibilities:
  - Provide a Firestore-friendly representation (no-arg constructor and field getters/setters).
  - Offer convenience helpers for list manipulation (`addAttendee`, `removeAttendee`, `addRegistration`, `addCreatedEvent`, etc.).
  - Do not enforce cross-entity invariants (e.g., capacity enforcement is in transaction code, not model methods).

Data consistency and transactional concerns
- The codebase includes both a non-transactional and a transactional RSVP flow. The transactional flow (using `runTransaction`) ensures that adding an attendee and writing the corresponding `Registration` document happen atomically, preventing partial updates and race conditions.
- The non-transactional flow is susceptible to partial failure (e.g., registration write succeeds but `attendeeIds` update fails). Use the transactional implementation for correctness.

Prototype caveats and TODOs
- Hardcoded demo student ID: Many view flows use a hardcoded `demo_student_123` as the current user. Replace with proper authentication & user context (Firebase Auth) for production.
- Merge conflicts in source: Several files contain Git conflict markers (e.g., `<<<<<<< HEAD`, `>>>>>>> pr-15`). These must be resolved to produce a clean, single variant of the code.
- `Event` constructor mismatch: Tests and some controller code construct `Event` with a numeric `ticketPrice` (double) as the last constructor parameter, whereas the committed `Event` model in this branch has a constructor that takes a `String price` as the last parameter. Either add an overloaded constructor that accepts `double ticketPrice` or update callers to use `setTicketPrice`/`setPrice` after construction.
- Status values are raw strings: `Registration.status` is stored as `String`. Consider an enum (e.g., `RegistrationStatus`) to centralize allowed values and transitions.
- Collections vs Sets: The models use `List<String>` with manual duplicate checks. For faster membership checks and clearer uniqueness semantics consider `Set<String>` (e.g., `LinkedHashSet`) where order matters.
- Defensive copies: `getAttendeeIds()` returns the internal `List` reference. If you want to prevent accidental external mutation, return an unmodifiable copy or a defensive copy.

Example sequence diagram (textual)
- Discover & RSVP sequence (simplified):
  1. `MainActivity` → `EventController.fetchAllEvents()` → Firestore `events` collection → returns list of `Event` → `MainActivity` updates UI.
  2. User taps an event → `MainActivity` starts `EventDetailActivity` with event extras.
  3. `EventDetailActivity` (on RSVP button):
     - Option A (transaction): `db.runTransaction`: read event -> verify capacity -> update `attendeeIds` -> set `Registration` doc -> commit. UI refresh.
     - Option B (non-transaction): write `Registration` doc -> update `events` document with `FieldValue.arrayUnion(studentId)` -> UI refresh. (Less safe.)
  4. `MyEventsActivity` queries `registrations` for `studentId` -> builds eventId list -> queries `events` for matching event docs -> UI list built.

How tests map to architecture
- Model tests (`EventTest`, `StudentTest`, `OrganizerTest`, `RegistrationTest`) validate the POJO behavior and local invariants.
- `ModelIntegrationTest` validates cross-model flows in-memory (RSVP bookkeeping, waitlist promotion simulation, capacity counting).
- Tests do not verify actual Firestore behavior — they simulate Firestore mapping via default-constructor + setters tests when necessary. Integration tests against Firestore would require emulator or instrumentation tests.

Recommended next steps to harden the prototype
- Resolve merge conflicts in code and keep the transactional RSVP implementation as the canonical flow.
- Replace `demo_student_123` with proper authentication and a `UserSession` or `AuthManager` abstraction.
- Add an `enum RegistrationStatus` and validate status transitions at service/controller level; add tests enforcing allowed transitions.
- Consider migrating `attendeeIds` and similar lists to `Set<String>` for performance and semantics.
- Add integration tests against the Firestore emulator to validate transactions and seeding logic.

If you want, I can:
- Add the missing overloaded `Event` constructor accepting `double ticketPrice` and run tests.
- Resolve merge conflicts in `MainActivity` and `EventDetailActivity` producing a clean variant that uses the transactional RSVP flow.
- Add a `RegistrationStatus` enum and update tests accordingly.

Which of the suggested follow-ups would you like me to implement next?
