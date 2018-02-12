package axiom.com.audiobook.activities;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Locale;

import axiom.com.audiobook.AudioPlayerService;
import axiom.com.audiobook.BlurBuilder;
import axiom.com.audiobook.R;
import axiom.com.audiobook.adapter.PlayListAdapter;
import axiom.com.audiobook.data.Book;
import axiom.com.audiobook.data.Chapter;
import axiom.com.audiobook.data.ChapterContract.ChapterEntry;
import axiom.com.audiobook.data.ChapterDbHelper;
import axiom.com.audiobook.data.Constants;
import axiom.com.audiobook.data.StorageUtils;
import butterknife.BindView;
import butterknife.ButterKnife;

public class AudioPlayerActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {
    @BindView(R.id.playlist_view)
    ListView mChapterListView;
    private ArrayList<String> mChapterPathList;
    @BindView(R.id.pause_button)
    ImageView mPauseButton;
    @BindView(R.id.play_button)
    ImageView mPlayButton;
    @BindView(R.id.replay_30_view)
    ImageView mRewind30;
    @BindView(R.id.forward_30_view)
    ImageView mForward30;
    @BindView(R.id.stop_button)
    ImageView mStopButton;
    @BindView(R.id.current_chapter_name)
    TextView mCurrentChapterName;
    @BindView(R.id.end_point)
    TextView mEndPointTime;
    @BindView(R.id.start_point)
    TextView mStartPointTime;
    @BindView(R.id.progress_seekbar)
    SeekBar mSeekBar;

    private Book mBook;
    private AudioPlayerService mPlayerService;
    private boolean mServiceBound = false;
    private long mLastSeekEventTime = 0;
    boolean mmFromTouch = false;

    private StringBuilder mFormatBuilder = new StringBuilder();
    private Formatter mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
    private Object[] mTimeArgs;
    private long mChapterDuration;

    private ArrayList<Chapter> mChapterList;
    private ArrayList<String> mChapterNameList;
    private int mCurrentChapterIndex;

    private StorageUtils mStorageUtils;

    private final Handler mHandler = new Handler();
    private PlayerServiceBroadcastReceiver mPlayerBroadcastReceiver;
    View mPrevSelectedItem = null;
    View mCurrentSelectedItem = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.audio_player_layout);
        ButterKnife.bind(this);
        if (getResources().getBoolean(R.bool.portrait_only)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        mBook = getIntent().getParcelableExtra("book_obj");
        setBackgroundImage(mBook);
        setTitle(mBook.getTitle());

        mStartPointTime.setText("0:00");

        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
        mTimeArgs = new Object[5];

        mStorageUtils = new StorageUtils(this);

        String chapterDirPath = mBook.getAbsolutePath();
        mChapterPathList = new ArrayList<>();
        File chapterDir = new File(chapterDirPath);
        if (chapterDir.isDirectory()) {
            String[] children = chapterDir.list();
            for (int i = 0; i < children.length; i++) {
                mChapterPathList.add(chapterDirPath + "/" + children[i]);
            }
        }

        mChapterList = new ArrayList<>();
        mChapterNameList = new ArrayList<>();
        ChapterDbHelper dbHelper = new ChapterDbHelper(this);
        dbHelper.getReadableDatabase();
        String selection = ChapterEntry.COLUMN_BOOK_ID + "=?";
        String[] selectionArgs = {mBook.getBookId()};
        Cursor cursor = getContentResolver().query(ChapterEntry.CONTENT_URI, null, selection, selectionArgs, null);
        String bookId = mBook.getBookId();
        while (cursor.moveToNext()) {
            String chapterId = cursor.getString(cursor.getColumnIndexOrThrow(ChapterEntry.COLUMN_CHAPTER_ID));
            String chapterName = cursor.getString(cursor.getColumnIndexOrThrow(ChapterEntry.COLUMN_CHAPTER_TITLE));
            String playtime = cursor.getString(cursor.getColumnIndexOrThrow(ChapterEntry.COLUMN_PLAYTIME));
            mChapterList.add(new Chapter(bookId, chapterId, chapterName, playtime));
            mChapterNameList.add(chapterName);
        }

        mChapterListView.getViewTreeObserver().addOnPreDrawListener(mOnPreDrawListener);
        PlayListAdapter adapter = new PlayListAdapter(this, R.layout.play_list_text_view, mChapterNameList);
        mChapterListView.setAdapter(adapter);

        mChapterListView.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                playAudio(i);
                mCurrentSelectedItem = view;
                mCurrentChapterIndex = i;
                initPlayerControlsForChapter(mCurrentChapterIndex);
                //mChapterListView.setSelection(i);
            }
        });

        mPlayButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mPlayButton.setVisibility(View.GONE);
                mPauseButton.setVisibility(View.VISIBLE);
                if (mServiceBound) {
                    mPlayerService.playAudio();
                } else {
                    Toast.makeText(AudioPlayerActivity.this, "Not connected to Service", Toast.LENGTH_SHORT);
                }
            }
        });

        mPauseButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mPauseButton.setVisibility(View.GONE);
                mPlayButton.setVisibility(View.VISIBLE);
                if (mServiceBound) {
                    mPlayerService.pauseAudio();
                } else {
                    Toast.makeText(AudioPlayerActivity.this, "Not connected to Service", Toast.LENGTH_SHORT);
                }

            }
        });

        mRewind30.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mServiceBound) {
                    mPlayerService.rewind30();
                } else {
                    Toast.makeText(AudioPlayerActivity.this, "Not connected to Service", Toast.LENGTH_SHORT);
                }
            }
        });

        mForward30.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mServiceBound) {
                    mPlayerService.forward30();
                } else {
                    Toast.makeText(AudioPlayerActivity.this, "Not connected to Service", Toast.LENGTH_SHORT);
                }
            }
        });


        mStopButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mServiceBound) {
                    mPlayerService.stopAudio();
                    mPlayerService.resetAudio();
                    stopAudioService();
                    resetPlayControls();
                } else {
                    Toast.makeText(AudioPlayerActivity.this, "Not connected to Service", Toast.LENGTH_SHORT);
                }
            }
        });

        mSeekBar.setOnSeekBarChangeListener(this);
        mSeekBar.setMax(100);

        registerPlayerReceiver();

        mCurrentChapterIndex = mStorageUtils.getChapterIndex();
        if (mCurrentChapterIndex != -1 && mCurrentChapterIndex < mChapterList.size()) {
            Intent onChapterClickIntent = new Intent(AudioPlayerActivity.this, AudioPlayerService.class);
            bindService(onChapterClickIntent, serviceConnection, Context.BIND_AUTO_CREATE);
            initPlayerControlsForChapter(mCurrentChapterIndex);
        } else {
            mStorageUtils.clearCachedChapterPlaylist();
        }

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                long delay = updateProgressBar();
                mHandler.postDelayed(this, delay);
            }
        });
    }

    private long updateProgressBar() {
        if (!mServiceBound) {
            return 500;
        }

        if (mChapterDuration < 0) {
            mChapterDuration = mPlayerService.getDuration();
            if(mChapterDuration <0) {
                mEndPointTime.setText(makeTimeString(this, 0));
            } else {
                mEndPointTime.setText(makeTimeString(this, mChapterDuration / 1000));
            }

            if (mPlayerService.getCurrentPlaybackBookId() != null &&
                    !mBook.getBookId().equals(mPlayerService.getCurrentPlaybackBookId())) {
                mCurrentChapterName.setText(mPlayerService.getCurrentPlaybackTitle());
            }

        }
        long pos = mPlayerService.getCurrentPosition();
        if ((pos >= 0) && (mChapterDuration > 0)) {
            mStartPointTime.setText(makeTimeString(this, pos / 1000));
            int progress = (int) ((pos * 100) / mChapterDuration);
            mSeekBar.setProgress(progress);
        }
        // calculate the number of milliseconds until the next full second, so
        // the counter can be updated at just the right time
        long remaining = 1000 - (pos % 1000);

        // approximate how often we would need to refresh the slider to
        // move it smoothly
        int width = mSeekBar.getWidth();
        if (width == 0) width = 320;
        long smoothrefreshtime = mChapterDuration / width;

        if (smoothrefreshtime > remaining) return remaining;
        if (smoothrefreshtime < 20) return 20;
        return smoothrefreshtime;
    }

    private String makeTimeString(Context context, long secs) {
        String durationFormat = context.getString(
                secs < 3600 ? R.string.durationformatshort : R.string.durationformatlong);

        /* Provide multiple arguments so the format can be changed easily
         * by modifying the xml.
         */
        mFormatBuilder.setLength(0);

        final Object[] timeArgs = mTimeArgs;
        timeArgs[0] = secs / 3600;
        timeArgs[1] = secs / 60;
        timeArgs[2] = (secs / 60) % 60;
        timeArgs[3] = secs;
        timeArgs[4] = secs % 60;

        return mFormatter.format(durationFormat, timeArgs).toString();
    }

    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (!fromUser || !mServiceBound) return;
        long now = SystemClock.elapsedRealtime();
        if ((now - mLastSeekEventTime) > 250) {
            mLastSeekEventTime = now;
            long duration = mPlayerService.getDuration();
            long position = duration * progress / 100;
            mPlayerService.seekTo((int) position);
            // trackball event, allow progress updates
            if (!mmFromTouch) {
                updateProgressBar();
            }
        }
    }

    public void onStartTrackingTouch(SeekBar seekBar) {
        mLastSeekEventTime = 0;
        mmFromTouch = true;
    }

    public void onStopTrackingTouch(SeekBar seekBar) {
        mmFromTouch = false;
    }

    private void playAudio(int chapterIndex) {
        //Check is service is active
        mStorageUtils.storeChapterList(mChapterPathList);
        mStorageUtils.setChapterIndex(chapterIndex);

        Intent onChapterClickIntent = new Intent(AudioPlayerActivity.this, AudioPlayerService.class);
        onChapterClickIntent.setAction(Constants.ACTION_PLAY_CHAPTER);
        onChapterClickIntent.putExtra("book_obj", mBook);
        startService(onChapterClickIntent);
        if (!mServiceBound) {
            bindService(onChapterClickIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    private void initPlayerControlsForChapter(int chapterIndex) {
        setListItemSelected();
        mChapterDuration = -1;
        mPlayButton.setVisibility(View.GONE);
        mPauseButton.setVisibility(View.VISIBLE);
        mSeekBar.setProgress(0);
            mCurrentChapterName.setText(mChapterList.get(chapterIndex).getChapterTitle());
        mStartPointTime.setText(makeTimeString(this, 0));
        mEndPointTime.setText(makeTimeString(this, 0));
    }

    private void resetPlayControls() {
        mChapterDuration = -1;
        mPlayButton.setVisibility(View.VISIBLE);
        mPauseButton.setVisibility(View.GONE);
        mSeekBar.setProgress(0);
        mCurrentChapterName.setText("");
        mStartPointTime.setText(makeTimeString(this, 0));
        mEndPointTime.setText(makeTimeString(this, 0));

    }

    //Binding this Client to the AudioPlayer Service
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            AudioPlayerService.LocalBinder binder = (AudioPlayerService.LocalBinder) service;
            mPlayerService = binder.getService();
            mServiceBound = true;
            Log.e("########","service connected ");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServiceBound = false;
        }
    };

    private void setBackgroundImage(Book book) {
        final ImageView imageView = (ImageView) findViewById(R.id.player_background);
        Picasso.with(this).load(book.getImageUrl()).placeholder(R.mipmap.default_placeholder)
                .into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        Bitmap blurredBitmap = BlurBuilder.blur(AudioPlayerActivity.this, bitmap);
                        imageView.setBackground(new BitmapDrawable(getResources(), blurredBitmap));
                    }

                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {

                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }
                });
    }

    private void registerPlayerReceiver() {
        mPlayerBroadcastReceiver = new PlayerServiceBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.ACTION_BEGIN_NEW_CHAPTER);
        filter.addAction(Constants.ACTION_PLAYBACK_STOPPED);
        this.registerReceiver(mPlayerBroadcastReceiver, filter);
    }

    public class PlayerServiceBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Constants.ACTION_BEGIN_NEW_CHAPTER)) {
                mCurrentChapterIndex = mStorageUtils.getChapterIndex();
                if (mCurrentChapterIndex < mChapterList.size() - 1) {
                    initPlayerControlsForChapter(mCurrentChapterIndex);
                }
            } else if(action.equals(Constants.ACTION_PLAYBACK_STOPPED)){
                resetPlayControls();
            }
        }
    }

    private void stopAudioService() {
        Intent stopServiceIntent = new Intent(AudioPlayerActivity.this, AudioPlayerService.class);
        if (mServiceBound) {
            unbindService(serviceConnection);
            mServiceBound = false;
        }
        stopService(stopServiceIntent);
    }

    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mPlayerBroadcastReceiver);
        if (mServiceBound) {
            unbindService(serviceConnection);
        }
    }

    private final ViewTreeObserver.OnPreDrawListener mOnPreDrawListener = new ViewTreeObserver.OnPreDrawListener() {
        @Override
        public boolean onPreDraw() {
            mChapterListView.getViewTreeObserver().removeOnPreDrawListener(this);
            setListItemSelected();
            return true;
        }
    };

    private void setListItemSelected() {
        if (mCurrentSelectedItem != null) {
            if (mPrevSelectedItem != null) {
                mPrevSelectedItem.setBackgroundColor(
                        ContextCompat.getColor(AudioPlayerActivity.this, R.color.transparent));
            }
            mPrevSelectedItem = mCurrentSelectedItem;
            mCurrentSelectedItem.setBackgroundColor(ContextCompat.getColor(AudioPlayerActivity.this, R.color.translucent_color));
            ((PlayListAdapter)mChapterListView.getAdapter()).chapterSelection(mCurrentChapterIndex);
            ((PlayListAdapter)mChapterListView.getAdapter()).notifyDataSetChanged();
        }
    }
}
