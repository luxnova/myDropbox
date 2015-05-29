package mobiquity.com.mydropox.adapters;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import java.util.ArrayList;
import java.util.List;
import mobiquity.com.mydropox.R;
import mobiquity.com.mydropox.activities.ViewFileActivity;
import mobiquity.com.mydropox.models.PictureInfo;
import mobiquity.com.mydropox.utils.Utils;


/**
 * This adapter goes for the grid view in the main activity on which the pictures are being shown on.
 *
 * Created by JoshuaWilliams on 5/27/15.
 *
 * @version 1.0
 */
public class GridViewPictureAdapter extends BaseAdapter implements View.OnClickListener {
    private final String LOG_TAG = "GridViewPictureAdapter";
    List<PictureInfo> pictures = new ArrayList<>();
    Activity context;


    public GridViewPictureAdapter(Activity context, ArrayList<PictureInfo> pictures){
        this.pictures = pictures;
        this.context = context;
    }


    @Override
    public int getCount() {
        return pictures.size();
    }


    @Override
    public PictureInfo getItem(int i) {
        return pictures.get(i);
    }

    /**
     * Adds a {@link PictureInfo} to the dataset and notifies the adapter of the change.
     *
     * @param pictureInfo - the picture along with it's information.
     */
    public void addToDataset(PictureInfo pictureInfo){
        pictures.add(pictureInfo);
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged(); //Must be ran on the ui thread
            }
        });
    }


    /**
     * Returns the data set of the gridview adapter.
     *
     * @return - the data associated with this gridview adapter.
     */
    public List<PictureInfo> getDataSet(){
        return pictures;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        ViewHolder viewHolder;

        if (view == null) {
            LayoutInflater inflater;
            viewHolder = new ViewHolder();

            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.item, viewGroup, false);

            viewHolder.image = (ImageView) view.findViewById(R.id.picture);

            view.setTag(viewHolder);
            viewHolder = (ViewHolder) view.getTag();

        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        PictureInfo picture = getItem(i);
        viewHolder.image.setImageDrawable(picture.getDrawable());

        //Checks if the picture view has animated. Basically checks if its a new picture or not.
        if(!picture.hasAnimated()){
            Animation scaleAnim = Utils.createScaleAnimation();
            viewHolder.image.startAnimation(scaleAnim);
            picture.setHasAnimated(true);
        }
        viewHolder.image.setOnClickListener(this);
        viewHolder.image.setTag(picture);
        return view;
    }


    @Override
    public void onClick(View v) {
        final Intent mainIntent = new Intent(context.getApplicationContext(), ViewFileActivity.class);
        mainIntent.putExtra("picture", (PictureInfo) v.getTag());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //Start the activity with the picture object inside the bundle
            final ActivityOptions transitionActivityOptions = ActivityOptions.makeSceneTransitionAnimation(context,
                    Pair.create(v, v.getTransitionName()));
            context.startActivity(mainIntent, transitionActivityOptions.toBundle());

        } else {
            context.startActivity(mainIntent);
            context.overridePendingTransition(R.anim.abc_slide_in_bottom, R.anim.abc_shrink_fade_out_from_bottom);
        }
    }


    /**
     * View holder class so that the views do always get recycled.
     */
    class ViewHolder {
        ImageView image;
    }

}
