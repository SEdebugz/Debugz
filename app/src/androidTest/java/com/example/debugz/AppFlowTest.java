package com.example.debugz;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.view.View;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.debugz.view.LandingActivity;

import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * ROLE: Instrumentation Test (Espresso).
 * PURPOSE: Verifies critical multi-user flows (Admin, Organizer, Student) using a 
 * "No-Injection" strategy to avoid InputManager crashes on Android 14/15.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class AppFlowTest {

    @Rule
    public ActivityScenarioRule<LandingActivity> activityRule =
            new ActivityScenarioRule<>(LandingActivity.class);

    @Test
    public void testAdminFlow_ModerationDashboard() throws InterruptedException {
        Thread.sleep(2000);

        // 1. Login as Admin
        onView(withId(R.id.etLoginId)).perform(replaceText("sefinalboss"), closeSoftKeyboard());
        onView(withId(R.id.etLoginPassword)).perform(replaceText("1234"), closeSoftKeyboard());
        onView(withId(R.id.btnAdminLogin)).perform(forceClick());

        Thread.sleep(3000);

        // 2. Verify US15 (Admin Moderation Dashboard)
        onView(withText("Admin Dashboard")).check(matches(isDisplayed()));
        onView(withId(R.id.rvAdminEvents)).check(matches(isDisplayed()));
    }

    @Test
    public void testOrganizerFlow_CreateEventNavigation() throws InterruptedException {
        Thread.sleep(2000);

        // 1. Navigate to Signup
        onView(withId(R.id.tvSignupLink)).perform(forceClick());
        Thread.sleep(1500);

        // 2. Select Organizer Role
        onView(withId(R.id.rbSignupOrganizer)).perform(forceClick());

        // 3. Fill out basic info (US12 groundwork)
        onView(withId(R.id.etSignupName)).perform(replaceText("Music Society"), closeSoftKeyboard());
        onView(withId(R.id.etSignupId)).perform(replaceText("music_soc_test"), closeSoftKeyboard());
        
        // 4. Verify the submit button is present
        onView(withId(R.id.btnSubmitSignup)).check(matches(isDisplayed()));
    }

    @Test
    public void testStudentFlow_DiscoveryUI() throws InterruptedException {
        Thread.sleep(2000);

        // 1. Verify Login Screen elements for Student role (US1)
        onView(withId(R.id.btnStudentLogin)).check(matches(isDisplayed()));
        onView(withText("LUMS Events")).check(matches(isDisplayed()));
    }

    /**
     * Custom ViewAction that bypasses the Espresso InputManager event injection crash.
     * It calls View.performClick() directly on the UI thread.
     */
    public static ViewAction forceClick() {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isDisplayed();
            }

            @Override
            public String getDescription() {
                return "programmatic force click to bypass API 34/35 restrictions";
            }

            @Override
            public void perform(UiController uiController, View view) {
                view.performClick();
            }
        };
    }
}
