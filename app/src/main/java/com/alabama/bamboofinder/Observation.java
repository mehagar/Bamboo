package com.alabama.bamboofinder;

import android.location.Location;
import android.media.Image;

import java.util.Date;

/**
 * Created by Michael H. on 3/19/2015.
 */

public class Observation {

    private Date mTimeStamp;
    private String mDescription;
    private boolean mValidated;
    private String mOwnerUserName;
    private Image mImage;
    private Location mLocation;

    public Location getLocation() {
        return mLocation;
    }

    public void setLocation(Location location) {
        this.mLocation = location;
    }

    public Date getTimeStamp() {
        return mTimeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.mTimeStamp = timeStamp;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        this.mDescription = description;
    }

    public boolean isValidated() {
        return mValidated;
    }

    public void setValidated(boolean validated) {
        this.mValidated = validated;
    }

    public String getOwnerUserName() {
        return mOwnerUserName;
    }

    public void setOwnerUserName(String ownerUserName) {
        this.mOwnerUserName = ownerUserName;
    }

    public Image getImage() {
        return mImage;
    }

    public void setImage(Image image) {
        this.mImage = image;
    }
}