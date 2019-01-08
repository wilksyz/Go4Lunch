package com.antoine.go4lunch;



import com.antoine.go4lunch.controlers.activity.MainActivity;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class UnitTest {

    private MainActivity mainActivity;


    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Before
    public void setUp(){
        mainActivity = new MainActivity();

    }

    @Test
    public void  unitTestToBounds(){
        LatLng latLng = new LatLng(47.8566983, 0.20596);
        LatLng northEast = new LatLng(47.83870877495221,0.17916340861802407);
        LatLng southWest = new LatLng(47.87468158561665,0.23277518914539397);

        LatLngBounds latLngBounds = new LatLngBounds(northEast, southWest);

        LatLngBounds latLngEnd = MainActivity.toBounds(latLng);

        assertEquals(latLngBounds, latLngEnd);
    }

    @Test
    public void unitTestCheckingDateForChatActivity(){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 0);
        long timeInMillisActual =calendar.getTimeInMillis();

        long timeInMillis = MainActivity.getTimeInMillisForAlarm();

        if (!calendar.before(Calendar.getInstance())){
            assertEquals(timeInMillisActual, timeInMillis);
        }else{
            assertNotEquals(timeInMillisActual, timeInMillis);
        }
    }
}