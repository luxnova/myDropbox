/*
 * Copyright (c) 2010-11 Dropbox, Inc.
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */


package mobiquity.com.mydropox.utils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.DropboxAPI.ThumbFormat;
import com.dropbox.client2.DropboxAPI.ThumbSize;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxIOException;
import com.dropbox.client2.exception.DropboxParseException;
import com.dropbox.client2.exception.DropboxPartialFileException;
import com.dropbox.client2.exception.DropboxServerException;
import com.dropbox.client2.exception.DropboxUnlinkedException;
import mobiquity.com.mydropox.adapters.GridViewPictureAdapter;
import mobiquity.com.mydropox.R;
import mobiquity.com.mydropox.activities.MainActivity;
import mobiquity.com.mydropox.models.PictureInfo;

/**
 * Here we show getting metadata for a directory and downloading a file in a
 * background thread, trying to show typical exception handling and flow of
 * control for an app that downloads a file from Dropbox.
 */

public class DownloadPictures extends AsyncTask<Void, Long, Boolean> {


    private final ProgressBar progressBar;
    private final RelativeLayout parent;
    private Activity mContext;
    private DropboxAPI<?> mApi;
    private String mPath;
    private FileOutputStream mFos;
    private ArrayList<PictureInfo> thumbs = new ArrayList<>();
    private GridView gridView;

    private boolean mCanceled;
    private Long mFileLen;
    private String mErrorMsg;


    public DownloadPictures(Activity context, String dropboxPath, GridView gridView) {
        this.gridView = gridView;
        gridView.setTag(2);
        //locates parent of the gridview to find the progress bar
        parent = (RelativeLayout) gridView.getParent();
        progressBar = (ProgressBar) parent.findViewById(R.id.progressbar);
        progressBar.setVisibility(View.VISIBLE);

        // We set the context this way so we don't accidentally leak activities
        mContext = context;

        mApi = Utils.buildSession();
        mPath = dropboxPath;

    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            if (mCanceled) {
                return false;
            }

            // Get the metadata for a directory
            Entry dirent = mApi.metadata(mPath, 1000, null, true, null);

            if (!dirent.isDir || dirent.contents == null) {
                // It's not a directory, or there's nothing in it
                mErrorMsg = "File or empty directory";
                return false;
            }

            // Make a list of everything in it that we can get a thumbnail for
            thumbs = new ArrayList<>();
            for (Entry ent: dirent.contents) {
                mFileLen = ent.bytes;
                PictureInfo pictureInfo = new PictureInfo(downloadPicture(ent), ent.fileName());
                thumbs.add(pictureInfo);
            }

            if (mCanceled) {
                return false;
            }

            if (thumbs.size() == 0) {
                // No thumbs in that directory
                mErrorMsg = "No pictures in that directory";
                return false;
            }

            //sets tag that load was successful.
            gridView.setTag(0);
            // We must have a legitimate picture
            return true;

        } catch (DropboxUnlinkedException e) {
            // The AuthSession wasn't properly authenticated or user unlinked.
        } catch (DropboxPartialFileException e) {
            // We canceled the operation
            mErrorMsg = "Download canceled";
        } catch (DropboxServerException e) {
            // Server-side exception.  These are examples of what could happen,
            // but we don't do anything special with them here.
            if (e.error == DropboxServerException._304_NOT_MODIFIED) {
                // won't happen since we don't pass in revision with metadata
            } else if (e.error == DropboxServerException._401_UNAUTHORIZED) {
                // Unauthorized, so we should unlink them.  You may want to
                // automatically log the user out in this case.
            } else if (e.error == DropboxServerException._403_FORBIDDEN) {
                // Not allowed to access this
            } else if (e.error == DropboxServerException._404_NOT_FOUND) {
                // path not found (or if it was the thumbnail, can't be
                // thumbnailed)
            } else if (e.error == DropboxServerException._406_NOT_ACCEPTABLE) {
                // too many entries to return
            } else if (e.error == DropboxServerException._415_UNSUPPORTED_MEDIA) {
                // can't be thumbnailed
            } else if (e.error == DropboxServerException._507_INSUFFICIENT_STORAGE) {
                // user is over quota
            } else {
                // Something else
            }
            // This gets the Dropbox error, translated into the user's language
            mErrorMsg = e.body.userError;
            if (mErrorMsg == null) {
                mErrorMsg = e.body.error;
            }
        } catch (DropboxIOException e) {
            // Happens all the time, probably want to retry automatically.
            mErrorMsg = "Network error.  Try again.";
        } catch (DropboxParseException e) {
            // Probably due to Dropbox server restarting, should retry
            mErrorMsg = "Dropbox error.  Try again.";
        } catch (DropboxException e) {
            // Unknown error
            mErrorMsg = "Unknown error.  Try again.";
        }

        //sets tag load was unsuccessful.
        gridView.setTag(1);
        mContext.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Utils.showToast(mErrorMsg);
            }
        });
        return false;
    }

    private Drawable downloadPicture(Entry ent) throws DropboxException {
        String cachePath = mContext.getCacheDir().getAbsolutePath() + "/" + ent.fileName();
        try {
            mFos = new FileOutputStream(cachePath);
        } catch (FileNotFoundException e) {
            mErrorMsg = "Couldn't create a local file to store the image";
            return null;
        }

        // This downloads a smaller, thumbnail version of the file.  The
        // API to download the actual file is roughly the same.
        mApi.getThumbnail(ent.path, mFos, ThumbSize.BESTFIT_960x640,
                ThumbFormat.JPEG, null);


        if (mCanceled) {
            return null;
        }

        return Drawable.createFromPath(cachePath);
    }

    @Override
    protected void onProgressUpdate(Long... progress) {
        int percent = (int)(100.0*(double)progress[0]/mFileLen + 0.5);
    }

    @Override
    protected void onPostExecute(Boolean result) {
        progressBar.setVisibility(View.INVISIBLE);

        if(gridView.getTag() == 1){
            mContext.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ImageView actionButton = (ImageView) parent.findViewById(R.id.action_button);
                    Utils.setPadding(actionButton, 17, 17, 17, 17);
                    actionButton.setImageResource(R.drawable.ic_action_refresh);
                }
            });
        }
        else{
            mContext.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ImageView actionButton = (ImageView) parent.findViewById(R.id.action_button);
                    Utils.setPadding(actionButton, 22, 22, 22, 22);
                    actionButton.setImageResource(R.drawable.white_photo_icon);
                }
            });
        }

        if(thumbs.size() == 0){
            return;
        }

        MainActivity.adapter = new GridViewPictureAdapter(mContext, thumbs);
        gridView.setAdapter(MainActivity.adapter);
        if(!result){
            // Couldn't download it, so show an error
            showToast(mErrorMsg);
        }

    }

    private void showToast(String msg) {
        Toast error = Toast.makeText(mContext, msg, Toast.LENGTH_LONG);
        error.show();
    }


}
