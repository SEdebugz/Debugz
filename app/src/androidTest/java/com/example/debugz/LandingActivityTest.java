package com.example.debugz;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.debugz.view.LandingActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Intent / UI tests for the LandingActivity (app entry point / login screen).
 * Validates the landing page requirement from the action items and
 * ensures the "Get Started" / "Sign In" button navigates to the Discover Events feed.
 */
@RunWith(AndroidJUnit4.class)
public class LandingActivityTest {

    @Rule
    public ActivityScenarioRule<LandingActivity> activityRule =
            new ActivityScenarioRule<>(LandingActivity.class);

    // ──────────────────────────────────────────────
    // Landing Page – Layout Verification
    // ──────────────────────────────────────────────

    @Test
    public void testGetStartedButton_isDisplayed() {
        onView(withId(R.id.btnGetStarted))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testLoginCard_isDisplayed() {
        onView(withId(R.id.cardLogin))
                .check(matches(isDisplayed()));
    }

    // ──────────────────────────────────────────────
    // Landing → Main Navigation
    // ──────────────────────────────────────────────

    @Test
    public void testGetStartedButton_navigatesToMainActivity() {
        onView(withId(R.id.btnGetStarted))
                .perform(click());
        // After click, the MainActivity (Discover Events) should display
        onView(withId(R.id.tvDiscover))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testGetStartedButton_showsRecyclerViewOnMain() {
        onView(withId(R.id.btnGetStarted))
                .perform(click());
        onView(withId(R.id.rvEvents))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testGetStartedButton_showsSearchBarOnMain() {
        onView(withId(R.id.btnGetStarted))
                .perform(click());
        onView(withId(R.id.etSearch))
                .check(matches(isDisplayed()));
    }
}

