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

import android.content.ContentUris;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.provider.WearableCalendarContract;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.DynamicLayout;
import android.text.Editable;
import android.text.Html;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.SurfaceHolder;

import java.util.concurrent.TimeUnit;

/**
 * Proof of concept sample watch face that demonstrates how a watch face can load calendar data.
 */
public class CalendarWatchFaceService extends CanvasWatchFaceService {
    private static final String TAG = "CalendarWatchFace";

    /**
     * How long to wait after loading meetings before loading them again. Meetings are only loaded
     * in interactive mode.
     */
    private static final long LOAD_MEETINGS_DELAY_MS = TimeUnit.MINUTES.toMillis(1);

    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    private class Engine extends CanvasWatchFaceService.Engine {

        static final int BACKGROUND_COLOR = Color.BLACK;
        static final int FOREGROUND_COLOR = Color.WHITE;
        static final int TEXT_SIZE = 25;
        static final int MSG_LOAD_MEETINGS = 0;

        /** Editable string containing the text to draw with the number of meetings in bold. */
        final Editable mEditable = new SpannableStringBuilder();

        /** Width specified when {@link #mLayout} was created. */
        int mLayoutWidth;

        /** Layout to wrap {@link #mEditable} onto multiple lines. */
        DynamicLayout mLayout;

        /** Paint used to draw text. */
        final TextPaint mTextPaint = new TextPaint();

        int mNumMeetings;

        private AsyncTask<Void, Void, Integer> mLoadMeetingsTask;

        /** Handler to load the meetings once a minute in interactive mode. */
        final Handler mLoadMeetingsHandler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case MSG_LOAD_MEETINGS:
                        cancelLoadMeetingTask();
                        mLoadMeetingsTask = new LoadMeetingsTask();
                        mLoadMeetingsTask.execute();
                        break;
                }
            }
        };

        @Override
        public void onCreate(SurfaceHolder holder) {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "onCreate");
            }
            super.onCreate(holder);
            setWatchFaceStyle(new WatchFaceStyle.Builder(CalendarWatchFaceService.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_VARIABLE)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setShowSystemUiTime(false)
                    .build());

            mTextPaint.setColor(FOREGROUND_COLOR);
            mTextPaint.setTextSize(TEXT_SIZE);
        }

        @Override
        public void onDestroy() {
            mLoadMeetingsHandler.removeMessages(MSG_LOAD_MEETINGS);
            cancelLoadMeetingTask();
            super.onDestroy();
        }

        @Override
        public void onDraw(Canvas canvas) {
            // Create or update mLayout if necessary.
            if (mLayout == null || mLayoutWidth != canvas.getWidth()) {
                mLayoutWidth = canvas.getWidth();
                mLayout = new DynamicLayout(mEditable, mTextPaint, mLayoutWidth,
                        Layout.Alignment.ALIGN_NORMAL, 1 /* spacingMult */, 0 /* spacingAdd */,
                        false /* includePad */);
            }

            // Update the contents of mEditable.
            mEditable.clear();
            mEditable.append(Html.fromHtml(getResources().getQuantityString(
                    R.plurals.calendar_meetings, mNumMeetings, mNumMeetings)));

            // Draw the text on a solid background.
            canvas.drawColor(BACKGROUND_COLOR);
            mLayout.draw(canvas);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            if (visible) {
                mLoadMeetingsHandler.sendEmptyMessage(MSG_LOAD_MEETINGS);
            } else {
                mLoadMeetingsHandler.removeMessages(MSG_LOAD_MEETINGS);
                cancelLoadMeetingTask();
            }
        }

        private void onMeetingsLoaded(Integer result) {
            if (result != null) {
                mNumMeetings = result;
                invalidate();
            }
            if (isVisible()) {
                mLoadMeetingsHandler.sendEmptyMessageDelayed(
                        MSG_LOAD_MEETINGS, LOAD_MEETINGS_DELAY_MS);
            }
        }

        private void cancelLoadMeetingTask() {
            if (mLoadMeetingsTask != null) {
                mLoadMeetingsTask.cancel(true);
            }
        }

        /**
         * Asynchronous task to load the meetings from the content provider and report the number of
         * meetings back via {@link #onMeetingsLoaded}.
         */
        private class LoadMeetingsTask extends AsyncTask<Void, Void, Integer> {
            @Override
            protected Integer doInBackground(Void... voids) {
                long begin = System.currentTimeMillis();
                Uri.Builder builder =
                        WearableCalendarContract.Instances.CONTENT_URI.buildUpon();
                ContentUris.appendId(builder, begin);
                ContentUris.appendId(builder, begin + DateUtils.DAY_IN_MILLIS);
                final Cursor cursor = getContentResolver() .query(builder.build(),
                        null, null, null, null);
                int numMeetings = cursor.getCount();
                if (Log.isLoggable(TAG, Log.VERBOSE)) {
                    Log.v(TAG, "Num meetings: " + numMeetings);
                }
                return numMeetings;
            }

            @Override
            protected void onPostExecute(Integer result) {
                onMeetingsLoaded(result);
            }
        }
    }
}
