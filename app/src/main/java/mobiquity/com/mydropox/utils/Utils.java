package mobiquity.com.mydropox.utils;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import mobiquity.com.mydropox.R;
import mobiquity.com.mydropox.models.PictureInfo;

/**
 * Created by JoshuaWilliams on 5/27/15.
 *
 * Utilities class contatining common methods.
 *
 * @author JoshuaWilliams
 * @version 1.0
 */
public class Utils {
    private static final String LOG_TAG = "Utils";

    private static Context context;
    private static ImageLoader imageLoader;
    private static GoogleApiClient locationclient;
    private static LocationRequest locationrequest = new LocationRequest();
    private static PictureInfo picture;



    /**
     * Stores a string inside the user's shared preferences.
     *
     * @param code - the code of the string.
     * @param content - the string being saved.
     */
    public static void saveToSharedPrefsString(String code, String content) {
        Log.i(LOG_TAG, "Saving to shared prefs Key - " + code + " Value - " + content);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(code, content);
        editor.apply();
    }


    /**
     * Retrieves a string from the shared preferences.
     *
     * @param code - code of the string to retrieve from the shared prefs.
     * @return - the appropriate string.
     */
    public static String getStringFromSharedPrefs(String code) {
        Log.i(LOG_TAG, "Retreiving from shared prefs Key - " + code);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        return preferences.getString(code, "");
    }


    /**
     * Sets the color of the status bar
     *
     * @param activity - The activity at which the status bar will be set.
     * @param color - the color resource to set the status bar.
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setStatusBarColor(Activity activity, int color){
        Window window = activity.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(activity.getResources().getColor(color));
    }

    /**
     * Circular reveal on the passed view. For Android-L
     *
     * @param revealView - the view that will be revealed.
     * @param ANIM_DURATION - duration of the animation
     * @param cx - center x point of the show.
     * @param cy - center y point of the show.
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static Animator getAnimRevealShow(View revealView, long ANIM_DURATION, int cx, int cy) {
        int finalRadius = Math.max(revealView.getWidth(), revealView.getHeight());
        Animator anim = ViewAnimationUtils.createCircularReveal(revealView, cx, cy, 0, finalRadius);
        revealView.setVisibility(View.VISIBLE);
        anim.setDuration(ANIM_DURATION);
        return anim;
    }

    /**
     * Circular hide. Uses circular reveal but sets invokes opposite parameters to give the effect of shrinking rather than
     * revealing.
     *
     * @param hideView - view to be hidden.
     * @param ANIM_DURATION - duration of animation.
     * @param cx - center x point of the hide.
     * @param cy - center y point of the hide.
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static Animator getAnimRevealHide(final View hideView, long ANIM_DURATION, int cx, int cy) {
        int initialRadius = hideView.getBottom();
        Animator anim = ViewAnimationUtils.createCircularReveal(hideView, cx, cy, initialRadius, 0);
        anim.setDuration(ANIM_DURATION);
        return anim;
    }

    /**
     * Gets the user's saved Dropbox session.
     * Used for the unit testing.
     *
     * @return - the session of the user.
     */
    public static AndroidAuthSession getSession(){
        AppKeyPair appKeyPair = new AppKeyPair(getContext().getString(R.string.app_key),
                getContext().getString(R.string.app_secret));

        return new AndroidAuthSession(appKeyPair);
    }

    /**
     * Builds the {@link AndroidAuthSession} object with the app key and secret.
     *
     * @return - the user's session.
     */
    public static DropboxAPI buildSession() {
        AppKeyPair appKeyPair = new AppKeyPair(getContext().getString(R.string.app_key),
                getContext().getString(R.string.app_secret));

        AndroidAuthSession session = new AndroidAuthSession(appKeyPair);
        loadAuth(session);
        DropboxAPI mApi = new DropboxAPI<>(session);
        return mApi;
    }


    /**
     * Shows keeping the access keys returned from Trusted Authenticator in a local
     * store, rather than storing user name & password, and re-authenticating each
     * time (which is not to be done, ever).
     */
    public static void loadAuth(AndroidAuthSession session) {
        SharedPreferences prefs = getContext().getSharedPreferences(getContext().getString(R.string.account_prefs_name), 0);
        String key = prefs.getString(getContext().getString(R.string.access_key), null);
        String secret = prefs.getString(getContext().getString(R.string.access_secret), null);
        if (key == null || secret == null || key.length() == 0 || secret.length() == 0) return;

        // If the key is set to "oauth2:", then we can assume the token is for OAuth 2.
        session.setOAuth2AccessToken(secret);

    }

    public static void setContext(Context mContext){
        context = mContext;
    }

    public static Context getContext(){
        return context;
    }

    /**
     * Checks the context and sets it if context is null.
     *
     * @param mContext - context of the current activity.
     */
    public static void checkContext(Context mContext) {
        if(context == null) context = mContext;
    }

    /**
     * Clears all keys in the Shared Preferences from the application.
     */
    public static void clearKeys() {
        SharedPreferences prefs = getContext().getSharedPreferences(getContext().getString(R.string.account_prefs_name), 0);
        SharedPreferences.Editor edit = prefs.edit();
        edit.clear();
        edit.commit();
    }

    /**
     * Logs user out by removing the credentials from the {@link AndroidAuthSession} and clearing the keys
     * from the Shared Preferences
     */
    public static void logUserOut(){
        DropboxAPI mApi = Utils.buildSession();
        mApi.getSession().unlink();

        clearKeys();
    }

    /**
     * Checks if the user is currently logged in based on the {@link AndroidAuthSession} that was saved.
     */
    public static boolean checkIfUserLoggedIn() {
        DropboxAPI mApi  = Utils.buildSession();
        return mApi.getSession().isLinked();
    }

    /**
     * Shows keeping the access keys returned from Trusted Authenticator in a local
     * store, rather than storing user name & password, and re-authenticating each
     * time (which is not to be done, ever).
     */
    public static void storeAuth(AndroidAuthSession session) {
        // Store the OAuth 2 access token, if there is one.
        String oauth2AccessToken = session.getOAuth2AccessToken();
        SharedPreferences prefs = getContext().getSharedPreferences(getContext().getString(R.string.account_prefs_name), 0);
        SharedPreferences.Editor edit = prefs.edit();

        if (oauth2AccessToken != null) {
            edit.putString(getContext().getString(R.string.access_key), "oauth2:");
            edit.putString(getContext().getString(R.string.access_secret), oauth2AccessToken);
            edit.commit();
            return;
        }
        // Store the OAuth 1 access token, if there is one.  This is only necessary if
        // you're still using OAuth 1.
        AccessTokenPair oauth1AccessToken = session.getAccessTokenPair();
        if (oauth1AccessToken != null) {
            edit.putString(getContext().getString(R.string.access_key), oauth1AccessToken.key);
            edit.putString(getContext().getString(R.string.access_secret), oauth1AccessToken.secret);
            edit.commit();
            return;
        }
    }

    /**
     * Shows a toast message.
     *
     * @param message - message to be displayed
     */
    public static void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }


    /**
     * Creates a scale animation from the anim resource directory.
     *
     */
    public static Animation createScaleAnimation() {
        Animation scale = AnimationUtils.loadAnimation(getContext(), R.anim.scale_anim);
        return scale;
    }


    /**
     * Returns the singleton instance of the {@link ImageLoader} set up with the configs needed for the app.
     *
     * @return - ImageLoader with configs.
     */
    public static ImageLoader getImageLoaderWithConfig() {
        if(imageLoader == null){
            imageLoader = ImageLoader.getInstance();
            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getContext())
                    .threadPriority(Thread.NORM_PRIORITY - 2)
                    .denyCacheImageMultipleSizesInMemory()
                    .memoryCache(new WeakMemoryCache())
                    .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                    .tasksProcessingOrder(QueueProcessingType.LIFO)
                    .writeDebugLogs() // Remove for release app
                    .build();

            imageLoader.init(config);
        }
        return imageLoader;
    }


    /**
     * Stores the location of the picture inside the user's shared prefs.
     *
     * @param picture - The current picture info object.
     */
    public static void storeLocation(PictureInfo picture){
        locationclient = new GoogleApiClient.Builder(getContext())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(connectionCallbacks)
                .build();

        setPicture(picture);

        if(!locationclient.isConnected()) {
            locationclient.connect();
        }

    }

    private static void setPicture(PictureInfo mPicture) {
        picture = mPicture;
    }

    private static PictureInfo getPicture(){
        return picture;
    }

    private static com.google.android.gms.location.LocationListener locationListener = new com.google.android.gms.location.LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.i(LOG_TAG, "Location changing...");
        }
    };

    private static GoogleApiClient.ConnectionCallbacks connectionCallbacks = new GoogleApiClient.ConnectionCallbacks() {

        @Override
        public void onConnected(Bundle bundle) {
            Log.i(LOG_TAG, "Location client connected");
            locationrequest = LocationRequest.create();
            locationrequest.setInterval(30 * 1000);
            locationrequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            LocationServices.FusedLocationApi.requestLocationUpdates(locationclient,
                    locationrequest, locationListener);



            if(LocationServices.FusedLocationApi.getLastLocation(locationclient) != null){
                double lat = LocationServices.FusedLocationApi.getLastLocation(locationclient).getLatitude();
                double lng = LocationServices.FusedLocationApi.getLastLocation(locationclient).getLongitude();

                String city = getCityName(lat, lng);

                Log.i(LOG_TAG, "Photo was taken - " + city);

                //save to shard prefs lat long along with picture name.
                //This is not the most ideal way to save the location of an image
                //But Dropbox's new Core Api did not provide sufficient documentation on how
                //to retreive 'photo_info' through the api client, nor does it clearly state how to
                //insert photo_info. In the end, I resorted to this method.
                String sLat = Double.toString(lat);
                String sLng = Double.toString(lng);

                saveToSharedPrefsString(getPicture().getName() + "_lat", sLat); //Storing as string ensures integrity of double.
                saveToSharedPrefsString(getPicture().getName() + "_lng", sLng);
                saveToSharedPrefsString(getPicture().getName() + "_city", city);

                getPicture().setLat(sLat);
                getPicture().setLng(sLng);
                getPicture().setCity(city);
            }
        }


        @Override
        public void onConnectionSuspended(int i) {

        }
    };


    /**
     * Converts drawable to byte array.
     *
     * @param d - the drawable to be converted.
     * @return - the byte array version of the drawable.
     */
    public static byte[] toByteArray(Drawable d){
        Bitmap bitmap = ((BitmapDrawable)d).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }

    /**
     * Converts byte array to drawable.
     *
     * @param b - the byte array to be converted.
     * @return - the drawable version of the byte array
     */
    public static Drawable toDrawawble(byte[] b){
        return new BitmapDrawable(getContext().getResources(), BitmapFactory.decodeByteArray(b, 0, b.length));
    }

    /**
     * Coverts a drawable to a bitmap. Not a drawable resource.
     *
     * @param background - the drawable background of an imageview
     * @return - the bitmap drawable from the background drawable.
     */
    public static Bitmap toBitmap(Drawable background) {
        return ((BitmapDrawable)background).getBitmap();
    }

    /**
     * Retrieves the city name from lattitude and longitude coordinates.
     *
     * @param lat - lattitude of the location
     * @param lng - longitude of the location
     * @return - the city name.
     */
    public static String getCityName(double lat, double lng){
        Geocoder gcd = new Geocoder(context, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = gcd.getFromLocation(lat, lng, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return (addresses != null) ? addresses.get(0).getLocality() : null;
    }

    /**
     * Gets a marker to add to google maps from the {@link PictureInfo} object
     *
     * @param picture - the picture information
     * @return - the marker options with the picture information added to it
     */
    public static MarkerOptions getMarker(PictureInfo picture) {
        try {
            return new MarkerOptions()
                    .title(picture.getName())
                    .snippet(picture.getCity())
                    .position(new LatLng(Double.parseDouble(picture.getLat()), Double.parseDouble(picture.getLng())));
        }catch (NullPointerException np){
            return null;
        }
    }


    /**
     * Sets padding to a view by density pixels.
     *
     * @param v - view on which the padding will be set
     * @param left - left padding
     * @param top - top padding
     * @param right - right padding
     * @param bottom - bottom padding
     */
    public static void setPadding(View v, int left, int top, int right, int bottom){
        float density = getContext().getResources().getDisplayMetrics().density;
        top = (int)(top * density);
        left = (int)(left * density);
        right = (int)(right * density);
        bottom = (int)(bottom * density);
        v.setPadding(left, top, right, bottom);
    }
}



