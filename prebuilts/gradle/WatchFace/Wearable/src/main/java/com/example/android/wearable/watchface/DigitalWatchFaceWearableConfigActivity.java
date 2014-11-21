/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.wearable.watchface;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.wearable.view.WearableListView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.Wearable;

/**
 * The watch-side config activity for {@link DigitalWatchFaceService}, which allows for setting the
 * background color.
 */
public class DigitalWatchFaceWearableConfigActivity extends Activity
        implements WearableListView.ClickListener {
    private static final String TAG = "DigitalWatchFaceConfig";

    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_digital_config);

        WearableListView listView = (WearableListView) findViewById(R.id.color_picker);

        listView.setHasFixedSize(true);
        listView.setClickListener(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        listView.setLayoutManager(layoutManager);

        String[] colors = getResources().getStringArray(R.array.color_array);
        listView.setAdapter(new ColorListAdapter(colors));

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle connectionHint) {
                        if (Log.isLoggable(TAG, Log.DEBUG)) {
                            Log.d(TAG, "onConnected: " + connectionHint);
                        }
                    }

                    @Override
                    public void onConnectionSuspended(int cause) {
                        if (Log.isLoggable(TAG, Log.DEBUG)) {
                            Log.d(TAG, "onConnectionSuspended: " + cause);
                        }
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                        if (Log.isLoggable(TAG, Log.DEBUG)) {
                            Log.d(TAG, "onConnectionFailed: " + result);
                        }
                    }
                })
                .addApi(Wearable.API)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override // WearableListView.ClickListener
    public void onClick(WearableListView.ViewHolder viewHolder) {
        ColorItemViewHolder colorItemViewHolder = (ColorItemViewHolder) viewHolder;
        String colorName = colorItemViewHolder.mLabel.getText().toString();
        updateConfigDataItem(Color.parseColor(colorName));
        finish();
    }

    private void updateConfigDataItem(final int backgroundColor) {
        DataMap configKeysToOverwrite = new DataMap();
        configKeysToOverwrite.putInt(DigitalWatchFaceUtil.KEY_BACKGROUND_COLOR,
                backgroundColor);
        DigitalWatchFaceUtil.overwriteKeysInConfigDataMap(mGoogleApiClient, configKeysToOverwrite);
    }

    @Override // WearableListView.ClickListener
    public void onTopEmptyRegionClick() { }

    private class ColorListAdapter extends RecyclerView.Adapter<ColorItemViewHolder> {

        private final String[] mColors;

        public ColorListAdapter(String[] colors) {
            mColors = colors;
        }

        @Override
        public ColorItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View root = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.color_picker_item, parent, false /* attachToRoot */);
            TextView label = (TextView) root.findViewById(R.id.label);
            return new ColorItemViewHolder(root, label);
        }

        @Override
        public void onBindViewHolder(ColorItemViewHolder holder, int position) {
            String colorName = mColors[position];
            holder.mLabel.setText(colorName);

            RecyclerView.LayoutParams layoutParams =
                    new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
            int colorPickerItemMargin = (int) getResources()
                    .getDimension(R.dimen.digital_config_color_picker_item_margin);
            // Add margins to first and last item to make it possible for user to tap on them.
            if (position == 0) {
                layoutParams.setMargins(0, colorPickerItemMargin, 0, 0);
            } else if (position == mColors.length - 1) {
                layoutParams.setMargins(0, 0, 0, colorPickerItemMargin);
            } else {
                layoutParams.setMargins(0, 0, 0, 0);
            }
            holder.itemView.setLayoutParams(layoutParams);
        }

        @Override
        public int getItemCount() {
            return mColors.length;
        }
    }

    private static class ColorItemViewHolder extends WearableListView.ViewHolder {
        private final TextView mLabel;

        public ColorItemViewHolder(View root, TextView label) {
            super(root);
            this.mLabel = label;
        }
    }
}
