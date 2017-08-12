package minthebest.redbutton;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.transition.AutoTransition;
import android.support.transition.Transition;
import android.support.transition.TransitionManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    public static final int ANIMATION_TRANSITION_TIME = 500;
    public static final int ALARM_NOTIFICATION_TIME = 5000;
    private Button redBtn;
    private ConstraintLayout rootView;
    private ConstraintSet setAfter;
    private ConstraintSet setOrigin;
    private Ringtone ringtone;
    private Runnable runnable;
    private AutoTransition animationAfter;
    private AutoTransition animationOrigin;
    private TransitionListener transitionListenerOrigin;
    private TransitionListener transitionListenerAfter;
    private Thread thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupUI();

        setupRunnable();

        registerListener();

        setupSoundPlayer();
    }

    private void setupRunnable() {
        runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(ALARM_NOTIFICATION_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    ringtone.stop();
                    animationAfter.removeListener(transitionListenerAfter);
                    animationOrigin.removeListener(transitionListenerOrigin);
                }
            }
        };
    }

    private void setupSoundPlayer() {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            ringtone = RingtoneManager.getRingtone(getApplicationContext(), notification);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void registerListener() {
        transitionListenerOrigin = new TransitionListener(animationAfter, setOrigin);
        transitionListenerAfter = new TransitionListener(animationOrigin, setAfter);

        redBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (thread != null && thread.isAlive()) {
                    return;
                }

                animationOrigin.addListener(transitionListenerOrigin);
                animationAfter.addListener(transitionListenerAfter);

                ringtone.play();

                TransitionManager.beginDelayedTransition(rootView, animationOrigin);
                setAfter.applyTo(rootView);

                thread = new Thread(runnable);
                thread.start();
            }
        });
    }

    private void setupUI() {
        redBtn = findViewById(R.id.red_btn);
        rootView = findViewById(R.id.root_view);

        setOrigin = new ConstraintSet();
        setOrigin.clone(this, R.layout.activity_main);

        setAfter = new ConstraintSet();
        setAfter.clone(this, R.layout.activity_main_transition_after);

        // setup Animation transition
        animationOrigin = new AutoTransition();
        animationAfter = new AutoTransition();

        animationOrigin.setDuration(ANIMATION_TRANSITION_TIME);
        animationAfter.setDuration(ANIMATION_TRANSITION_TIME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (thread.isAlive()) {
            thread.interrupt();
        }
    }

    private class TransitionListener implements Transition.TransitionListener {

        AutoTransition autoTransition;
        ConstraintSet constraintSet;

        TransitionListener(AutoTransition autoTransition, ConstraintSet constraintSet) {
            this.autoTransition = autoTransition;
            this.constraintSet = constraintSet;
        }

        @Override
        public void onTransitionStart(@NonNull Transition transition) {}

        @Override
        public void onTransitionEnd(@NonNull Transition transition) {
            TransitionManager.beginDelayedTransition(rootView, autoTransition);
            constraintSet.applyTo(rootView);
        }

        @Override
        public void onTransitionCancel(@NonNull Transition transition) {}

        @Override
        public void onTransitionPause(@NonNull Transition transition) {}

        @Override
        public void onTransitionResume(@NonNull Transition transition) {}
    }
}
