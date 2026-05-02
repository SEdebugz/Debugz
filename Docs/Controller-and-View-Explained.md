# Controller and View Files — Detailed Explanation

This document explains every file in the `controller` and `view` packages in detail. For each file I explain purpose, important fields, lifecycle methods, Firestore interactions, UI wiring, callbacks, and notable implementation details or caveats.

Files covered
- `app/src/main/java/com/example/debugz/controller/EventController.java`
- `app/src/main/java/com/example/debugz/view/MainActivity.java`
- `app/src/main/java/com/example/debugz/view/EventDetailActivity.java`
- `app/src/main/java/com/example/debugz/view/MyEventsActivity.java`
- `app/src/main/java/com/example/debugz/view/LandingActivity.java`
- `app/src/main/java/com/example/debugz/view/EventAdapter.java`

Read this file to understand how the UI and Firestore integrations are wired in the prototype and which responsibilities belong to the controller vs the view layer.

---

EventController.java
Path: `app/src/main/java/com/example/debugz/controller/EventController.java`

Purpose
- Thin mediator between Firestore and view layer for event-related operations.
- Centralizes the demo seeding logic and the search/filter helper used by `MainActivity`.

Important imports and dependencies
- `com.google.firebase.firestore.FirebaseFirestore` — controller holds a `FirebaseFirestore` instance to query the `events` collection.
- `com.example.debugz.models.Event` — maps Firestore documents to `Event` POJOs.

Key fields
- `private FirebaseFirestore db;` — shared Firestore instance acquired in the constructor.

Constructor
- `public EventController()` — sets `db = FirebaseFirestore.getInstance();`.

Public API
- `public interface OnEventsFetchedListener` — callback contract exposing three callbacks: `onSuccess(List<Event>)`, `onFailure(Exception)`, and `onDatabaseEmpty()`. Note: the interface exposes multiple mutually-exclusive callbacks rather than a single result wrapper; callers must coordinate them.

- `public void fetchAllEvents(OnEventsFetchedListener listener)`
  - Calls `db.collection("events").get()` asynchronously.
  - On success:
    - If the returned `queryDocumentSnapshots` is empty → calls `listener.onDatabaseEmpty()`.
    - Otherwise maps each document to an `Event` via `doc.toObject(Event.class)`, sets the `eventId` to `doc.getId()`, collects the list and calls `listener.onSuccess(events)`.
  - On failure calls `listener.onFailure(exception)`.
  - Behavior notes: the method does client-side mapping and returns in-memory `Event` objects — no streaming/real-time snapshot listener is used here.

- `public void seedDemoData(Runnable onSuccess)`
  - Creates three demo `Event` instances and writes them to `db.collection("events").document(e.getEventId()).set(e)`.
  - Calls `onSuccess.run()` after the final write succeeds (last `.addOnSuccessListener`).
  - Purpose: once the `events` collection is empty the view can call `seedDemoData()` to populate demo content.

- `public List<Event> searchEvents(String query, List<Event> allEvents)`
  - Simple client-side filter: lowercases query and compares containment in `event.getTitle()` and `event.getDescription()`.
  - Returns a new filtered `List<Event>`.
  - Limitations: no normalization beyond lowercasing and only checks title/description. Performance depends on `allEvents` size.

Notes & suggestions
- Controller currently mixes data access and demo seeding, which is convenient for prototypes but should be separated for production.
- `OnEventsFetchedListener` with multiple callbacks is workable but can be simplified with a `Result` object or single callback with a status enum.

---

MainActivity.java
Path: `app/src/main/java/com/example/debugz/view/MainActivity.java`

Purpose
- Hosts the main event discovery feed where users browse and search events.
- Wires RecyclerView with `EventAdapter`, search box, and loads events through either `EventController` or direct Firestore calls (the file contains two variants due to an unresolved merge conflict).

Important imports
- `EventController` — preferred controller-based fetch.
- `FirebaseFirestore`, `QueryDocumentSnapshot` — alternate direct Firestore fetch variant present in the file.
- `EventAdapter` and `Event` for UI binding.

Key fields
- `RecyclerView rvEvents`, `EventAdapter adapter`
- `List<Event> allEvents`, `List<Event> filteredEvents` used to store loaded and filtered events.
- `EditText etSearch` — for text filtering.
- `EventController eventController` — used in the controller-driven variant.

onCreate behavior
- Set layout via `setContentView(R.layout.activity_main)`.
- Instantiates `eventController = new EventController();` (controller-based variant).
- RecyclerView setup: `LinearLayoutManager` + `EventAdapter(filteredEvents, event -> { ... start EventDetailActivity ... })`.
- `setupSearch()` attaches a `TextWatcher` to `etSearch` to call `filter(s)` on text change.
- Finally calls `loadEvents()` or `fetchEvents()` depending on variant.

loadEvents() (controller-driven variant)
- Calls `eventController.fetchAllEvents(new EventController.OnEventsFetchedListener() { ... })`.
- onSuccess: clear `allEvents`, add fetched events, then call `filter("")` to show all.
- onFailure: shows a Toast message about internet or google-services.json.
- onDatabaseEmpty: calls `eventController.seedDemoData(() -> loadEvents());` so it seeds and reloads.

fetchEvents() (direct Firestore variant)
- Calls `db.collection("events").get()` directly.
- On success: if empty, call `seedDemoData()` (local method) else map docs to Event and `filter("")`.
- seedDemoData(): creates demo events and writes them using a `WriteBatch` and commits; on success calls `fetchEvents()`.

filter(String query)
- Uses `eventController.searchEvents(query, allEvents)` to build `filteredEvents` and calls `adapter.notifyDataSetChanged()`.

Click handling and Intents
- When an event card is clicked, an `Intent` is built with extras including `eventId`, `title`, `date`, `time`, `location`, `description`, and either `ticketPrice` or `price` depending on variant (merge conflict present).

Important implementation details & caveats
- There are unresolved Git merge markers in the file showing two different implementations (controller-driven vs direct Firestore). Both fetch events from Firestore but differ in where seeding lives and whether `ticketPrice` vs `price` is passed in Intent extras.
- The UI depends on either `eventController` or direct `db` operations. Pick one approach and resolve conflicts for clarity.
- Error handling is basic (toasts) and seeding is built into production code — consider moving seeding to a dev-only path.

---

EventDetailActivity.java
Path: `app/src/main/java/com/example/debugz/view/EventDetailActivity.java`

Purpose
- Shows full event details and handles the RSVP creation flow for a selected event.
- Contains two RSVP implementations: a non-transactional approach and a transactional approach using `db.runTransaction(...)` (file contains both variants due to merge markers).

Important imports
- `FirebaseFirestore`, `Transaction`, `FieldValue` (variants) — used for writing to `registrations` and updating `events`.
- `Registration` and `Event` models.

Key fields
- `FirebaseFirestore db` — initialized in `onCreate()` with `FirebaseFirestore.getInstance()`.
- Event metadata fields: `String eventId, title, date, time, location, description, price; int capacity; double ticketPrice;` — these are populated from `Intent` extras.

onCreate behavior
- Pulls extras from intent such as `eventId`, `title`, `date`, `time`, `location`, `description`, and either `ticketPrice` double or `price` String depending on variant.
- Calls `setupViews()` to bind data to UI and `refreshEventData()` to fetch the live event document and show updated capacity information.

setupViews()
- Binds TextViews (`tvTitle`, `tvDate`, `tvLocation`, `tvPrice`, `tvDescription`) and an RSVP `Button`.
- Formats price text using either `ticketPrice` (numeric) or `price` (display string); the file contains both variants and some duplicate setting of `tvPrice`.
- `btnRSVP.setOnClickListener(v -> handleRSVPWithTransaction());` in transactional variant, or `handleRSVP()` in non-transactional variant.

handleRSVP() — non-transactional flow (older variant)
- Uses a hardcoded student id `demo_student_123`.
- Creates a `Registration` instance with random `UUID` registrationId, studentId, eventId, status `Confirmed`, and timestamp.
- Writes registration to `db.collection("registrations").document(registrationId).set(registration)`.
- On success updates event document with `FieldValue.arrayUnion(studentId)` to append to `attendeeIds` array, then shows success toast and finishes activity.
- Failure handlers show toasts for RSVP failure or capacity update failure.
- Caveat: not atomic — potential inconsistency if registration write succeeds and attendee update fails (or vice versa).

handleRSVPWithTransaction() — transactional flow (recommended)
- Uses `db.runTransaction((Transaction.Function<Void>) transaction -> { ... })`.
- Inside transaction:
  - `eventRef = db.collection("events").document(eventId)` and `transaction.get(eventRef)` to read current event state.
  - Map snapshot to `Event` and validate: throw if event is null, if `event.getAttendeeIds().contains(studentId)` then throw, if `event.getAttendeeIds().size() >= event.getMaxCapacity()` then throw.
  - If checks pass: `event.addAttendee(studentId)` (local object) and `transaction.update(eventRef, "attendeeIds", event.getAttendeeIds())` to persist array.
  - Create a `Registration` object and `transaction.set(db.collection("registrations").document(registrationId), reg)`.
- Transaction commit: on success show success toast and call `refreshEventData()`; on failure show error toast.
- This approach is atomic and enforces capacity/duplicate prevention server-side.

refreshEventData()
- Fetches the live event document and updates UI capacities (attendee count / max capacity) — ensures detail view reflects the current server state after changes.

Important implementation details & caveats
- The activity uses a hardcoded demo student id `demo_student_123` — authentication/real user not integrated.
- Merge markers present: file contains both non-transactional and transactional flows. The transactional flow is superior and should be kept.
- Some duplicated or conflicting lines exist for price handling and view binding (likely from merge). Clean-up recommended.

---

MyEventsActivity.java
Path: `app/src/main/java/com/example/debugz/view/MyEventsActivity.java`

Purpose
- Displays events the demo student has RSVP'd to by resolving `registrations` back to `events` documents.

Important imports
- `FirebaseFirestore`, `QueryDocumentSnapshot`, `Event`, `Registration`.

Key fields
- `RecyclerView rvMyEvents`, `EventAdapter adapter`, `List<Event> myEventsList`, `FirebaseFirestore db`.

onCreate behavior
- Initialize `db = FirebaseFirestore.getInstance();` and RecyclerView + adapter (adapter created with a no-op click listener for optionally viewing details).
- Calls `fetchMyRegistrations()`.

fetchMyRegistrations()
- Uses a hardcoded `currentStudentId = "demo_student_123"` (must match EventDetailActivity for demo).
- Queries `db.collection("registrations").whereEqualTo("studentId", currentStudentId).get()`.
- On success iterates snapshots, maps each to `Registration` via `doc.toObject(Registration.class)` and collects `reg.getEventId()` into `eventIds` list.
- If `eventIds` non-empty, calls `fetchEventDetails(eventIds)`.
- On failure shows toast "Error fetching RSVPs".

fetchEventDetails(List<String> eventIds)
- Uses `db.collection("events").whereIn("eventId", eventIds).get()` to fetch matching events.
- On success, maps docs to `Event` objects, clears `myEventsList`, adds events and `adapter.notifyDataSetChanged()`.
- Notes: the prototype uses a `whereIn` query — Firestore limits `whereIn` to a maximum number of items (e.g., 10) unless using multiple batches; the comments mention client-side filtering alternative.

Important implementation details & caveats
- Prototype uses hardcoded `demo_student_123` as the current student ID. Replace with a real auth-based user id.
- If a registration references an event that doesn't exist, this code may silently ignore it. There is no error handling for missing event documents.

---

LandingActivity.java
Path: `app/src/main/java/com/example/debugz/view/LandingActivity.java`

Purpose
- Minimal branded landing screen with a "Get Started" button that navigates to `MainActivity`.

Important details
- `onCreate()` binds `btnGetStarted` and sets an `OnClickListener` to start `MainActivity` and call `finish()` so the user does not return to the landing screen using the back button.
- Caveat: no branching by role/auth state. Simple single-path entry.

---

EventAdapter.java
Path: `app/src/main/java/com/example/debugz/view/EventAdapter.java`

Purpose
- RecyclerView adapter that binds `Event` data to UI `item_event` cards for the discovery and My Events lists.
- Renders title, date, price, capacity text, progress bar and percentage badges.

Important imports
- `LinearProgressIndicator` (Material) for capacity progress.

Key fields
- `private List<Event> eventList;` — source list.
- `private OnEventClickListener listener;` — callback for item clicks.

Constructor
- `EventAdapter(List<Event> eventList, OnEventClickListener listener)` stores references.

ViewHolder
- `EventViewHolder` holds TextViews: `tvTitle`, `tvDate`, `tvCapacityInfo`, `tvLeftInfo`, `tvCapacityBadge`, `tvItemPrice`, and a `LinearProgressIndicator pbCapacity`.

onBindViewHolder specifics
- Reads `Event event = eventList.get(position)` and binds:
  - Title and date.
  - Price: `holder.tvItemPrice.setText(event.getPrice() != null ? event.getPrice() : "Free");` (uses display price when present).
  - Capacity logic:
    - `currentAttendees = (event.getAttendeeIds() != null) ? event.getAttendeeIds().size() : 0;`
    - `max = event.getMaxCapacity();`
    - `tvCapacityInfo` shows `currentAttendees + " / " + max`.
    - `tvLeftInfo` shows `(max - currentAttendees) + " left"`.
    - If `max > 0` compute `progress = (int) (((float) currentAttendees / max) * 100);` and set `pbCapacity` and `tvCapacityBadge` to show percent full.
  - `holder.itemView.setOnClickListener(v -> listener.onEventClick(event));` forwards clicks to the activity.

Important implementation details & caveats
- Adapter assumes `event.getAttendeeIds()` contains up-to-date attendee information; if events are loaded before attendee arrays are populated, capacity indicators may be inaccurate until refreshed.
- Duplicate logic / uniqueness is provided by `Event.addAttendee()` rather than adapter.

---

Repository-wide notes for controller/view files
- Merge conflict markers: Several view files (`MainActivity`, `EventDetailActivity`, `EventAdapter`) and tests contain Git conflict markers (`<<<<<<< HEAD`, `=======`, `>>>>>>> pr-15`). This means the repository is in a conflicted/partially-merged state. Before running the app or tests, resolve these conflicts by choosing a single implementation for each file.
- Hardcoded demo user: `demo_student_123` is used in `EventDetailActivity` and `MyEventsActivity`. Replace with FirebaseAuth or an application-level user session.
- Prefer transactional RSVP: the transactional `db.runTransaction` implementation in `EventDetailActivity` enforces capacity and duplicate-checking atomically and should be kept over the non-transactional variant.
- Seeding logic: Both `EventController.seedDemoData` and `MainActivity.seedDemoData` (alternate variant) write demo events to Firestore. For production, restrict seeding to dev builds or the emulator.

---

If you want, I can:
- Resolve the merge conflicts in these files, keep the transactional RSVP flow, and wire `MainActivity` to use `EventController` consistently.
- Replace `demo_student_123` usage with a lightweight `UserSession` abstraction (and update tests accordingly).
- Add inline comments in the code for every important method to explain behavior and assumptions.

Which of the above should I implement next?
