package com.volynski.familytrack.widget;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.squareup.picasso.Picasso;
import com.volynski.familytrack.R;
import com.volynski.familytrack.data.models.firebase.Group;
import com.volynski.familytrack.data.models.firebase.Membership;
import com.volynski.familytrack.data.models.firebase.User;
import com.volynski.familytrack.utils.SharedPrefsUtil;

import java.io.IOException;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import timber.log.Timber;


/**
 * Created by DmitryVolynski on 03.11.2017.
 */

public class FamilyTrackWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new FamilyTrackRemoteViewsFactory(this.getApplicationContext());
    }
}

class FamilyTrackRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private Context mContext;
    private Group mGroup;

    public FamilyTrackRemoteViewsFactory(Context context) {
        mContext = context;
    }

    @Override
    public void onCreate() {
    }

    @Override
    public int getCount() {
        if (mGroup == null) return 0;
        return  mGroup.getActiveMembersForWidget().size();
    }

    @Override
    public void onDataSetChanged() {
        mGroup = SharedPrefsUtil.getActiveGroup(mContext);
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public RemoteViews getViewAt(int i) {
        Bitmap photo = BitmapFactory.decodeResource(mContext.getResources(),
                R.mipmap.ic_no_user_photo);

        RemoteViews view = null;

        if (mGroup == null ||
                mGroup.getActiveMembersForWidget().size() == 0) {
            return null;
        }

        if (i == 0) {
            // return view for widget header
            view = new RemoteViews(mContext.getPackageName(), R.layout.widget_header);
            view.setTextViewText(R.id.widget_item_text,
                    "FamilyTrack/[" + mGroup.getName() + "]");
        } else {
            // return view for normal item (group user)
            User user = mGroup.getActiveMembersForWidget().get(i);
            view = new RemoteViews(mContext.getPackageName(),
                    R.layout.widget_list_item);
            view.setTextViewText(R.id.textview_widgetlistitem_username,
                    user.getDisplayName());

            String locationName = (user.getLastKnownLocation() == null ?
                    "Location unknown" : user.getLastKnownLocation().getKnownLocationName());
            view.setTextViewText(R.id.textview_widgetlistitem_location, locationName);

            int roleId = (user.getActiveMembership() != null ?
                    user.getActiveMembership().getRoleId() : Membership.ROLE_UNDEFINED);
            int resId = mContext.getResources().getIdentifier("ROLE_" + roleId,
                    "string", mContext.getPackageName());

            String lastKnownTime = (user.getLastKnownLocation() == null ?
                    "Time unknown" : user.getLastKnownLocation().getPeriodAsString());
            view.setTextViewText(R.id.textview_widgetlistitem_time, lastKnownTime);

            if (!user.getPhotoUrl().equals("")) {
                try {
                    photo = Picasso.with(mContext)
                            .load(user.getPhotoUrl())
                            .transform(new CropCircleTransformation())
                            .get();
                } catch (IOException ex) {
                    Timber.e(ex);
                }
            }
            view.setImageViewBitmap(R.id.imageview_widgetlistitem_photo, photo);
        }

        return view;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
