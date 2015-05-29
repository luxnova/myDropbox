package mobiquity.com.mydropox.activities;


import android.animation.Animator;
import android.annotation.TargetApi;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
import com.facebook.FacebookSdk;
import mobiquity.com.mydropox.R;
import mobiquity.com.mydropox.utils.Utils;

/**
 *
 * This activity shows the logo and some animation that leads to the login screen.
 *
 * @author Joshua Williams
 * @version 1.0
 */
public class SplashActivity extends AppCompatActivity {

    private static final long SHOW_SPLASH_LENGTH = 1600; //milliseconds


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        FacebookSdk.sdkInitialize(this);
        Utils.checkContext(this);
        Utils.getImageLoaderWithConfig().displayImage("drawable://" + R.drawable.drop_icon_white, (ImageView) findViewById(R.id.icon));
        Utils.setStatusBarColor(this, R.color.white);
        startSplashTimer();

    }

    /**
     * Starts the handler that shows the Splash screen. Also sets up the Android-L animations if the
     * user is running 5.0.
     */
    private void startSplashTimer() {
        new Handler().postDelayed(new Runnable() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void run() {
                final Intent mainIntent;
                if (Utils.checkIfUserLoggedIn()) {
                    mainIntent = new Intent(getApplicationContext(), MainActivity.class);
                } else {
                    mainIntent = new Intent(SplashActivity.this, LoginActivity.class);
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {


                    //Sharing all of the views to create a seamless, smooth transition to the next activity.
                    View title = findViewById(R.id.splash_title);
                    View button = findViewById(R.id.login_button);
                    View bgView = findViewById(R.id.bg_view);
                    View statusBar = findViewById(android.R.id.statusBarBackground);
                    View icon = findViewById(R.id.icon);


                    final ActivityOptions transitionActivityOptions = ActivityOptions.makeSceneTransitionAnimation(SplashActivity.this,
                            Pair.create(title, title.getTransitionName()),
                            Pair.create(bgView, bgView.getTransitionName()),
                            Pair.create(icon, icon.getTransitionName()),
                            Pair.create(statusBar, statusBar.getTransitionName()),
                            Pair.create(button, button.getTransitionName()));

                    //creates circular reveal animation and returns the animator object
                    Animator anim = Utils.getAnimRevealShow(bgView, 400, bgView.getWidth() / 2, bgView.getHeight() / 2);
                    anim.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animator) {
                            Utils.setStatusBarColor(SplashActivity.this, R.color.blue);
                        }

                        @Override
                        public void onAnimationEnd(Animator animator) {
                            startActivity(mainIntent, transitionActivityOptions.toBundle());
                        }

                        @Override
                        public void onAnimationCancel(Animator animator) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animator) {

                        }
                    });

                    anim.start();


                } else {
                    startActivity(mainIntent);
                    overridePendingTransition(R.anim.abc_slide_in_bottom, R.anim.abc_shrink_fade_out_from_bottom);
                }
            }
        }, SHOW_SPLASH_LENGTH);
    }

}
