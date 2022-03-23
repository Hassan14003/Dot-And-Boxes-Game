package com.dotsandboxes.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.dotsandboxes.R;
import com.dotsandboxes.event_bus.RxBus;
import com.dotsandboxes.event_bus.events.EmitSoundEvent;
import com.dotsandboxes.fragments.ChooseTurnFragment;
import com.dotsandboxes.fragments.GameFragment;
import com.dotsandboxes.fragments.ResultFragment;
import com.dotsandboxes.fragments.WonLostFragment;
import com.dotsandboxes.game.controllers.Game;
import com.dotsandboxes.services.MusicPlayerService;
import com.dotsandboxes.utils.Constants;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class GameActivity extends MusicPlayerActivity implements FragmentManager.OnBackStackChangedListener,
        GameFragment.OnFragmentInteractionListener, ResultFragment.OnFragmentInteractionListener, ChooseTurnFragment.OnFragmentInteractionListener, WonLostFragment.OnFragmentInteractionListener {


    private static final long BOT_DELAY_TIME = 1000;
    @BindView(R.id.iv_toolbar_back)
    ImageView ivToolbarBack;
    @BindView(R.id.tv_toolbar_title)
    TextView tvToolbarTitle;
    @BindView(R.id.iv_toolbar_sound)
    ImageView ivToolbarSound;
    @BindView(R.id.common_toolbar)
    Toolbar commonToolbar;
    @BindView(R.id.iv_toolbar_vibrate)
    ImageView ivToolbarVibrate;
    @BindView(R.id.iv_toolbar_music)
    ImageView ivToolbarMusic;
    @BindView(R.id.content)
    FrameLayout content;
    @BindView(R.id.iv_toolbar_setting)
    ImageView ivToolbarSetting;
    boolean mIsStateAlreadySaved = false;
    boolean mPendingShowDialog = false;
    private GameFragment gameFragment;
    private Game.Mode mode;
    private String gameMode;
    private String player1Name;
    private String player2Name;
    private String playerNameMe;
    private String playerMeImage;
    private boolean chooseTurn = false;
    private InterstitialAd mInterstitialAd;
    /**
     * Class for interacting with the game interface of the service.
     */
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MusicPlayerService.LocalBinder binder = (MusicPlayerService.LocalBinder) service;
            mService = binder.getService();
            serviceBound = true;

            Intent intent = new Intent(GameActivity.this, MusicPlayerService.class);

            boolean playMusic = PreferenceManager.getDefaultSharedPreferences(GameActivity.this).getBoolean(getString(R.string.pref_key_music), true);

            if (playMusic) {
                intent.setAction(MusicPlayerService.ACTION_START_MUSIC);
            } else {
                intent.setAction(MusicPlayerService.ACTION_STOP_MUSIC);
            }

            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            startService(new Intent(MusicPlayerService.ACTION_STOP_MUSIC));
            serviceBound = false;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        doBindService();

    }

    @Override
    protected void onStop() {
        doBindService();
        super.onStop();
    }

    void doBindService() {
        Intent intent = new Intent(GameActivity.this, MusicPlayerService.class);

        // start playing music if the user specified so in the settings screen
        boolean playMusic = PreferenceManager.getDefaultSharedPreferences(GameActivity.this).getBoolean(getString(R.string.pref_key_music), true);
        if (playMusic) {
            intent.setAction(MusicPlayerService.ACTION_START_MUSIC);
        } else {
            intent.setAction(MusicPlayerService.ACTION_STOP_MUSIC);
        }

        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        serviceBound = true;
    }

    void doUnbindService() {
        if (serviceBound) {
            unbindService(serviceConnection);
            serviceBound = false;
        }
    }

    @Override
    public void onPause() {

        super.onPause();
    }

    /**
     * Called when returning to the activity
     */
    @Override
    public void onResume() {
        super.onResume();
        onResumeFragments();


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        ButterKnife.bind(this);

        init(savedInstanceState);
    }

    @Override
    public void onResumeFragments() {
        super.onResumeFragments();
        mIsStateAlreadySaved = false;
        if (mPendingShowDialog) {
            mPendingShowDialog = false;
            Bundle args = gameFragment.loadGameResultFragment();
            onWinFragmentLoad(ResultFragment.FRAGMENT_ID, args);
        }
    }

    private void init(Bundle savedInstanceState) {
        setSupportActionBar(commonToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        ivToolbarBack.setVisibility(View.VISIBLE);
        ivToolbarMusic.setVisibility(View.VISIBLE);
        ivToolbarSound.setVisibility(View.VISIBLE);
        ivToolbarVibrate.setVisibility(View.VISIBLE);
        ivToolbarSetting.setVisibility(View.GONE);
        tvToolbarTitle.setText("");


        Intent intent = getIntent();
        String selectedRow = intent.getStringExtra(Constants.SELECTED_ROW);
        String selectedColumn = intent.getStringExtra(Constants.SELECTED_COLUMN);
        gameMode = intent.getStringExtra(Constants.GAME_MODE);
        player1Name = intent.getStringExtra(Constants.PLAYER1_NAME);
        player2Name = intent.getStringExtra(Constants.PLAYER2_NAME);
        playerNameMe = intent.getStringExtra(Constants.prefrences.NAME);
        playerMeImage = intent.getStringExtra(Constants.prefrences.PROFILE_IMAGE);


        int rows = Integer.parseInt(selectedRow);
        int columns = Integer.parseInt(selectedColumn);

        getSupportFragmentManager().addOnBackStackChangedListener(this);

        if (savedInstanceState != null) {
            gameFragment = (GameFragment) getSupportFragmentManager().getFragment(savedInstanceState, GameFragment.class.getName());
        } else {
            Bundle args = new Bundle();
            args.putInt(Constants.SELECTED_ROW, rows);
            args.putInt(Constants.SELECTED_COLUMN, columns);
            args.putString(Constants.GAME_MODE, gameMode);
            args.putString(Constants.PLAYER1_NAME, player1Name);
            args.putString(Constants.PLAYER2_NAME, player2Name);
           /* args.putString(Constants.prefrences.NAME, playerNameMe);
            args.putString(Constants.prefrences.PROFILE_IMAGE, playerMeImage);*/
            gameFragment = new GameFragment();
            gameFragment.setArguments(args);
        }

//        adView.setVisibility(View.GONE);

        loadGameFragment();


    }

    private void loadGameFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content, gameFragment);
        transaction.commit();

        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        final boolean hasMusic = preferences.getBoolean(getString(R.string.pref_key_music), true);
        final boolean hasSound = preferences.getBoolean(getString(R.string.pref_key_sound), false);
        final boolean hasVibrate = preferences.getBoolean(getString(R.string.pref_key_vibrate), false);

        ivToolbarVibrate.setColorFilter(ContextCompat.getColor(GameActivity.this, R.color.icon_color_white));
        ivToolbarMusic.setColorFilter(ContextCompat.getColor(GameActivity.this, R.color.icon_color_white));
        ivToolbarSound.setColorFilter(ContextCompat.getColor(GameActivity.this, R.color.icon_color_white));


        if (hasMusic) {
            ivToolbarSound.setAlpha(1f);
        } else {
            ivToolbarSound.setAlpha(0.5f);
        }

        if (hasSound) {
            ivToolbarMusic.setAlpha(1f);
        } else {
            ivToolbarMusic.setAlpha(0.5f);
        }

        if (hasVibrate) {
            ivToolbarVibrate.setAlpha(1f);
        } else {
            ivToolbarVibrate.setAlpha(0.5f);
        }

    }


    @OnClick({R.id.iv_toolbar_back, R.id.iv_toolbar_sound, R.id.iv_toolbar_music, R.id.iv_toolbar_vibrate, R.id.iv_toolbar_setting})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_toolbar_back:
                onBackPressed();
                break;
            case R.id.iv_toolbar_sound:
                final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                final boolean hasMusic = preferences.getBoolean(getString(R.string.pref_key_music), false);
                if (!hasMusic) {
                    Intent intent = new Intent(this, MusicPlayerService.class);
                    intent.setAction(MusicPlayerService.ACTION_START_MUSIC);
                    mService.sendCommand(intent);

                    preferences.edit().putBoolean(getString(R.string.pref_key_music), true).apply();
                    ivToolbarSound.setAlpha(1f);
                } else {
                    Intent intent = new Intent(this, MusicPlayerService.class);
                    intent.setAction(MusicPlayerService.ACTION_STOP_MUSIC);
                    mService.sendCommand(intent);

                    preferences.edit().putBoolean(getString(R.string.pref_key_music), false).apply();
                    ivToolbarSound.setAlpha(0.5f);
                }
                break;
            case R.id.iv_toolbar_music:
                SharedPreferences preferencesSound = PreferenceManager.getDefaultSharedPreferences(this);
                boolean hasSound = preferencesSound.getBoolean(getString(R.string.pref_key_sound), false);
                if (!hasSound) {
                    preferencesSound.edit().putBoolean(getString(R.string.pref_key_sound), true).apply();
                    ivToolbarMusic.setAlpha(1f);
                } else {
                    RxBus.getInstance().send(new EmitSoundEvent());

                    preferencesSound.edit().putBoolean(getString(R.string.pref_key_sound), false).apply();
                    ivToolbarMusic.setAlpha(0.5f);
                }
                gameFragment.onSoundRequestClick();

                break;
            case R.id.iv_toolbar_vibrate:
                SharedPreferences preferencesVibreate = PreferenceManager.getDefaultSharedPreferences(this);
                boolean hasVibrate = preferencesVibreate.getBoolean(getString(R.string.pref_key_vibrate), false);
                if (!hasVibrate) {
                    preferencesVibreate.edit().putBoolean(getString(R.string.pref_key_vibrate), true).apply();
                    ivToolbarVibrate.setAlpha(1f);
                } else {

                    preferencesVibreate.edit().putBoolean(getString(R.string.pref_key_vibrate), false).apply();
                    ivToolbarVibrate.setAlpha(0.5f);
                }
                gameFragment.onVibrateRequested();
                break;
        }
    }

    @Override
    public void onBackStackChanged() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        int backStackCount = fragmentManager.getBackStackEntryCount();

        if (backStackCount < 1)
            finish();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        //Save the fragment's instance
        getSupportFragmentManager().putFragment(outState, GameFragment.class.getName(), gameFragment);
    }

    @Override
    public void onWinFragmentLoad(int fragmentId, Bundle args) {


        if (mIsStateAlreadySaved) {
            mPendingShowDialog = true;
        } else {


            if (fragmentId == ResultFragment.FRAGMENT_ID) {
                int scorePlayer1 = args.getInt(GameFragment.ARG_PLAYER1_SCORE);
                int scorePlayer2 = args.getInt(GameFragment.ARG_PLAYER2_SCORE);
                Game.Mode mode = (Game.Mode) args.getSerializable(GameFragment.ARG_GAME_MODE);

      /*      WonLostFragment dialog = WonLostFragment.newInstance(args);
            dialog.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
            dialog.setArguments(args);
            dialog.show(getSupportFragmentManager(), "dialog_fragment");
            dialog.setCancelable(false);*/

                if (!isAppInBackground(GameActivity.this)) {
                    if (gameMode.equals(Constants.ONLINE)) {
                        WonLostFragment dialog = WonLostFragment.newInstance(args);
                        dialog.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
                        dialog.setArguments(args);
                        dialog.show(getSupportFragmentManager(), "dialog_fragment");
                        dialog.setCancelable(false);
                    } else {
                        ResultFragment dialog = ResultFragment.newInstance(args);
                        dialog.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
                        dialog.setArguments(args);
                        dialog.show(getSupportFragmentManager(), "dialog_fragment");
                        dialog.setCancelable(false);
                    }
                }


            }
        }
    }

    @Override
    public void onSoundRequested() {
        mService.sendCommand(new Intent(MusicPlayerService.ACTION_PLAY_SOUND));
    }

    @Override
    public void onChooseTurnFragmentLoad() {
        ChooseTurnFragment dialog = new ChooseTurnFragment();
        Bundle bundle = new Bundle();
        bundle.putString(Constants.GAME_MODE, gameMode);
        bundle.putString(Constants.PLAYER1_NAME, player1Name);
        bundle.putString(Constants.PLAYER2_NAME, player2Name);
        bundle.putString(Constants.prefrences.NAME, playerNameMe);
        bundle.putString(Constants.prefrences.PROFILE_IMAGE, playerMeImage);
        dialog.setArguments(bundle);
        dialog.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        dialog.show(getSupportFragmentManager(), "dialog_fragment");
        dialog.setCancelable(false);
    }

    @Override
    public void onReplayRequested(Bundle arguments) {
        gameFragment = new GameFragment();
        gameFragment.setArguments(arguments);

        loadGameFragment();
    }

    @Override
    public void onMenuRequested() {
        finish();
    }

    @Override
    public void onPlayer1Selected() {
        gameFragment.onPlayer1Clicked();
    }

    @Override
    public void onPlayer2Selected() {
        gameFragment.onPlayer2Clicked();
    }

    @Override
    public void onBackPressed() {
        showExitDailog();
    }

    public void showExitDailog() {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(GameActivity.this);
        builder1.setMessage(getString(R.string.game_exit));
        builder1.setCancelable(false);

        builder1.setPositiveButton(
                getString(R.string.yes),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                        overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
                        dialog.cancel();
                    }
                });

        builder1.setNegativeButton(
                getString(R.string.no),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }
}
