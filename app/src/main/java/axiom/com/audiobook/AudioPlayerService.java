package axiom.com.audiobook;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.IOException;
import java.util.ArrayList;

import axiom.com.audiobook.activities.AudioPlayerActivity;
import axiom.com.audiobook.data.Book;
import axiom.com.audiobook.data.Constants;
import axiom.com.audiobook.data.Constants.PlaybackStatus;
import axiom.com.audiobook.data.StorageUtils;

import static android.R.attr.id;
import static android.app.PendingIntent.FLAG_CANCEL_CURRENT;
import static axiom.com.audiobook.data.Constants.ACTION_PAUSE;
import static axiom.com.audiobook.data.Constants.ACTION_PLAY;
import static axiom.com.audiobook.data.Constants.ACTION_REWIND_30;
import static axiom.com.audiobook.data.Constants.ACTION_STOP;

public class AudioPlayerService extends Service implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener,
        AudioManager.OnAudioFocusChangeListener {

    private final String TAG = AudioPlayerService.class.getSimpleName();

    private final IBinder iBinder = new LocalBinder();

    private Book mBook;

    private MediaPlayer mMediaPlayer;
    private int mResumePosition = 0;

    private AudioManager audioManager;
    //Handle incoming phone calls
    private boolean mOngoingCall = false;
    //private PhoneStateListener phoneStateListener;
    //private TelephonyManager telephonyManager;

    //AudioPlayer notification ID
    private static final int NOTIFICATION_ID = 101;

    //List of available Audio files
    private ArrayList<String> mChapterList;
    private int mCurrentChapterIndex = -1;
    private String mCurrentChapterPath;
    com.squareup.picasso.Target mTarget;
    NotificationManager mNotificationManager;

    //============== Service Callbacks Start===========================================================================

    @Override
    public void onCreate() {
        super.onCreate();
        //callStateListener();
        registerBecomingNoisyReceiver();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent.getAction().equals(Constants.ACTION_PLAY_CHAPTER)) {
            try {
                //Load data from SharedPreferences
                StorageUtils storage = new StorageUtils(getApplicationContext());
                mChapterList = storage.loadChapterList();
                mCurrentChapterIndex = storage.getChapterIndex();

                if (mCurrentChapterIndex != -1 && mCurrentChapterIndex < mChapterList.size()) {

                    //index is in a valid range
                    mCurrentChapterPath = mChapterList.get(mCurrentChapterIndex);
                } else {
                    stopSelf();
                }
            } catch (NullPointerException e) {
                stopSelf();
            }

            //Request audio focus
            if (!requestAudioFocus()) {
                //Could not gain focus
                stopSelf();
            }

            mBook = intent.getParcelableExtra("book_obj");
            if (mMediaPlayer != null) {
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.stop();
                }
                mMediaPlayer.reset();
                mMediaPlayer.release();
                mMediaPlayer = null;
            }
            if (mMediaPlayer == null) {
                if (!TextUtils.isEmpty(mCurrentChapterPath)) {
                    initializeMediaPlayer();
                }
            }
        } else {
            handleIncomingActions(intent);
        }

        return START_NOT_STICKY;
    }

    public class LocalBinder extends Binder {
        public AudioPlayerService getService() {
            return AudioPlayerService.this;
        }
    }

    public void onTaskRemoved(Intent rootIntent) {
        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        new StorageUtils(this).clearCachedChapterPlaylist();

        if (mMediaPlayer != null) {
            stopAudio();
            resetAudio();
        }
        if (mBecomingNoisyReceiver != null) {
            unregisterReceiver(mBecomingNoisyReceiver);
            mBecomingNoisyReceiver = null;
        }
        //Disable the PhoneStateListener
     /*   if (phoneStateListener != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }*/

        removeAudioFocus();
        removeNotification();
    }

    public String getCurrentPlaybackTitle() {
        if (mBook != null) {
            return mBook.getTitle();
        }
        return null;
    }

    public String getCurrentPlaybackBookId() {
        if (mBook != null) {
            return mBook.getBookId();
        }
        return null;
    }

    //============== Service Callbacks End=======================================================================

    // ==============Media Player Start===========================================================================
    private void initializeMediaPlayer() {
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
        }
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnBufferingUpdateListener(this);
        mMediaPlayer.setOnInfoListener(this);
        mMediaPlayer.setOnSeekCompleteListener(this);

        mMediaPlayer.reset();

        try {
            mMediaPlayer.setDataSource(mCurrentChapterPath);
        } catch (IOException e) {
            e.printStackTrace();
            stopSelf();
        }
        mMediaPlayer.prepareAsync();
        buildNotification(PlaybackStatus.PLAYING);

    }

    public void playAudio() {
        if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
            mMediaPlayer.start();
        }
    }

    public void stopAudio() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
        }
        removeNotification();
        new StorageUtils(this).clearCachedChapterPlaylist();
    }

    public void resetAudio() {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    public void pauseAudio() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            mResumePosition = mMediaPlayer.getCurrentPosition();
        }
    }

    public void resumeAudio() {
        if (!mMediaPlayer.isPlaying()) {
            mMediaPlayer.seekTo(mResumePosition);
            mMediaPlayer.start();
        }
    }

    public void rewind30() {
        int currentPos = mMediaPlayer.getCurrentPosition();
        int seekPosition = (currentPos - 30 * 1000) < 0 ? 0 : (currentPos - 30 * 1000);
        mMediaPlayer.seekTo(seekPosition);
    }

    public void forward30() {
        int totalDuration = mMediaPlayer.getDuration();
        int currentPos = mMediaPlayer.getCurrentPosition();
        int seekPosition = (currentPos + 30 * 1000) > totalDuration ? totalDuration - (5 * 1000) : (currentPos + 30 * 1000);
        mMediaPlayer.seekTo(seekPosition);
    }

    public long getDuration() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.getDuration();
        } else {
            return -1;
        }
    }

    public int getCurrentPosition() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            return mMediaPlayer.getCurrentPosition();
        } else {
            return -1;
        }
    }

    public void seekTo(int position) {
        if (mMediaPlayer != null) {
            mMediaPlayer.seekTo(position);
        }
    }

    public void skipToNext() {

        if (mCurrentChapterIndex == mChapterList.size() - 1) {
            //if last in playlist
            mCurrentChapterIndex = 0;
            mCurrentChapterPath = mChapterList.get(mCurrentChapterIndex);
        } else {
            //get next in playlist
            mCurrentChapterPath = mChapterList.get(++mCurrentChapterIndex);
        }

        //Update stored index
        new StorageUtils(getApplicationContext()).setChapterIndex(mCurrentChapterIndex);

        pauseAudio();
        //reset mediaPlayer
        mMediaPlayer.reset();
        initializeMediaPlayer();
        sendChapterDoneIntent();
    }

    public void skipToPrevious() {

        if (mCurrentChapterIndex == 0) {
            //if first in playlist
            //set index to the last of audioList
            mCurrentChapterIndex = mChapterList.size() - 1;
            mCurrentChapterPath = mChapterList.get(mCurrentChapterIndex);
        } else {
            //get previous in playlist
            mCurrentChapterPath = mChapterList.get(--mCurrentChapterIndex);
        }
        //Update stored index
        new StorageUtils(getApplicationContext()).setChapterIndex(mCurrentChapterIndex);

        stopAudio();
        //reset mediaPlayer
        mMediaPlayer.reset();
        initializeMediaPlayer();
        sendChapterDoneIntent();
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        playAudio();
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        stopAudio();
        mMediaPlayer.reset();
        removeNotification();

        mCurrentChapterIndex++;
        if (mCurrentChapterIndex < mChapterList.size() - 1) {
            mCurrentChapterPath = mChapterList.get(mCurrentChapterIndex);
            new StorageUtils(this).setChapterIndex(mCurrentChapterIndex);
            initializeMediaPlayer();
            sendChapterDoneIntent();
        } else {
            stopSelf();
        }
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int extra) {
        switch (i) {
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Log.e(TAG, "media error server died" + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.e(TAG, "media error unknown" + extra);
                break;
        }
        return false;
    }

    @Override
    public void onSeekComplete(MediaPlayer mediaPlayer) {
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {
    }

    @Override
    public boolean onInfo(MediaPlayer mediaPlayer, int i, int i1) {
        return false;
    }

    // ==========================Media Player End=====================================================

    // ====================Notification Related Start==============================================================

    private void buildNotification(PlaybackStatus playbackStatus) {

        int notificationAction = R.drawable.ic_pause_notification; //needs to be initialized
        PendingIntent play_pauseAction = null;

        //Build a new notification according to the current state of the MediaPlayer
        if (playbackStatus == PlaybackStatus.PLAYING) {
            notificationAction = R.drawable.ic_pause_notification;
            //create the pause action
            play_pauseAction = playbackAction(1);
        } else if (playbackStatus == PlaybackStatus.PAUSED) {
            notificationAction = R.drawable.ic_play_notification;
            //create the play action
            play_pauseAction = playbackAction(0);
        }

        Bitmap largeIcon;
        largeIcon = BitmapFactory.decodeResource(getResources(),
                R.mipmap.default_placeholder);

        mNotificationManager =
                (NotificationManager) (getSystemService(NOTIFICATION_SERVICE));


        NotificationCompat.Action.Builder rewindActionBuilder = new NotificationCompat.Action.Builder(R.drawable.ic_replay_30_notification, "", playbackAction(3));
        NotificationCompat.Action.Builder playActionBuilder = new NotificationCompat.Action.Builder(notificationAction, "", play_pauseAction);
        NotificationCompat.Action.Builder stopActionBuilder = new NotificationCompat.Action.Builder(R.drawable.ic_stop_notification, "", playbackAction(2));

        int color = ContextCompat.getColor(this, R.color.colorPrimary);

        // Create a new Notification
        final NotificationCompat.Builder notificationBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(this, "AudioBookChannel")
                // Hide the timestamp
                .setShowWhen(false)
                // Set the large and small icons
                .setLargeIcon(largeIcon)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setSound(null, 0)
                // Set Notification content information
                .setContentText(mBook.getAuthorName())
                .setContentTitle(mBook.getTitle())
                .setContentIntent(getActivityLaunchIntent())
                // Add playback actions
                .setColor(color)
                .setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle())
                .addAction(rewindActionBuilder.build())
                .addAction(playActionBuilder.build())
                .addAction(stopActionBuilder.build());


        mTarget = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                notificationBuilder.setLargeIcon(bitmap);
                 //.setDefaults(Notification.DEFAULT_ALL)
                // send the notification again to update it w/ the right image
                mNotificationManager.notify(id, notificationBuilder.build());
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
            }
        };
        Picasso.with(this).load(mBook.getThumbNailUrl()).into(mTarget);
    }


    private PendingIntent getActivityLaunchIntent() {
        Intent playerActivityIntent = new Intent(this, AudioPlayerActivity.class);
        playerActivityIntent.putExtra("book_obj", mBook);
        return PendingIntent.getActivity(this, 0, playerActivityIntent, FLAG_CANCEL_CURRENT);
    }

    private PendingIntent playbackAction(int actionNumber) {
        Intent playbackAction = new Intent(this, AudioPlayerService.class);
        playbackAction.putExtra("book_obj", mBook);
        switch (actionNumber) {
            case 0:
                // Play
                playbackAction.setAction(ACTION_PLAY);
                return PendingIntent.getService(this, actionNumber, playbackAction, FLAG_CANCEL_CURRENT);
            case 1:
                // Pause
                playbackAction.setAction(ACTION_PAUSE);
                return PendingIntent.getService(this, actionNumber, playbackAction, FLAG_CANCEL_CURRENT);
            case 2:
                // Next track
                playbackAction.setAction(ACTION_STOP);
                return PendingIntent.getService(this, actionNumber, playbackAction, FLAG_CANCEL_CURRENT);
            case 3:
                // Previous track
                playbackAction.setAction(ACTION_REWIND_30);
                return PendingIntent.getService(this, actionNumber, playbackAction, FLAG_CANCEL_CURRENT);
            default:
                break;
        }
        return null;
    }

    private void removeNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    private void handleIncomingActions(Intent playbackAction) {
        if (playbackAction == null || playbackAction.getAction() == null) return;

        String actionString = playbackAction.getAction();
        if (actionString.equalsIgnoreCase(ACTION_PLAY)) {
            resumeAudio();
            buildNotification(PlaybackStatus.PLAYING);
        } else if (actionString.equalsIgnoreCase(ACTION_PAUSE)) {
            pauseAudio();
            buildNotification(PlaybackStatus.PAUSED);
        } else if (actionString.equalsIgnoreCase(ACTION_REWIND_30)) {
            rewind30();
        } else if (actionString.equalsIgnoreCase(ACTION_STOP)) {
            stopAudio();
            Intent stopIntent = new Intent(Constants.ACTION_PLAYBACK_STOPPED);
            sendBroadcast(stopIntent);
        }
    }

    // ====================Notification Related End==============================================================

    //================Audio Focus Related Start =====================================================================
    @Override
    public void onAudioFocusChange(int focusState) {
        //Invoked when the audio focus of the system is updated.
        switch (focusState) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // resume playback
                if (mMediaPlayer == null) {
                    initializeMediaPlayer();
                }
                mMediaPlayer.setVolume(1.0f, 1.0f);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                // Lost focus for an unbounded amount of time: stop playback and release media player
                if (mMediaPlayer.isPlaying()) mMediaPlayer.stop();
                mMediaPlayer.release();
                mMediaPlayer = null;
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                if (mMediaPlayer.isPlaying()) mMediaPlayer.pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (mMediaPlayer.isPlaying()) mMediaPlayer.setVolume(0.1f, 0.1f);
                break;
        }
    }

    private boolean requestAudioFocus() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            //Focus gained
            return true;
        }
        //Could not gain focus
        return false;
    }

    private boolean removeAudioFocus() {
        if (audioManager != null) {
            return AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
                    audioManager.abandonAudioFocus(this);
        }
        return false;
    }
    //================Audio Focus Related End=====================================================================

    //=================Telephony/ Call related Start =============================================================

    /*private void callStateListener() {
        // Get the telephony manager
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        //Starting listening for PhoneState changes
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                switch (state) {
                    //if at least one call exists or the phone is ringing
                    //pause the MediaPlayer
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                    case TelephonyManager.CALL_STATE_RINGING:
                        if (mMediaPlayer != null) {
                            pauseAudio();
                            mOngoingCall = true;
                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        // Phone idle. Start playing.
                        if (mMediaPlayer != null) {
                            if (mOngoingCall) {
                                mOngoingCall = false;
                                resumeAudio();
                            }
                        }
                        break;
                }
            }
        };
        // Register the listener with the telephony manager
        // Listen for changes to the device call state.
        telephonyManager.listen(phoneStateListener,
                PhoneStateListener.LISTEN_CALL_STATE);
    }
    */
    //=================Telephony/ Call related End=========================================================

    //===========ACTION_AUDIO_BECOMING_NOISY Listener Start==========================================================
    private BroadcastReceiver mBecomingNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //pause audio on ACTION_AUDIO_BECOMING_NOISY
            pauseAudio();
            buildNotification(PlaybackStatus.PAUSED);
        }
    };

    private void registerBecomingNoisyReceiver() {
        //register after getting audio focus
        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(mBecomingNoisyReceiver, intentFilter);
    }
    //===========ACTION_AUDIO_BECOMING_NOISY Listener End==============================================================

    private void sendChapterDoneIntent() {
        Intent nextChapterIntent = new Intent(Constants.ACTION_BEGIN_NEW_CHAPTER);
        sendBroadcast(nextChapterIntent);
    }
}

