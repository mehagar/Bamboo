package com.alabama.bamboofinder;

import android.support.v4.app.Fragment;

/**
 * Created by Michael Walker on 4/9/2015.
 */
public class ObservationListActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() { return new ObservationListFragment(); }
}
