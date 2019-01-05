package com.antoine.go4lunch;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.antoine.go4lunch.controlers.activity.MainActivity;
import com.antoine.go4lunch.data.PlaceApiStream;
import com.antoine.go4lunch.models.matrixAPI.DistanceMatrixRestaurant;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)

public class   ExampleInstrumentedTest {

    Map<String,String> queryLocation = new HashMap<>();

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule =
            new ActivityTestRule<>(MainActivity.class);

    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.antoine.go4lunch", appContext.getPackageName());
    }

    @Test
    public void checkButtonViewRestaurantIsClickable(){
        onView(withId(R.id.navigation_dashboard)).perform(click());
    }

    @Test
    public void checkButtonMpaIsClickable(){
        onView(withId(R.id.navigation_notifications)).perform(click());
    }

    @Test
    public void testStreamDistanceMatrix(){
        Context appContext = InstrumentationRegistry.getTargetContext();
        queryLocation.put("origins", "47.8567521,0.2058559");
        queryLocation.put("destinations","place_id:ChIJC_X4VNqQ4kcRem2BgyFdOKY");
        queryLocation.put("key", appContext.getString(R.string.google_maps_api));

        Observable<DistanceMatrixRestaurant> observableMost = PlaceApiStream.streamFetchDistanceMatrix(queryLocation);
        TestObserver<DistanceMatrixRestaurant> testObserver = new TestObserver<>();
        observableMost.subscribeWith(testObserver)
                .assertNoErrors() // 3.1 - Check if no errors
                .assertNoTimeout() // 3.2 - Check if no Timeout
                .awaitTerminalEvent();// 3.3 - Await the stream terminated before continue

        DistanceMatrixRestaurant API = testObserver.values().get(0);

        assertThat("API status: ", API.getRows().get(0).getElements().get(0).getDistance().getText().equals("0.6 km"));
    }
}
