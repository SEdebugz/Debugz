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
| **Admin** | `AdminDashboardActivity` | Approve signups + delete any event |

There is **no Firebase Auth**. Student and organizer accounts are stored in Firestore and require **manual admin approval** before login. Logged-in identity is then cached locally in `SharedPreferences` via `UserSession`.

Hardcoded admin credentials:
- username: `sefinalboss`
- password: `1234`

---

## Directory Structure

```text
app/src/main/java/com/example/debugz/
├── UserSession.java                  # Role-based session (SharedPreferences)
├── NotificationHelper.java           # Notification channel + RSVP/reminder posts
├── ReminderWorker.java               # WorkManager worker for 24h-before reminders
│
├── models/
│   ├── Account.java                  # Student/organizer login record + approval status
│   ├── Event.java                    # Core event POJO + upvote helpers
│   ├── Student.java                  # Student profile + registration ID list
│   ├── Organizer.java                # Organizer profile + created event ID list
│   ├── Registration.java             # RSVP join record (studentId, eventId, status)
│   └── Administrator.java            # Admin identity POJO
│
├── controller/
│   ├── AccountController.java        # Firestore signup/login/approval operations
│   └── EventController.java          # Firestore event CRUD + upvote operations
│
└── view/
    ├── LandingActivity.java          # Shared login page + signup link + admin login
    ├── SignupActivity.java           # Student/organizer signup form (pending approval)
    ├── MainActivity.java             # Student discover feed (US1, US2, US6)
    ├── EventDetailActivity.java      # Event detail, RSVP, upvote, calendar (US3–US9, US11)
    ├── MyEventsActivity.java         # Student's RSVP'd events (US10)
    ├── OrganizerDashboardActivity.java  # Organizer event list (US12–US14)
    ├── EditEventActivity.java        # Create / edit event form (US12, US13)
    ├── AttendeesActivity.java        # RSVP'd attendee list for an event (US14)
    ├── AdminDashboardActivity.java   # Pending signups + event moderation (US15 + approvals)
    └── EventAdapter.java             # RecyclerView adapter for event cards

app/src/main/res/layout/
├── activity_landing.xml
├── activity_signup.xml
├── activity_main.xml
├── activity_event_detail.xml
├── activity_my_events.xml
├── activity_organizer_dashboard.xml
├── activity_edit_event.xml
├── activity_attendees.xml
├── activity_admin_dashboard.xml
├── item_event.xml
├── item_event_manage.xml
├── item_attendee.xml
├── item_pending_account.xml          # Admin approval row
└── item_admin_event.xml

app/src/test/java/com/example/debugz/
├── AccountTest.java                  # 6 tests — Account POJO + approval helpers
├── EventTest.java                    # 34 tests — Event POJO + upvote logic
├── StudentTest.java                  # 20 tests — Student POJO
├── OrganizerTest.java                # 15 tests — Organizer POJO
├── RegistrationTest.java             # 15 tests — Registration POJO
├── AdministratorTest.java            # 6 tests — Administrator POJO
├── ModelIntegrationTest.java         # 15 tests — cross-model flows
├── EventControllerPureTest.java      # 10 tests — pure filter/search logic
├── UserSessionPureTest.java          # 6 tests — role/admin constants
└── ExampleUnitTest.java              # 1 test — placeholder
```

**Total: 128 unit tests, 0 failures.**

---

## Firestore Collections

### `accounts`
Maps to `Account.java`. Used for manual signup/login of students and organizers.

| Field | Type | Notes |
|---|---|---|
| `accountId` | String | Roll number / org ID; also used as document ID |
| `name` | String | Student name or society/organizer name |
| `email` | String | Optional |
| `password` | String | Plain text in prototype only |
| `role` | String | `STUDENT` or `ORGANIZER` |
| `status` | String | `PENDING`, `APPROVED`, `REJECTED` |
| `createdAt` | long | Epoch ms |

### `events`
Maps to `Event.java`.

| Field | Type | Notes |
|---|---|---|
| `eventId` | String | Usually equals the document ID |
| `title` | String | |
| `description` | String | |
| `location` | String | |
| `date` | String | Human-readable, e.g. `"March 15, 2026"` |
| `time` | String | e.g. `"10:00 AM"` |
| `organizerId` | String | Value of `UserSession.getUserId()` for the organizer |
| `maxCapacity` | int | 0 = unlimited |
| `price` | String | e.g. `"Free"`, `"500 PKR"` |
| `attendeeIds` | Array<String> | Student IDs who RSVP'd |
| `upvoteCount` | int | Managed by `EventController.toggleUpvote()` |
| `upvotedBy` | Array<String> | Student IDs that have upvoted |

### `registrations`
Maps to `Registration.java`.

| Field | Type | Notes |
|---|---|---|
| `registrationId` | String | UUID / document ID |
| `studentId` | String | `UserSession.getUserId()` at RSVP time |
| `eventId` | String | Links to the event |
| `status` | String | `"Confirmed"`, `"Waitlisted"`, `"Checked-in"` |
| `timestamp` | long | Epoch ms |

---

## Key Classes

### `UserSession`
```java
UserSession session = UserSession.getInstance(context);
session.isLoggedIn();
session.getUserId();
session.getUserName();
session.getRole();
session.login(id, name, role);
session.logout();
UserSession.ADMIN_USERNAME; // "sefinalboss"
UserSession.ADMIN_PASSWORD; // "1234"
```

### `AccountController`
```java
accountController.submitSignup(account, listener);      // creates PENDING account
accountController.login(id, password, role, listener);  // approved student/organizer login
accountController.fetchPendingAccounts(listener);       // admin approval list
accountController.updateStatus(id, newStatus, listener);
```

### `EventController`
```java
controller.fetchAllEvents(listener);
controller.fetchEventsByOrganizer(organizerId, listener);
controller.createEvent(event, listener);
controller.updateEvent(event, listener);
controller.deleteEvent(eventId, listener);              // cascades to registrations
controller.toggleUpvote(eventId, studentId, listener);
controller.searchEvents(query, list);
controller.filterEventsByOrganizer(id, list);
```

---

## Login / Signup Flow

### Landing page
`LandingActivity` uses **one shared ID/password form** for Students and Organizers plus a separate **Admin Login** button on the same screen.

This is the current UX decision because it is less confusing than asking all three roles for different information in one form:
- **Student login** → Firestore `accounts/{id}` must exist, role must be `STUDENT`, password must match, status must be `APPROVED`
- **Organizer login** → same, but role must be `ORGANIZER`
- **Admin login** → hardcoded username/password check only (`sefinalboss` / `1234`)

### Signup page
`SignupActivity` is linked from the landing page and is only for **Students** and **Organizers**.

Flow:
1. User selects Student or Organizer.
2. User enters name, email, ID, password, confirm password.
3. `accounts/{accountId}` is written with status `PENDING`.
4. Admin opens `AdminDashboardActivity` and approves or rejects.
5. Only approved accounts can log in.

---

## User Story Implementation Map

| US | Description | Status | Where |
|---|---|---|---|
| US1 | Browse all campus events | ✅ Done | `MainActivity` |
| US2 | Search by keyword/category | ✅ Done | `MainActivity` + `EventController.searchEvents()` |
| US3 | View event details | ✅ Done | `EventDetailActivity` |
| US4 | RSVP to event | ✅ Done | `EventDetailActivity.handleRSVP()` transaction |
| US5 | See spots remaining | ✅ Done | `EventDetailActivity` |
| US6 | Upvote events / trending feed | ✅ Done | `EventController.toggleUpvote()` |
| US7 | Push notification on RSVP | ✅ Done | `NotificationHelper.postRsvpConfirmation()` |
| US8 | Add event to calendar | ✅ Done | `CalendarContract` insert intent |
| US9 | See who's attending | ✅ Done | `tvAttendeesInfo` in `EventDetailActivity` |
| US10 | My RSVP'd events | ✅ Done | `MyEventsActivity` |
| US11 | Event reminder notification | ✅ Done | `ReminderWorker` via WorkManager |
| US12 | Organizer edits event details | ✅ Done | `EditEventActivity` |
| US13 | Organizer sets capacity/price | ✅ Done | `EditEventActivity` |
| US14 | Organizer views attendees | ✅ Done | `AttendeesActivity` |
| US15 | Admin removes event listings | ✅ Done | `AdminDashboardActivity` |

Additional prototype feature now present:
- **Manual signup + admin approval** for Students and Organizers

---

## Work Completed So Far

The current codebase has moved beyond the original checkpoint implementation and now includes a complete multi-role prototype with shared Firestore data and local sessions.

Completed implementation highlights:
- **Student flows**: browse, search, view details, RSVP, upvote, add to calendar, reminders, and My Events
- **Organizer flows**: create event, edit event, set capacity/price, and inspect attendee registrations
- **Admin flows**: delete any event and batch-remove related registrations
- **Account flows**: student/organizer signup, pending approval state, admin approval/rejection, and approval-gated login
- **Persistence**: events, registrations, and accounts are all backed by Firestore; local `SharedPreferences` only stores the active session
- **Documentation**: architecture, models, tests, and assistant guidance have all been expanded in `Docs/` and `CLAUDE.md`
- **Testing**: the JVM unit test suite covers models, integration-style flows, controller pure logic, and session/admin constants

---

## Admin Delete Cascade

`EventController.deleteEvent(eventId, listener)`:
1. Deletes `events/{eventId}`
2. Queries `registrations` where `eventId == X`
3. Batch-deletes all matching registrations
4. Calls `listener.onSuccess()`

Deleted events disappear from the student feed and My Events on the next load.

---

## Notifications and Reminders

| Trigger | Method | Class |
|---|---|---|
| RSVP success | `postRsvpConfirmation(context, title)` | `NotificationHelper` |
| 24 h before event | `postReminder(context, title)` | `NotificationHelper` / `ReminderWorker` |

Channel ID: `debugz_events`

`EventDetailActivity.scheduleReminder()` parses `date + time` using `SimpleDateFormat("MMMM d, yyyy h:mm a", Locale.US)` and enqueues a `OneTimeWorkRequest<ReminderWorker>` for 24h before the event. If parsing fails or the event is within 24 hours, scheduling is skipped.

---

## Test Strategy

All tests are JVM unit tests in `app/src/test/`.

| Suite | Count | Covers |
|---|---|---|
| `AccountTest` | 6 | Account POJO fields + approval helpers |
| `EventTest` | 34 | Event POJO, attendees, upvotes |
| `StudentTest` | 20 | Student POJO |
| `OrganizerTest` | 15 | Organizer POJO |
| `RegistrationTest` | 15 | Registration POJO |
| `AdministratorTest` | 6 | Administrator POJO |
| `ModelIntegrationTest` | 15 | Cross-model flows |
| `EventControllerPureTest` | 10 | Pure filter/search logic |
| `UserSessionPureTest` | 6 | Role constants + admin constants |
| `ExampleUnitTest` | 1 | Placeholder |
| **Total** | **128** | **0 failures** |

Firestore-backed methods are still verified manually against the live Firestore project.

---

## Known Constraints / Prototype Caveats

| Caveat | Detail |
|---|---|
| No Firebase Auth | Account data lives in Firestore and session data lives in SharedPreferences. |
| Admin credentials hardcoded | `sefinalboss` / `1234` |
| Passwords stored in plain text | Prototype only — not acceptable for production |
| Date strings are freeform | Informal dates like `"Tonight"` skip calendar/reminder gracefully |
| Upvote not rate-limited server-side | Clearing local session can allow re-upvotes |
| `whereIn` limit | `MyEventsActivity` may truncate if a student has >30 RSVPs |
| No cancel-RSVP UI | Data model supports it, UI not implemented |

---

## Current Issues / Known Problems

These are the main remaining issues in the current prototype beyond the user stories already implemented:

1. **Authentication is prototype-grade, not production-grade**
   - Student and organizer passwords are stored in Firestore as plain text.
   - Admin credentials are hardcoded in the app.
   - User sessions are local-only (`SharedPreferences`) and not backed by Firebase Auth tokens.

2. **Authorization still depends heavily on client behavior**
   - The app code enforces most role checks, but real security still depends on proper Firestore rules.
   - Without secure backend enforcement, a malicious client could bypass expected UI restrictions if Firestore rules are too permissive.

3. **Some user-story-adjacent features remain simplified**
   - `MyEventsActivity` uses `whereIn`, so large RSVP histories may not fully load.
   - There is no cancel-RSVP UI.
   - Friend/social attendance is represented only as attendee counts, not actual friend relationships.
   - Reminder scheduling is per-device and local, not server-side.

4. **Data validation is intentionally minimal**
   - Dates and times are stored as user-entered strings.
   - Informal values like `"Tonight"` are accepted but skip calendar/reminder automation.
   - Signup/login and event forms focus on functionality over strict validation.

5. **Manual testing is still required for Firestore-backed flows**
   - Unit tests verify pure Java behavior.
   - Firestore integration, notifications, WorkManager, and calendar intents must still be verified on device/emulator.

---

## Recommended Next Steps

If this project is extended further, the highest-value next steps are:

1. **Replace manual auth with Firebase Auth**
   - Stop storing passwords in Firestore.
   - Use authenticated UIDs for students, organizers, and admins.

2. **Add Firestore security rules for roles and ownership**
   - Only organizers should edit their own events.
   - Only admins should approve accounts or delete arbitrary events.
   - Only the currently authenticated user should create their own registrations/upvotes.

3. **Improve account onboarding UX**
   - Add clearer login error states.
   - Add approval status messaging after signup.
   - Optionally separate admin login visually from the student/organizer login form even further.

4. **Harden event data handling**
   - Replace freeform date/time strings with structured timestamps.
   - Add stronger validation for capacity, price, and date inputs.

5. **Expand test coverage beyond JVM unit tests**
   - Add emulator/instrumentation tests for Firestore-backed flows.
   - Add UI tests for signup approval, organizer event creation, and admin moderation.

6. **Add missing usability features**
   - Cancel RSVP
   - Waitlist promotion flow
   - Friend/social layer if US9 is expanded beyond attendee counts
   - Better organizer/admin filtering and search tools

---

## How to Run Tests

```powershell
.\gradlew :app:testDebugUnitTest
```

## How to Build / Install

```powershell
.\gradlew :app:assembleDebug
.\gradlew :app:installDebug
```
