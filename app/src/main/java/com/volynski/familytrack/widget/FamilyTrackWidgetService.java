package com.volynski.familytrack.widget;

import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.volynski.familytrack.R;
import com.volynski.familytrack.data.models.firebase.Group;
import com.volynski.familytrack.data.models.firebase.User;
import com.volynski.familytrack.utils.SharedPrefsUtil;


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
        RemoteViews view = null;

        if (mGroup == null ||
                mGroup.getActiveMembersForWidget().size() == 0) {
            return null;
        }

        User user = mGroup.getActiveMembersForWidget().get(i);

        if (i == 0) {
            // return view for widget header
            view = new RemoteViews(mContext.getPackageName(), R.layout.widget_header);
        } else {
            // return view for normal item (ingredient or step)
            view = new RemoteViews(mContext.getPackageName(), R.layout.widget_item);
        }

        view.setTextViewText(R.id.widget_item_text, "Test");
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
