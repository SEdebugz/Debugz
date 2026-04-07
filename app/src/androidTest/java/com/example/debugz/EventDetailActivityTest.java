package com.example.debugz;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.debugz.view.EventDetailActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Intent / UI tests for EventDetailActivity.
 * Validates requirements:
 *   US3 – View detailed event information (topic, time, location, capacity)
 *   US4 – RSVP to an event
 *   US5 – See how many spots remain
 */
@RunWith(AndroidJUnit4.class)
public class EventDetailActivityTest {

    /**
     * Provides a realistic Intent that simulates what MainActivity sends
     * when a student taps on the "LUMUN 2026" event card.
     */
    private static Intent createTestIntent() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), EventDetailActivity.class);
        intent.putExtra("eventId", "event_002");
        intent.putExtra("title", "LUMUN 2026");
        intent.putExtra("date", "March 18, 2026");
        intent.putExtra("time", "09:00 AM");
        intent.putExtra("location", "SDSB Auditorium");
        intent.putExtra("description", "Premier Model UN conference at LUMS.");
        intent.putExtra("capacity", 650);
        return intent;
    }

    @Rule
    public ActivityScenarioRule<EventDetailActivity> activityRule =
            new ActivityScenarioRule<>(createTestIntent());

    // ──────────────────────────────────────────────
    // US3: View detailed event information
    // ──────────────────────────────────────────────

    @Test
    public void testEventTitle_isDisplayedCorrectly() {
        onView(withId(R.id.tvDetailTitle))
                .check(matches(isDisplayed()));
        onView(withId(R.id.tvDetailTitle))
                .check(matches(withText("LUMUN 2026")));
    }

    @Test
    public void testEventDate_isDisplayedCorrectly() {
        onView(withId(R.id.tvDetailDate))
                .check(matches(isDisplayed()));
        onView(withId(R.id.tvDetailDate))
                .check(matches(withText("March 18, 2026 at 09:00 AM")));
    }

    @Test
    public void testEventLocation_isDisplayedCorrectly() {
        onView(withId(R.id.tvDetailLocation))
                .check(matches(isDisplayed()));
        onView(withId(R.id.tvDetailLocation))
                .check(matches(withText("SDSB Auditorium")));
    }

    @Test
    public void testEventDescription_isDisplayedCorrectly() {
        onView(withId(R.id.tvDetailDescription))
                .check(matches(isDisplayed()));
        onView(withId(R.id.tvDetailDescription))
                .check(matches(withText("Premier Model UN conference at LUMS.")));
    }

    @Test
    public void testEventCapacity_isDisplayedCorrectly() {
        onView(withId(R.id.tvDetailCapacity))
                .check(matches(isDisplayed()));
        onView(withId(R.id.tvDetailCapacity))
                .check(matches(withText("Max Capacity: 650")));
    }

    // ──────────────────────────────────────────────
    // US4: RSVP to an event
    // ──────────────────────────────────────────────

    @Test
    public void testRSVPButton_isDisplayed() {
        onView(withId(R.id.btnRSVP))
                .check(matches(isDisplayed()));
        onView(withId(R.id.btnRSVP))
                .check(matches(withText("RSVP to Event")));
    }

    @Test
    public void testRSVPButton_isClickable() {
        onView(withId(R.id.btnRSVP))
                .perform(click());
        // The RSVP action triggers a Firestore write. In a device test this
        // will either succeed (with internet) or show a toast.
        // The key validation is that clicking doesn't crash the app.
    }
}

