package mobiquity.com.mydropox.utils;

import android.view.View;

import com.google.android.gms.maps.model.MarkerOptions;

import org.junit.Test;

import mobiquity.com.mydropox.models.PictureInfo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by JoshuaWilliams on 5/29/15.
 * This class tests some of the Utilities class methods.
 *
 * @version 1.0
 */
public class TestClass {

    /**
     * Some unit tests testing some of the Utilities class methods
     */
    @Test
    public void TestMethods(){
        //No test key exists.
        String result = Utils.getStringFromSharedPrefs("test");
        assertEquals("", result);

        //Ensure that the view's padding is set
        View view = new View(Utils.getContext());
        Utils.setPadding(view, 10, 10, 10, 10);
        int paddingBotttom = view.getPaddingBottom();
        assertEquals(10, paddingBotttom);

        //Is the user actually logged out
        Utils.logUserOut();
        boolean isUserLoggedIn = Utils.getSession().isLinked();
        assertEquals(false, isUserLoggedIn);

        //Make sure the session built is not null
        assertNotNull(Utils.buildSession());

        //Since the picture info has no lat and long set, it will return null
        MarkerOptions markerOptions = Utils.getMarker(new PictureInfo(null, "Test")); //lat and long will not be set
        assertEquals(null, markerOptions);

        //No such lat and long
        String city = Utils.getCityName(99999999.99, 9291931.31);
        assertEquals(null, city);
    }
}
