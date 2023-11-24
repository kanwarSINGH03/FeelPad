package com.example.journalapp;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.core.app.ActivityScenario;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class SearchActivityTest {

    @Test
    public void testSearchQuery() {
            // Launch the activity
        ActivityScenario.launch(SearchActivity.class);

        // Wait for the SearchView to become visible
        Espresso.onView(ViewMatchers.withId(R.id.noteSearchView))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        // Type a search query in the SearchView
        String testQuery = "cool cool";
        Espresso.onView(ViewMatchers.withId(R.id.noteSearchView))
                .perform(ViewActions.typeText(testQuery), ViewActions.closeSoftKeyboard());

        // Optionally, perform more actions or assertions based on your UI behavior

        // Wait for a moment (you might want to use IdlingResource for more complex scenarios)
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
