package com.example.debugz;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.debugz.view.MainActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Intent / UI tests for the MainActivity (Discover Events screen).
 * Validates requirements:
 *   US1 – Browse upcoming campus events
 *   US2 – Search events by keyword
 *   US10 – Navigate to My Events (RSVP monitoring)
 */
@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    // ──────────────────────────────────────────────
    // US1: Browse all upcoming campus events
    // ──────────────────────────────────────────────

    @Test
    public void testDiscoverEventsTitle_isDisplayed() {
        onView(withId(R.id.tvDiscover))
                .check(matches(isDisplayed()));
        onView(withId(R.id.tvDiscover))
                .check(matches(withText("Discover Events")));
    }

    @Test
    public void testRecyclerView_isDisplayed() {
        onView(withId(R.id.rvEvents))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testSearchBar_isDisplayed() {
        onView(withId(R.id.etSearch))
                .check(matches(isDisplayed()));
    }

    // ──────────────────────────────────────────────
    // US2: Search events by keyword
    // ──────────────────────────────────────────────

    @Test
    public void testSearchBarTyping() {
        onView(withId(R.id.etSearch))
                .perform(typeText("LUMUN"), closeSoftKeyboard());
    }

    @Test
    public void testSearchBar_typingAndClearing() {
        onView(withId(R.id.etSearch))
                .perform(typeText("Career"), closeSoftKeyboard());
        onView(withId(R.id.etSearch))
                .perform(clearText(), closeSoftKeyboard());
    }

    @Test
    public void testSearchBar_caseInsensitiveQuery() {
        onView(withId(R.id.etSearch))
                .perform(typeText("lumun"), closeSoftKeyboard());
    }

    @Test
    public void testSearchBar_partialKeywordQuery() {
        onView(withId(R.id.etSearch))
                .perform(typeText("Eng"), closeSoftKeyboard());
    }

    @Test
    public void testSearchBar_noMatchQuery() {
        onView(withId(R.id.etSearch))
                .perform(typeText("xyznonexistent"), closeSoftKeyboard());
    }

    // ──────────────────────────────────────────────
    // US10: Navigate to My Events screen
    // ──────────────────────────────────────────────

    @Test
    public void testMyEventsButton_isDisplayed() {
        onView(withId(R.id.btnMyEvents))
                .check(matches(isDisplayed()));
        onView(withId(R.id.btnMyEvents))
                .check(matches(withText("My Events")));
    }

    @Test
    public void testMyEventsButton_click_navigatesToMyEvents() {
        onView(withId(R.id.btnMyEvents))
                .perform(click());
        // After clicking, the MyEventsActivity should display
        onView(withId(R.id.tvMyEventsTitle))
                .check(matches(isDisplayed()));
    }
}