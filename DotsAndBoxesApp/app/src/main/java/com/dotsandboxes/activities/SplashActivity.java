package com.dotsandboxes.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.dotsandboxes.BuildConfig;
import com.dotsandboxes.R;
import com.dotsandboxes.utils.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class SplashActivity extends AppCompatActivity {


    @BindView(R.id.img_dot_boax_gif)
    ImageView imgDotBoaxGif;
    private Timer timer;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    //timer task to load main activity after specified time
    private TimerTask loadNextActivity = new TimerTask() {
        @Override
        public void run() {
            Intent nextActivity;
            SharedPreferences prefs = getSharedPreferences(Constants.PREFS_NAME_GENERAL, Context.MODE_PRIVATE);
            nextActivity = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(nextActivity);
            finish();
        }
    };
    private OnUpdateNeededListener onUpdateNeededListener = new OnUpdateNeededListener() {

        @Override
        public void onNoUpdateNeeded() {
            timer = new Timer();
            Glide.with(SplashActivity.this)
                    .asGif()
                    .load(R.raw.logo_animastion)
                    .listener(new RequestListener<GifDrawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                    Target<GifDrawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GifDrawable resource, Object model,
                                                       Target<GifDrawable> target, DataSource dataSource,
                                                       boolean isFirstResource) {
                            resource.setLoopCount(1);
                            if (!resource.isRunning()) {
                                timer.schedule(loadNextActivity, getResources().getInteger(R.integer.splash_duration));
                            }
                            return false;
                        }
                    })
                    .into(imgDotBoaxGif);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);

        init();
    }

    /**
     * This function provide to load gif and perform timer task after gif loaded.
     */
    private void init() {

        onUpdateNeededListener.onNoUpdateNeeded();

    }


    public interface OnUpdateNeededListener {
        void onNoUpdateNeeded();
    }
}
