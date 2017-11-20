package com.volynski.familytrack.viewmodels;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;

import com.volynski.familytrack.data.FamilyTrackDataSource;
import com.volynski.familytrack.data.models.firebase.User;
import com.volynski.familytrack.views.navigators.UserListNavigator;

/**
 * Created by DmitryVolynski on 06.11.2017.
 */

public abstract class AbstractViewModel extends BaseObservable {
    public final ObservableField<String> snackbarText = new ObservableField<>();
    public final ObservableField<String> title = new ObservableField<>();
    public final ObservableField<String> description = new ObservableField<>();
    public final ObservableBoolean isDataLoading = new ObservableBoolean(false);

    private boolean mCreatedFromViewHolder = false;

    Context mContext;
    FamilyTrackDataSource mRepository;
    String mCurrentUserUuid = "";
    User mCurrentUser;


    public AbstractViewModel(Context context,
                             String currentUserUuid,
                             FamilyTrackDataSource dataSource) {
        mContext = context.getApplicationContext();
        mCurrentUserUuid = currentUserUuid;
        mRepository = dataSource;
    }

    public boolean isCreatedFromViewHolder() {
        return mCreatedFromViewHolder;
    }

    public void setCreatedFromViewHolder(boolean createdFromViewHolder) {
        this.mCreatedFromViewHolder = createdFromViewHolder;
    }
}
