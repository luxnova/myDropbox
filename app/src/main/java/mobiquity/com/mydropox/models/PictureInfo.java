package mobiquity.com.mydropox.models;

import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import mobiquity.com.mydropox.utils.Utils;


/**
 * Created by JoshuaWilliams on 5/28/15.
 *
 * Class that holds the data for the picture of the dropbox folder info.
 *
 * @version 1.0
 */
public class PictureInfo implements Parcelable {

    private Drawable drawable;
    private boolean animated;
    private String name;
    private String lat;
    private String lng;
    private String city;

    /******************************* Constructors ***************************************/
    public PictureInfo(Drawable drawable, String name) {
        setName(name);
        setHasAnimated(false);
        setDrawable(drawable);

        String picName = Utils.getStringFromSharedPrefs(name + "_lat");
        Log.i("Picture Info ", picName);
        if(!picName.isEmpty()){
            Log.i("Picture Info ", "Lat - " + Utils.getStringFromSharedPrefs(name + "_lat"));
            setLat(Utils.getStringFromSharedPrefs(name + "_lat"));
            setLng(Utils.getStringFromSharedPrefs(name + "_lng"));
            setCity(Utils.getStringFromSharedPrefs(name + "_city"));
        }
    }

    // Parcelling Constructor
    public PictureInfo(Parcel in){
        int size = in.readInt();
        byte[] drawableData = new byte[size];

        in.readByteArray(drawableData);
        String bool = in.readString();

        setDrawable(Utils.toDrawawble(drawableData));
        setHasAnimated(Boolean.parseBoolean(bool));

        String[] additionalData = new String[4];
        in.readStringArray(additionalData);

        setName(additionalData[0]);
        setLat(additionalData[1]);
        setLng(additionalData[2]);
        setCity(additionalData[3]);
    }
    /******************************* END *************************************************/

    /******************************* Getters and Setters ***************************************/

    public void setCity(String city) {
        this.city = city;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName(){
        return name;
    }

    public void setDrawable(Drawable drawableID) {
        this.drawable = drawableID;
    }

    public Drawable getDrawable(){
        return drawable;
    }

    public void setHasAnimated(boolean animated) {
        this.animated = animated;
    }

    public boolean hasAnimated(){
        return animated;
    }

    @Override
    public int describeContents() {
        return 0;
    }
    /******************************* END ***************************************/


    /************************************* Parcelable Interface *********************************/
    @Override
    public void writeToParcel(Parcel parcel, int i) {
        byte[] drawableData = Utils.toByteArray(getDrawable());

        parcel.writeInt(drawableData.length);
        parcel.writeByteArray(drawableData);
        parcel.writeString(Boolean.toString(hasAnimated()));
        parcel.writeStringArray(new String[]{getName(), getLat(), getLng(), getCity()});
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public PictureInfo createFromParcel(Parcel in) {
            return new PictureInfo(in);
        }

        public PictureInfo[] newArray(int size) {
            return new PictureInfo[size];
        }
    };

    public String getCity() {
        return city;
    }


    /************************************* Parcelable Interface *********************************/

}
