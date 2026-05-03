# Debugz - Final Release (Project Part 4)

## Team Information
- **Team Name:** debugz

| Name                       | Roll Number | GitHub ID                 |
|----------------------------|-------------|---------------------------|
| Abdul Moeez Khurshid       | 27100284    | Jokekiller321             |
| Muhammad Mustafa Javed     | 27100189    | 27100189-MuhammadMustafa  |
| Faneez Zulfiqar Ali        | 27100247    | faneezali7                |
| Muhammad Ahmed             | 27100195    | ahmeddd-2211              |
| Huzayfah Abid              | 27100271    | huzayfah420               |

---

## Final Product Backlog (Phase 4 Status)

| ID | User Story | Status |
|-----|-----|-----|
| US1 | Browse all upcoming campus events | **DONE ✅** |
| US2 | Search events by category or keyword | **DONE ✅** |
| US3 | View detailed event information | **DONE ✅** |
| US4 | RSVP and **Cancel RSVP** to an event | **DONE ✅** |
| US5 | See how many spots remain (Real-time Capacity) | **DONE ✅** |
| US7 | Real-time notifications for RSVPs | **DONE ✅** |
| US8 | **Calendar Integration** (Add to Google Cal) | **DONE ✅** |
| US10 | Monitor RSVP'd events (My Events screen) | **DONE ✅** |
| US12 | Organizer: Create and Edit event details | **DONE ✅** |
| US13 | Organizer: Set event capacity | **DONE ✅** |
| US14 | Organizer: View attendee list | **DONE ✅** |
| US15 | Admin: Moderate events (Delete/Approve Users) | **DONE ✅** |

---

## Design Patterns Applied
- **Singleton Pattern:** Implemented in `UserSession.java` to maintain a single, consistent user state throughout the application lifecycle.
- **Controller Pattern:** Used in `EventController` and `AccountController` to decouple Firestore database logic from the Activity (View) layers.
- **Adapter Pattern:** Implemented in `EventAdapter` and `PendingAccountAdapter` to bridge complex data models with RecyclerView UI components.
- **Observer Pattern:** Leveraged Firebase Firestore Snapshots to automatically update the UI when the remote database state changes.
- **Transaction Pattern:** Used in `EventDetailActivity` to ensure atomic updates to event capacity, preventing race conditions during concurrent RSVPs.

---

## Sprint Planning (Retrospective)
- **Sprint 1 (Mar 1 - Mar 15):** Requirement gathering, CRC Cards, and Wireframing.
- **Sprint 2 (Mar 16 - Apr 6):** Initial Firestore integration, Student Browsing, and Search.
- **Sprint 3 (Apr 7 - Apr 20):** Organizer Dashboard, Event Creation, and Admin Deletion.
- **Sprint 4 (Apr 21 - Final):** Multi-role Approval system, Calendar Integration, Cancel RSVP, and Espresso UI Testing.

---

## Object Oriented Analysis
- **UML Diagram:** https://github.com/SEdebugz/Debugz/blob/main/Docs/uml.png
- **CRC Cards:** https://github.com/SEdebugz/Debugz/blob/main/Docs/crc_cards_picture.png

---

## Technical Features
- **Backend:** Firebase Firestore (Atomic Transactions & Write Batches).
- **Notifications:** Local notifications via `NotificationHelper` for RSVP confirmation.
- **Calendar:** Intent-based insertion into native Calendar apps.
- **Testing:** Comprehensive Unit tests (Logic) and Espresso tests (UI Flow).
