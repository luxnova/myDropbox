package mobiquity.com.mydropox.activities;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.GridView;
import android.widget.ImageView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import mobiquity.com.mydropox.adapters.GridViewPictureAdapter;
import mobiquity.com.mydropox.R;
import mobiquity.com.mydropox.models.PictureInfo;
import mobiquity.com.mydropox.utils.DownloadPictures;
import mobiquity.com.mydropox.utils.UploadPicture;
import mobiquity.com.mydropox.utils.Utils;

/**
 * Created by Joshua Williams
 *
 * This activity shows the user's dropbox /Photos entries and the map at where the photos were taken.
 *
 * @version 1.0
 */

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = "MainActivity";
    private String mCameraFileName;
    private static final int NEW_PICTURE = 1;
    public static GridViewPictureAdapter adapter;
    private int activityLoaded;
    private GoogleMap map;
    private View mapView;
    private ImageView actionButton;
    private GridView gridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Loads picutre with imageloader since the icon is huge.
        Utils.getImageLoaderWithConfig().displayImage("drawable://" + R.drawable.drop_icon_white, (ImageView) findViewById(R.id.icon));

        //Checks if the activity is loaded already
        if(activityLoaded != 1){
            setUpActivity();
            Utils.setStatusBarColor(this, R.color.dark_blue);
            setUpEnterAnimations();
        }

    }

    /**
     * Sets up the activity and finds views
     */
    private void setUpActivity() {

        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
                .getMap();

        actionButton = (ImageView) findViewById(R.id.action_button);
        Utils.setPadding(actionButton, 22 ,22 ,22, 22);
        actionButton.setImageResource(R.drawable.white_photo_icon);
        actionButton.bringToFront();

        mapView = findViewById(R.id.map);
        mapView.setVisibility(View.INVISIBLE);
        gridView = (GridView) findViewById(R.id.gridView);

        loadData();
    }

    @Override
    public void onResume(){
        super.onResume();
        Utils.checkContext(getApplicationContext());
        Log.i(LOG_TAG, "onResume");
    }

    /**
     * Sets up the enter animations for the activity. Postpones the transition from the previous activity until the views
     * on this activity has been set up. More specifically the statusbar. This ensures that the status bar does not
     * flicker when the shared elements are transitioning the their position in this activity's layout.
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setUpEnterAnimations() {
        // Postpone the transition until the window's decor view has
        // finished its layout.
        postponeEnterTransition();
        final View decor = getWindow().getDecorView();
        decor.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                decor.getViewTreeObserver().removeOnPreDrawListener(this);
                startPostponedEnterTransition();
                return true;
            }
        });

    }

    /**
     * OnClick Listener method for the floating action button. Allows the user to take a picture.
     *
     * @param v - View that the method is set on.
     */
    public void takePictureListener(View v){
        if(gridView.getTag() == 1){
            loadData();
        }
        else if(gridView.getTag() == 0){
            capturePicture();

        }
    }

    /**
     * Loads dropbox pictures and populates the gridview.
     */
    private void loadData() {
        //Makes call to download pictures before initializing the adapter. Adapter gets initialized in post execute.
        DownloadPictures download = new DownloadPictures(this,
                getString(R.string.photo_dir), gridView);
        download.execute();
    }

    /**
     * Sends implicit intent to capture a picture on the user's phone.
     */
    private void capturePicture(){
        Intent intent = new Intent();
        // Picture from camera
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);

        // This is not the right way to do this, but for some reason, having
        // it store it in
        // MediaStore.Images.Media.EXTERNAL_CONTENT_URI isn't working right.

        Date date = new Date();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd-kk-mm-ss", Locale.US);

        String newPicFile = df.format(date) + ".jpg";
        String outPath = new File(Environment.getExternalStorageDirectory(), newPicFile).getPath();
        File outFile = new File(outPath);

        mCameraFileName = outFile.toString();
        Uri outuri = Uri.fromFile(outFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outuri);
        Log.i(LOG_TAG, "Importing New Picture: " + mCameraFileName);
        try {
            startActivityForResult(intent, NEW_PICTURE);
        } catch (ActivityNotFoundException e) {
            Utils.showToast("There doesn't seem to be a camera.");
        }
    }
    /**
     * Overriden backpressed callback method to return user to their home screen rather than
     * the Splash screen.
     */
    @Override
    public void onBackPressed(){
        if(mapView.getVisibility() == View.VISIBLE){
            Animator anim = Utils.getAnimRevealHide(mapView, 400, mapView.getLeft(), mapView.getTop()); //Top left of the view.
            anim.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                    actionButton.bringToFront();
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    mapView.setVisibility(View.INVISIBLE); //Sets view as visible at the end of animation
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
            anim.setStartDelay(100); //Delay animation so it does not lag.
            anim.start();
            return;
        }

        //Returns user to their respective home screen
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }

    /**
     * Saves the file name to the bundle when the user leaves the application so that
     * it can be easily restored when the user returns to the application.
     *
     * @param outState - the current data of the application.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("mCameraFileName", mCameraFileName);
        super.onSaveInstanceState(outState);
    }

    // This is what gets called on finishing a media piece to import
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == NEW_PICTURE) {
            // return from file upload
            if (resultCode == Activity.RESULT_OK) {
                Uri uri = null;
                if (data != null) {
                    uri = data.getData();
                }
                if (uri == null && mCameraFileName != null) {
                    uri = Uri.fromFile(new File(mCameraFileName));
                }
                File file = new File(mCameraFileName);

                if (uri != null) {
                    UploadPicture upload = new UploadPicture(this, getString(R.string.photo_dir), file);
                    upload.execute();
                }
            } else {
                Log.w(LOG_TAG, "Unknown Activity Result from mediaImport: "
                        + resultCode);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.log_out) {
            if(mapView.getVisibility() == View.VISIBLE){
                onBackPressed(); //Get rid of map so log out animation can be smooth.
            }
            Utils.logUserOut();
            View title = findViewById(R.id.title_text);
            View button = findViewById(R.id.action_button);
            View bgView = findViewById(R.id.bg_view);
            View icon = findViewById(R.id.icon);

            final Intent mainIntent = new Intent(getApplicationContext(), LoginActivity.class);

            final ActivityOptions transitionActivityOptions = ActivityOptions.makeSceneTransitionAnimation(this,
                    Pair.create(title, title.getTransitionName()),
                    Pair.create(bgView, bgView.getTransitionName()),
                    Pair.create(icon, icon.getTransitionName()),
                    Pair.create(button, button.getTransitionName()));

            startActivity(mainIntent, transitionActivityOptions.toBundle());
            return true;
        }
        else if(id == R.id.showOnMap){
            map.clear();
            mapView.bringToFront();
            //Animate the mapview, not the map.
            Animator anim = Utils.getAnimRevealShow(mapView, 400, mapView.getLeft(), mapView.getTop());
            anim.start();

            List<PictureInfo> pictures = adapter.getDataSet();

            if(pictures == null){
                Utils.showToast(getString(R.string.check_connection));
                return false;
            }

            //Every picture that has a location will be shown on the map.
            for(PictureInfo picture : pictures){
                MarkerOptions markerOptions = Utils.getMarker(picture);
                if(markerOptions != null){
                    map.addMarker(Utils.getMarker(picture));
                }
            }

            map.getUiSettings().setCompassEnabled(false);
            map.getUiSettings().setIndoorLevelPickerEnabled(false);
            map.getUiSettings().setMyLocationButtonEnabled(false);
            //Animate camera to the first position.
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.parseDouble(pictures.get(0).getLat()),
                    Double.parseDouble(pictures.get(0).getLng())), 10));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
