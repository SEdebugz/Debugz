package com.example.debugz;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.debugz.view.MyEventsActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Intent / UI tests for MyEventsActivity.
 * Validates requirement US10 – monitor RSVP'd events to plan time effectively.
 */
@RunWith(AndroidJUnit4.class)
public class MyEventsActivityTest {

    @Rule
    public ActivityScenarioRule<MyEventsActivity> activityRule =
            new ActivityScenarioRule<>(MyEventsActivity.class);

    // ──────────────────────────────────────────────
    // US10: Monitor RSVP'd events
    // ──────────────────────────────────────────────

    @Test
    public void testMyEventsTitle_isDisplayed() {
        onView(withId(R.id.tvMyEventsTitle))
                .check(matches(isDisplayed()));
        onView(withId(R.id.tvMyEventsTitle))
                .check(matches(withText("My RSVP'd Events")));
    }

    @Test
    public void testMyEventsRecyclerView_isDisplayed() {
        onView(withId(R.id.rvMyEvents))
                .check(matches(isDisplayed()));
    }
}

