package mobiquity.com.mydropox.activities;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareButton;
import com.facebook.share.widget.ShareDialog;
import mobiquity.com.mydropox.R;
import mobiquity.com.mydropox.models.PictureInfo;
import mobiquity.com.mydropox.utils.Utils;

/**
 * Created by Joshua Williams
 *
 * This activity allows the user to view a file from the Grid View.
 * @version  1.0
 */
public class ViewFileActivity extends AppCompatActivity {
    private static final String LOG_TAG = "ViewFileActivity";
    private CallbackManager callbackManager;
    private ShareDialog shareDialog;
    private ShareButton shareButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_file);

        Bundle data = getIntent().getExtras();
        PictureInfo picture = data.getParcelable("picture");

        setUpActivity(picture);
        setUpEnterAnimations();
    }


    /**
     * Sets up activity based on the Picture Info Object
     */
    private void setUpActivity(PictureInfo picture) {
        ImageView image = (ImageView) findViewById(R.id.picture);
        image.setImageDrawable(picture.getDrawable());

        TextView locationTV = (TextView) findViewById(R.id.location);
        if(picture.getCity() == null){
            Log.i(LOG_TAG, "User's location was off during picture capture.");
            locationTV.setVisibility(View.GONE);
        }
        locationTV.setText("Taken in " + picture.getCity());

        shareButton = (ShareButton) findViewById(R.id.shareButton);
        shareButton.setTag(picture);

        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(this);
        shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
            @Override
            public void onSuccess(Sharer.Result result) {
                Log.i(LOG_TAG, "Share successful");
            }

            @Override
            public void onCancel() {
                Log.i(LOG_TAG, "Share cancelled");
            }

            @Override
            public void onError(FacebookException e) {
                e.printStackTrace();
                Utils.showToast(getString(R.string.something_went_wrong));
            }
        });

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


    // Receives the result from the share activity.
    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }


    /**
     * On click listener method for share button.
     * Allows users to share photo to facebook with friends.
     *
     * @param v - view on which the method will act on.
     */
    public void sharePicture(View v){
        PictureInfo picture = (PictureInfo) v.getTag();

        Bitmap image = Utils.toBitmap(picture.getDrawable());
        SharePhoto photo = new SharePhoto.Builder()
                .setBitmap(image)
                .build();
        SharePhotoContent content = new SharePhotoContent.Builder()
                .addPhoto(photo)
                .build();

        shareButton.setShareContent(content);

        if (ShareDialog.canShow(ShareLinkContent.class)) {
            ShareLinkContent linkContent = new ShareLinkContent.Builder()
                    .setContentTitle("Check out this cool photo")
                    .setContentUrl(Uri.parse("http://developers.facebook.com/android"))
                    .build();
            shareDialog.show(linkContent);
        }
    }
}
