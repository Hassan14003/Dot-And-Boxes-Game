package com.dotsandboxes.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.dotsandboxes.R;
import com.dotsandboxes.services.MusicPlayerService;
import com.dotsandboxes.utils.Constants;
import com.dotsandboxes.utils.PrefUtils;
import com.eftimoff.androidplayer.Player;
import com.eftimoff.androidplayer.actions.property.PropertyAction;
import com.google.android.gms.ads.AdView;

import androidx.core.util.Pair;
import androidx.preference.PreferenceManager;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

public class MainActivity extends MusicPlayerActivity{
    private static final String TAG = "MainActivity";
    // Request code used to invoke sign in user interactions.
    private static final int RC_SIGN_IN = 9001;
    public PrefUtils prefUtils;

    @BindView(R.id.ll_me_vs_robot)
    LinearLayout llMeVsRobot;

    @BindView(R.id.tv_selected_grid)
    TextView tvSelectedGrid;
    @BindView(R.id.ll_grid_size)
    LinearLayout llGridSize;
    @BindView(R.id.img_player_1_name_vs_robot)
    ImageView imgPlayer1NameVsRobot;
    @BindView(R.id.tv_player_1_name_vs_robot)
    TextView tvPlayer1NameVsRobot;
    @BindView(R.id.img_player_2_name_vs_robot)
    ImageView imgPlayer2NameVsRobot;
    @BindView(R.id.tv_player_2_name_vs_robot)
    TextView tvPlayer2NameVsRobot;
    @BindView(R.id.ll_choose_grid_size)
    LinearLayout llChooseGridSize;
    @BindView(R.id.ll_choose_game_type)
    LinearLayout llChooseGameType;
    @BindView(R.id.btn_music)
    ImageView btnMusic;


    private Pair<String, String> rowColumnPair = new Pair<>("4", "4");
    private String nameFromPrefrence = "";
    private String imageFromPreference = "";



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

            Intent intent = new Intent(MainActivity.this, MusicPlayerService.class);

            boolean playMusic = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getBoolean(getString(R.string.pref_key_music), true);

            if (playMusic) {
                intent.setAction(MusicPlayerService.ACTION_START_MUSIC);
                mService.sendCommand(intent);
            } else {
                intent.setAction(MusicPlayerService.ACTION_STOP_MUSIC);
                mService.sendCommand(intent);
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setAnimation();
    }

    /**
     * this function will start animation
     */
    private void setAnimation() {

        final PropertyAction music = PropertyAction.newPropertyAction(btnMusic).scaleX(0).scaleY(0).duration(500).interpolator(new AccelerateDecelerateInterpolator()).build();

        final PropertyAction gridSize = PropertyAction.newPropertyAction(llChooseGridSize).scaleX(0).scaleY(0).duration(500).interpolator(new AccelerateDecelerateInterpolator()).build();
        final PropertyAction gameType = PropertyAction.newPropertyAction(llChooseGameType).scaleX(0).scaleY(0).duration(500).interpolator(new AccelerateDecelerateInterpolator()).build();
//        final PropertyAction ad = PropertyAction.newPropertyAction(adView).scaleX(0).scaleY(0).duration(500).interpolator(new AccelerateDecelerateInterpolator()).build();
        Player.init().animate(music).
                then().animate(gridSize).
                then().animate(gameType).

                play();

    }

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

    @Override
    protected void onResume() {
        super.onResume();
        init();


    }


    void doBindService() {
        Intent intent = new Intent(MainActivity.this, MusicPlayerService.class);

        // start playing music if the user specified so in the settings screen
        boolean playMusic = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getBoolean(getString(R.string.pref_key_music), true);
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


    private void init() {
        prefUtils = new PrefUtils(this);

        nameFromPrefrence = prefUtils.getString(Constants.prefrences.NAME);
        imageFromPreference = prefUtils.getString(Constants.prefrences.PROFILE_IMAGE);

        boolean playMusic = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getBoolean(getString(R.string.pref_key_music), true);

        if (playMusic) {
            Glide.with(MainActivity.this)
                    .load(R.drawable.ic_sound_on)
                    .into(btnMusic);
        } else {
            Glide.with(MainActivity.this)
                    .load(R.drawable.ic_sound_off)
                    .into(btnMusic);
        }

        if (nameFromPrefrence != null && !TextUtils.isEmpty(nameFromPrefrence)) {
            tvPlayer1NameVsRobot.setText(nameFromPrefrence);


        } else {
            tvPlayer1NameVsRobot.setText(getString(R.string.me));


        }

        if (imageFromPreference != null && !TextUtils.isEmpty(imageFromPreference)) {
            setImageToImageView(imageFromPreference, imgPlayer1NameVsRobot);

        } else {
            setImageToImageViewFromResource(R.drawable.me_icon, imgPlayer1NameVsRobot);


        }


        setImageToImageViewFromResource(R.drawable.robot_icon, imgPlayer2NameVsRobot);



    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

    }

    @OnClick({R.id.ll_grid_size,
            R.id.ll_me_vs_robot,
             R.id.btn_music})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ll_grid_size:
                showPopupMenu(llGridSize);
                break;
            case R.id.ll_me_vs_robot:
                //logic for me vs robot
                Intent intent = new Intent(MainActivity.this, GameActivity.class);
                String row = rowColumnPair.first;
                String column = rowColumnPair.second;
                intent.putExtra(Constants.SELECTED_ROW, row);
                intent.putExtra(Constants.SELECTED_COLUMN, column);
                intent.putExtra(Constants.GAME_MODE, Constants.ROBOT);
                intent.putExtra(Constants.prefrences.NAME, nameFromPrefrence);
                intent.putExtra(Constants.prefrences.PROFILE_IMAGE, imageFromPreference);
                startActivity(intent);

                break;
            case R.id.btn_music:
                final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                final boolean hasMusic = preferences.getBoolean(getString(R.string.pref_key_music), false);
                Intent intentMusic = new Intent(this, MusicPlayerService.class);
                if (!hasMusic) {

                    intentMusic.setAction(MusicPlayerService.ACTION_START_MUSIC);
                    mService.sendCommand(intentMusic);

                    preferences.edit().putBoolean(getString(R.string.pref_key_music), true).apply();
                    Glide.with(MainActivity.this)
                            .load(R.drawable.ic_sound_on)
                            .into(btnMusic);
                } else {
                    intentMusic.setAction(MusicPlayerService.ACTION_STOP_MUSIC);
                    mService.sendCommand(intentMusic);

                    preferences.edit().putBoolean(getString(R.string.pref_key_music), false).apply();
                    Glide.with(MainActivity.this)
                            .load(R.drawable.ic_sound_off)
                            .into(btnMusic);
                }
                break;
        }
    }

    @Override
    protected void onPause() {

        super.onPause();
    }



    /**
     * Open popup menu to choose grid size for game
     *
     * @param layout
     */
    public void showPopupMenu(LinearLayout layout) {
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View popupView = layoutInflater.inflate(R.layout.choose_grid_menu, null);

        TextView four = (TextView) popupView.findViewById(R.id.four);
        TextView five = (TextView) popupView.findViewById(R.id.five);
        TextView six = (TextView) popupView.findViewById(R.id.six);
        TextView seven = (TextView) popupView.findViewById(R.id.seven);
        TextView eight = (TextView) popupView.findViewById(R.id.eight);
        TextView nine = (TextView) popupView.findViewById(R.id.nine);


        PopupWindow popupWindow = new PopupWindow(
                popupView,
                layout.getWidth(),
                ViewGroup.LayoutParams.WRAP_CONTENT);

        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {

            }
        });

        popupWindow.showAsDropDown(layout);// your view instance in which click you want to show menu

        // Do your customised stuff

        four.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPopupClicked(four.getText().toString());
                popupWindow.dismiss();
            }
        });

        five.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPopupClicked(five.getText().toString());
                popupWindow.dismiss();
            }
        });

        six.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPopupClicked(six.getText().toString());
                popupWindow.dismiss();
            }
        });

        seven.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPopupClicked(seven.getText().toString());
                popupWindow.dismiss();
            }
        });

        eight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPopupClicked(eight.getText().toString());
                popupWindow.dismiss();
            }
        });

        nine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPopupClicked(nine.getText().toString());
                popupWindow.dismiss();
            }
        });


    }

    /**
     * This method is used to choose grid size, as value passed for grid size
     *
     * @param value the for grid size, how many row and column do you want
     */
    public void onPopupClicked(String value) {
        tvSelectedGrid.setText(value);
        switch (value) {
            case Constants.GRID_SIZE.FOUR_BY_FOUR:
                rowColumnPair = new Pair<String, String>("4", "4");
                break;
            case Constants.GRID_SIZE.FIVE_BY_FIVE:
                rowColumnPair = new Pair<String, String>("5", "5");
                break;
            case Constants.GRID_SIZE.SIX_BY_SIX:
                rowColumnPair = new Pair<String, String>("6", "6");
                break;
            case Constants.GRID_SIZE.SEVEN_BY_SEVEN:
                rowColumnPair = new Pair<String, String>("7", "7");
                break;
            case Constants.GRID_SIZE.EIGHT_BY_EIGHT:
                rowColumnPair = new Pair<String, String>("8", "8");
                break;
            case Constants.GRID_SIZE.NINE_BY_NINE:
                rowColumnPair = new Pair<String, String>("9", "9");
                break;
            case Constants.GRID_SIZE.TEN_BY_TEN:
                rowColumnPair = new Pair<String, String>("10", "10");
                break;
        }
    }



    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, Constants.BACK_BUTTON_TWICE, Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 1000);
    }


}
