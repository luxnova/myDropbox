package mobiquity.com.mydropox.activities;

import android.annotation.TargetApi;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.ViewTreeObserver;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import mobiquity.com.mydropox.R;
import mobiquity.com.mydropox.utils.Utils;

/**
 *
 * This activity allows the user to login to their Dropbox account.
 *
 * @author Joshua Williams
 * @version 1.0
 */

public class LoginActivity extends AppCompatActivity {
    private static final String LOG_TAG = "LoginActivity";

    private DropboxAPI<AndroidAuthSession> mApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Utils.setStatusBarColor(this, R.color.blue);
        setUpEnterAnimations();

        mApi = Utils.buildSession();
        setLoggedIn(Utils.checkIfUserLoggedIn());
    }

    /**
     * Log in button onclick listener. Logs the user in.
     */
    public void loginButtonOnClick(View v){
        Log.i(LOG_TAG, "User logging in.");
        // Start the remote authentication
        mApi.getSession().startOAuth2Authentication(LoginActivity.this);
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
     * Overriden backpressed callback method to return user to their home screen rather than
     * the Splash screen.
     */
    @Override
    public void onBackPressed(){
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }


    @Override
    protected void onResume() {
        super.onResume();

        AndroidAuthSession session = mApi.getSession();
        // The next part must be inserted in the onResume() method of the
        // activity from which session.startAuthentication() was called, so
        // that Dropbox authentication completes properly.
        if (session.authenticationSuccessful()) {
            try {
                // Mandatory call to complete the auth
                session.finishAuthentication();

                // Store it locally in our app for later use
                Utils.storeAuth(session);
                setLoggedIn(true);

            } catch (IllegalStateException e) {
                Utils.showToast("Couldn't authenticate with Dropbox:" + e.getLocalizedMessage());
                Log.i(LOG_TAG, "Error authenticating", e);
            }
        }
    }

    /**
     * If the user is logged in then start the Main Activity.
     *
     * @param loggedIn - whether the user is logged in.
     */
    public void setLoggedIn(boolean loggedIn) {
        Log.i(LOG_TAG, "Session built - is user logged in? - " + loggedIn);
        if (loggedIn) {
            final Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                //Sharing all of the views to create a seamless, smooth transition to the next activity.
                View title = findViewById(R.id.splash_title);
                View button = findViewById(R.id.login_button);
                View bgView = findViewById(R.id.bg_view);
                View icon = findViewById(R.id.icon);

                final ActivityOptions transitionActivityOptions = ActivityOptions.makeSceneTransitionAnimation(this,
                        Pair.create(title, title.getTransitionName()),
                        Pair.create(bgView, bgView.getTransitionName()),
                        Pair.create(icon, icon.getTransitionName()),
                        Pair.create(button, button.getTransitionName()));

                startActivity(mainIntent, transitionActivityOptions.toBundle());
            } else {
                startActivity(mainIntent);
                overridePendingTransition(R.anim.abc_slide_in_bottom, R.anim.abc_shrink_fade_out_from_bottom);
            }
        }
    }

}
