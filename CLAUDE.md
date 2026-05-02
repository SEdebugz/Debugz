# CLAUDE.md — Debugz / LUMS Events App

> Codebase summary for AI assistants. Read this before making any changes.

---

## Project Overview

**Debugz** is an Android event-discovery and RSVP app for LUMS campus.  
Tech stack: **Java · Android SDK 24–35 · Firebase Firestore · WorkManager · Material 3**

Three user roles share a single app:

| Role | Entry Point | Key Features |
|---|---|---|
| **Student** | `MainActivity` | Browse, search, RSVP, upvote, calendar, My Events |
| **Organizer** | `OrganizerDashboardActivity` | Create, edit, view attendees |
| **Admin** | `AdminDashboardActivity` | Delete any event (cascades to registrations) |

There is **no Firebase Auth**. Identity is stored in `SharedPreferences` via `UserSession`.  
The admin password is `admin2026` (hardcoded constant in `UserSession.ADMIN_PASSWORD`).

---

## Directory Structure

```
app/src/main/java/com/example/debugz/
├── UserSession.java                  # Role-based session (SharedPreferences)
├── NotificationHelper.java           # Notification channel + RSVP/reminder posts
├── ReminderWorker.java               # WorkManager worker for 24h-before reminders
│
├── models/
│   ├── Event.java                    # Core event POJO + upvote helpers
│   ├── Student.java                  # Student profile + registration ID list
│   ├── Organizer.java                # Organizer profile + created event ID list
│   ├── Registration.java             # RSVP join record (studentId, eventId, status)
│   └── Administrator.java            # Admin identity POJO
│
├── controller/
│   └── EventController.java          # All Firestore read/write operations
│
└── view/
    ├── LandingActivity.java          # Role login (name + ID/password)
    ├── MainActivity.java             # Student discover feed (US1, US2, US6)
    ├── EventDetailActivity.java      # Event detail, RSVP, upvote, calendar (US3–US9, US11)
    ├── MyEventsActivity.java         # Student's RSVP'd events (US10)
    ├── OrganizerDashboardActivity.java  # Organizer event list (US12–US14)
    ├── EditEventActivity.java        # Create / edit event form (US12, US13)
    ├── AttendeesActivity.java        # RSVP'd attendee list for an event (US14)
    ├── AdminDashboardActivity.java   # All events + delete (US15)
    └── EventAdapter.java             # RecyclerView adapter for event cards

app/src/main/res/layout/
├── activity_landing.xml
├── activity_main.xml
├── activity_event_detail.xml
├── activity_my_events.xml
├── activity_organizer_dashboard.xml
├── activity_edit_event.xml
├── activity_attendees.xml
├── activity_admin_dashboard.xml
├── item_event.xml                    # Event card (upvote count badge)
├── item_event_manage.xml             # Organizer event card (Edit + Attendees buttons)
├── item_attendee.xml                 # Single attendee row
└── item_admin_event.xml              # Admin event row (Delete button)

app/src/test/java/com/example/debugz/
├── EventTest.java                    # 34 tests — Event POJO + upvote logic
├── StudentTest.java                  # 20 tests — Student POJO
├── OrganizerTest.java                # 15 tests — Organizer POJO
├── RegistrationTest.java             # 15 tests — Registration POJO
├── AdministratorTest.java            # 6 tests  — Administrator POJO
├── ModelIntegrationTest.java         # 15 tests — cross-model flows (incl. admin, US13/14)
├── EventControllerPureTest.java      # 10 tests — pure filter/search logic (no Firebase)
├── UserSessionPureTest.java          # 4 tests  — UserSession constants
└── ExampleUnitTest.java              # 1 test   — placeholder
```

**Total: 120 unit tests, 0 failures.**

---

## Firestore Collections

### `events`
Maps to `Event.java`. Every document is created by an organizer through `EditEventActivity`.

| Field | Type | Notes |
|---|---|---|
| `eventId` | String | Usually equals the document ID (set after fetch) |
| `title` | String | |
| `description` | String | |
| `location` | String | |
| `date` | String | Human-readable, e.g. `"March 15, 2026"` |
| `time` | String | e.g. `"10:00 AM"` |
| `organizerId` | String | Value of `UserSession.getUserId()` for the organizer |
| `maxCapacity` | int | 0 = unlimited |
| `price` | String | e.g. `"Free"`, `"500 PKR"` |
| `attendeeIds` | Array\<String\> | Student IDs who RSVP'd |
| `upvoteCount` | int | Incremented/decremented by `EventController.toggleUpvote()` |
| `upvotedBy` | Array\<String\> | Student IDs that upvoted (prevents duplicates) |

### `registrations`
Maps to `Registration.java`. Written inside the RSVP transaction in `EventDetailActivity`.

| Field | Type | Notes |
|---|---|---|
| `registrationId` | String | UUID, equals the document ID |
| `studentId` | String | `UserSession.getUserId()` at RSVP time |
| `eventId` | String | Links to the `events` document |
| `status` | String | `"Confirmed"`, `"Waitlisted"`, `"Checked-in"` |
| `timestamp` | long | Epoch ms |

---

## Key Classes

### `UserSession`
```java
UserSession session = UserSession.getInstance(context);
session.isLoggedIn()        // true if session exists
session.getUserId()         // roll number / org ID / "admin"
session.getUserName()       // display name
session.getRole()           // ROLE_STUDENT | ROLE_ORGANIZER | ROLE_ADMIN
session.login(id, name, role)
session.logout()
UserSession.ADMIN_PASSWORD  // "admin2026"
```

### `EventController`
```java
// Read
controller.fetchAllEvents(OnEventsFetchedListener)
controller.fetchEventsByOrganizer(organizerId, OnEventsFetchedListener)

// Write
controller.createEvent(event, OnEventOperationListener)
controller.updateEvent(event, OnEventOperationListener)
controller.deleteEvent(eventId, OnEventOperationListener)   // cascades to registrations

// US6
controller.toggleUpvote(eventId, studentId, OnUpvoteListener)

// Pure (no Firestore — unit testable)
controller.searchEvents(query, list)           // title/description substring match
controller.filterEventsByOrganizer(id, list)   // in-memory organizer filter
```

### `Event` (upvote helpers added in Phase 3)
```java
event.addUpvote(studentId)     // returns true if newly added
event.removeUpvote(studentId)  // returns true if removed
event.getUpvoteCount()
event.getUpvotedBy()
```

---

## User Story Implementation Map

| US | Description | Status | Where |
|---|---|---|---|
| US1 | Browse all campus events | ✅ Done | `MainActivity` |
| US2 | Search by keyword/category | ✅ Done | `MainActivity` + `EventController.searchEvents()` |
| US3 | View event details | ✅ Done | `EventDetailActivity` |
| US4 | RSVP to event | ✅ Done | `EventDetailActivity.handleRSVP()` (Firestore transaction) |
| US5 | See spots remaining | ✅ Done | `EventDetailActivity` — live `tvSpotsLeft` |
| US6 | Upvote events / trending feed | ✅ Done | `EventController.toggleUpvote()` + feed sorted by `upvoteCount` |
| US7 | Push notification on RSVP | ✅ Done | `NotificationHelper.postRsvpConfirmation()` |
| US8 | Add event to calendar | ✅ Done | `CalendarContract` ACTION_INSERT intent |
| US9 | See who's attending | ✅ Done | `tvAttendeesInfo` in `EventDetailActivity` |
| US10 | My RSVP'd events | ✅ Done | `MyEventsActivity` — queries registrations by studentId |
| US11 | Event reminder notification | ✅ Done | `ReminderWorker` via WorkManager (24 h before event) |
| US12 | Organizer edits event details | ✅ Done | `EditEventActivity` (edit mode) |
| US13 | Organizer sets capacity/price | ✅ Done | `EditEventActivity` — maxCapacity + price fields |
| US14 | Organizer views attendees | ✅ Done | `AttendeesActivity` — queries registrations by eventId |
| US15 | Admin removes event listings | ✅ Done | `AdminDashboardActivity` — deletes event + registrations |

---

## Navigation Flow

```
LandingActivity
 ├── [Continue as Student]  ──────────────────────► MainActivity
 │                                                      ├── [Discover feed] → EventDetailActivity
 │                                                      │       ├── RSVP (transaction)
 │                                                      │       ├── Upvote (transaction)
 │                                                      │       └── Add to Calendar / WorkManager reminder
 │                                                      ├── [My Events] → MyEventsActivity
 │                                                      └── [Logout] → LandingActivity
 │
 ├── [Continue as Organizer] ────────────────────► OrganizerDashboardActivity
 │                                                      ├── [+ Create] → EditEventActivity (create)
 │                                                      ├── [Edit]     → EditEventActivity (edit)
 │                                                      ├── [Attendees]→ AttendeesActivity
 │                                                      └── [Logout]   → LandingActivity
 │
 └── [Continue as Admin]    ──────────────────────► AdminDashboardActivity
                                                        ├── [Delete event] (with confirmation dialog)
                                                        └── [Logout] → LandingActivity
```

**Session persistence:** `LandingActivity.onCreate()` checks `UserSession.isLoggedIn()`; if true it skips the form and routes directly to the role screen.

---

## RSVP Transaction (Firestore)

`EventDetailActivity.handleRSVP()` runs a single Firestore transaction that:
1. Reads the event document snapshot.
2. Checks `attendeeIds` does not already contain `studentId` → throws `ALREADY_EXISTS` if so.
3. Checks `attendeeIds.size() < maxCapacity` → throws `RESOURCE_EXHAUSTED` if full.
4. Appends `studentId` to `attendeeIds` and calls `transaction.update(eventRef, …)`.
5. Creates a `Registration` document in the same transaction with `transaction.set(…)`.

On success: fires a local notification (US7), schedules a WorkManager reminder (US11), refreshes the UI, and shows an "Add to Calendar?" dialog (US8).

---

## Admin Delete Cascade

`EventController.deleteEvent(eventId, listener)`:
1. Deletes the `events/{eventId}` document.
2. Queries `registrations` where `eventId == X`.
3. Batch-deletes all matching registration documents.
4. Calls `listener.onSuccess()`.

This means deleted events disappear from the student discovery feed **and** from My Events on the next load.

---

## Notifications

| Trigger | Method | Class |
|---|---|---|
| RSVP success | `postRsvpConfirmation(context, title)` | `NotificationHelper` |
| 24 h before event | `postReminder(context, title)` | `NotificationHelper` (called by `ReminderWorker`) |

Channel ID: `debugz_events`  
Notification channel is created in `LandingActivity.onCreate()` (idempotent).  
`POST_NOTIFICATIONS` permission declared in `AndroidManifest.xml`; silently skipped if not granted (Android 13+ only — older devices don't require it).

---

## WorkManager Reminder (US11)

In `EventDetailActivity.scheduleReminder()`:
- Parses `date + time` strings using `SimpleDateFormat("MMMM d, yyyy h:mm a", Locale.US)`.
- Delay = `eventDateMs − 24h − now`. If ≤ 0 (event within 24 h or informal date like `"Tonight"`), scheduling is silently skipped.
- Enqueues a `OneTimeWorkRequest<ReminderWorker>` with the event title as input data.

---

## Gradle Dependencies

```kotlin
// app/build.gradle.kts
implementation(libs.appcompat)
implementation(libs.material)
implementation(libs.activity)
implementation(libs.constraintlayout)
implementation(platform(libs.firebase.bom))   // BOM 33.7.0
implementation(libs.firebase.firestore)
implementation(libs.firebase.analytics)
implementation(libs.work.runtime)              // WorkManager 2.9.1
testImplementation(libs.junit)
```

---

## Test Strategy

All tests are **JVM unit tests** (`app/src/test/`) — no Android runtime, no Firestore emulator required.

| Suite | Count | Covers |
|---|---|---|
| `EventTest` | 34 | Event POJO fields, attendee helpers, upvote add/remove/duplicate |
| `StudentTest` | 20 | Student POJO, preferences, registrationIds |
| `OrganizerTest` | 15 | Organizer POJO, createdEventIds |
| `RegistrationTest` | 15 | Registration POJO, status transitions |
| `AdministratorTest` | 6 | Administrator POJO |
| `ModelIntegrationTest` | 15 | Cross-model RSVP, waitlist, admin removal, capacity update (US13/14) |
| `EventControllerPureTest` | 10 | `filterEventsByOrganizer` + `searchEvents` — pure in-memory logic |
| `UserSessionPureTest` | 4 | Role constants and admin password constant |
| `ExampleUnitTest` | 1 | Placeholder |
| **Total** | **120** | **0 failures** |

Firestore-backed methods (`fetchAllEvents`, `createEvent`, `updateEvent`, `deleteEvent`, `toggleUpvote`, `fetchEventsByOrganizer`) are tested manually against a live Firestore project. The `google-services.json` in `app/` points to the live project.

---

## Known Constraints / Prototype Caveats

| Caveat | Detail |
|---|---|
| No Firebase Auth | Identity stored locally in SharedPreferences. Anyone can enter any roll number. |
| Admin password hardcoded | `UserSession.ADMIN_PASSWORD = "admin2026"`. |
| Date strings are freeform | Parser expects `"MMMM d, yyyy"` (e.g. `"March 15, 2026"`). Informal dates like `"Tonight"` skip calendar/reminder features gracefully. |
| Upvote not rate-limited server-side | A user could clear SharedPreferences and re-upvote. Server-side rules not implemented. |
| `whereIn` limit | `MyEventsActivity` uses Firestore `whereIn` which supports max 30 values. Students with >30 RSVPs will see truncated list. |
| No cancel-RSVP UI | No screen to remove an RSVP. The data model supports it (`removeAttendee`, `removeRegistration`) but the UI was not in scope. |

---

## How to Run Tests

```powershell
# From repo root (Windows PowerShell)
.\gradlew :app:testDebugUnitTest

# Results in:
# app/build/test-results/testDebugUnitTest/*.xml
# app/build/reports/tests/testDebugUnitTest/index.html
```

## How to Build / Install

```powershell
.\gradlew :app:assembleDebug          # build APK
.\gradlew :app:installDebug           # install on connected device/emulator
```

