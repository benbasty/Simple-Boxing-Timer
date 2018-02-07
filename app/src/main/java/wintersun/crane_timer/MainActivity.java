package wintersun.crane_timer;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Locale;

import layout.SettingsFragment;

/*
    App created by: Thomas Winter


    Sound retrieved from: http://freesound.org/people/Benboncan/sounds/66952/
    Sound created by: Benboncan
    License: http://creativecommons.org/licenses/by/3.0/

    Logo retrieved from: https://thenounproject.com/term/boxing-gloves/2712/
    Logo created by: Gabriele Fumero, IT
    License: http://creativecommons.org/licenses/by/3.0/

 */

public class MainActivity extends Activity implements SettingsFragment.OnFragmentInteractionListener, SettingsFragment.OnDataPass, SensorEventListener{

    //timer variables
    private CountDownTimer countDownTimer, restTimer;
    private boolean timerStarted = false;
    private boolean boolRest = false;
    private boolean settingsActive = true;
    private Button buttonStart, buttonMute, buttonSettings;
    private TextView textView, textView2;
    private long roundTime;
    private long restTime;
    private final long interval = 1 * 500;
    private int round = 1;
    private boolean warnEndRound = true, warnEndRest = true, warnHalfRound = false, warnInterval = false, boolVib = true;

    //TTS & Sound
    TextToSpeech tts;
    boolean muteTTS = false;

    SoundPool soundPool;
    HashMap<Integer, Integer> soundPoolMap;
    int soundID = 1;

    int seconds = 0, minutes = 0;
    AudioManager audioManager;

    float curVolume;
    float maxVolume;
    float leftVolume;
    float rightVolume;
    int priority = 1;
    int no_loop = 0;
    float normal_playback_rate = 1f;

    //settings fragment
    Fragment settings;

    //progressbar circular
    CircleProgressBar circleProgressBar;

    //counter
    int detectorCounter = 0, calories = 0;
    double distance = 0.00;
    TextView tvSteps, tvCals, tvDistance;

    //Sensors
    SensorManager mSensorManager;
    Sensor mSensor;

    //Animations
    Animation animAlpha;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //set up settings fragment
        settings = getFragmentManager().findFragmentById(R.id.fragment2);

        //set up step sensor
        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);

        //set up textviews
        textView = (TextView) this.findViewById(R.id.tv_timer);
        textView2 = (TextView) this.findViewById(R.id.tv_timerLabel);
        tvSteps = (TextView)this.findViewById(R.id.tv_stepCounter);
        tvDistance = (TextView)this.findViewById(R.id.tv_distanceCounter);
        tvCals = (TextView)this.findViewById(R.id.tv_calorieCounter);

        //set up progressbar
        circleProgressBar = (CircleProgressBar) findViewById(R.id.custom_progressBar);
        circleProgressBar.setColor(Color.GRAY);
        circleProgressBar.setMax(100);
        circleProgressBar.setProgressWithAnimation(0);

        //set up alpha animation
        animAlpha = AnimationUtils.loadAnimation(this, R.anim.anim_alpha);

        //iniate app in settings mode
        showHideTimer();


        //set up TTS & sounds
        startTTS();
        soundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 100);
        soundPoolMap = new HashMap<Integer, Integer>();
        soundPoolMap.put(soundID, soundPool.load(this, R.raw.boxingbell, 1));

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        curVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        leftVolume = curVolume / maxVolume;
        rightVolume = curVolume / maxVolume;

        //setup buttons & onClickListeners
        buttonMute = (Button)findViewById(R.id.button);
        buttonMute.setText("Help");
        buttonMute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                vibrate(20);

                //set text label of button, alternate between Mute/Help, start/stop of TTS

                //in timer screen this button mutes/unmutes TTS
                if(buttonMute.getText().toString() == "Mute" || buttonMute.getText().toString() == "Unmute"){
                    if (!muteTTS) {
                        buttonMute.setText("Unmute");
                        muteTTS = true;
                        stopTTS();
                        Toast.makeText(MainActivity.this, "Voice muted", Toast.LENGTH_SHORT).show();
                    } else {
                        buttonMute.setText("Mute");
                        muteTTS = false;
                        startTTS();
                        Toast.makeText(MainActivity.this, "Voice unmuted", Toast.LENGTH_SHORT).show();
                    }
                //in settings screen this button takes you to help screen
                }else if(buttonMute.getText().toString() == "Help" || buttonMute.getText().toString() == "Back"){
                    if(buttonMute.getText().toString() == "Help"){
                        buttonMute.setText("Back");
                    }else if(buttonMute.getText().toString() == "Back")
                        buttonMute.setText("Help");

                    SettingsFragment.switchSettingsHelp();
                }
            }
        });

        buttonSettings = (Button)findViewById(R.id.btn_settings);
        buttonSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                vibrate(20);

                //stop keeping screen on
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                //when in settings screen, switch to timer and rename buttons
                if(settingsActive){
                    settingsActive = false;

                    if(!settingsActive){
                        buttonSettings.setText("Settings");
                        if(muteTTS)
                            buttonMute.setText("Unmute");
                        else
                            buttonMute.setText("Mute");
                    }
                //when in timer screen, switch to settings, cancel timer, reset values
                }else{
                    settingsActive = true;
                    countDownTimer.cancel();
                    restTimer.cancel();
                    round = 1;
                    detectorCounter = 0;
                    calories = 0;
                    distance = 0;
                    timerStarted = false;
                    buttonStart.setText("START");
                    if (boolRest) boolRest = false;

                    if(settingsActive){
                        buttonSettings.setText("Timer");
                        buttonMute.setText("Help");

                        if(!SettingsFragment.getIsLayoutSettings())
                            SettingsFragment.switchSettingsHelp();
                    }
                }
                //hide timer textviews/settings
                showHideTimer();
                showHideFragment(settings);
            }
        });

        buttonStart = (Button) this.findViewById(R.id.btn_start);
        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                vibrate(20);

                //reset progressBar with animation
                circleProgressBar.setColor(Color.GRAY);
                circleProgressBar.setProgressWithAnimation(100);

                //reset textviews
                textView.setText("" + String.format("%d:%02d", roundTime / 60000, (roundTime / 1000) % 60));
                textView2.setText("Round 1");
                tvCals.setText("0 cals");
                tvSteps.setText("0 steps");
                tvDistance.setText("0.00 km");

                //start timer if it is not started
                if (!timerStarted) {
                    countDownTimer.start();
                    soundPool.play(soundID, leftVolume, rightVolume, priority, no_loop, normal_playback_rate);
                    timerStarted = true;
                    if(!settings.isHidden()){
                        settingsActive = false;
                        buttonSettings.setText("Settings");

                        if(muteTTS)
                            buttonMute.setText("Unmute");
                        else
                            buttonMute.setText("Mute");

                        showHideFragment(settings);
                        showHideTimer();
                    }
                    textView2.setText("Round " + round);
                    buttonStart.setText("STOP");

                    //keep screen on
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                    if(round == 1){
                        tts.speak(textView2.getText().toString(), TextToSpeech.QUEUE_ADD, null);
                    }

                //stop timer if it is running
                } else {
                    countDownTimer.cancel();
                    restTimer.cancel();
                    round = 1;
                    detectorCounter = 0;
                    calories = 0;
                    distance = 0;
                    timerStarted = false;
                    buttonStart.setText("RESTART");
                    if (boolRest) boolRest = false;

                    //stop keeping screen on
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                }
            }
        });

        //set up timers according to values received from settings fragment
        countDownTimer = new CountDownTimerActivity(roundTime, interval);
        restTimer = new CountDownTimerActivity(restTime, interval);
    }

    @Override
    public void onResume(){
        super.onResume();

        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        //register listener or notify user
        if(mSensor != null) {
            mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_FASTEST);//use small sensor delay rate
        }else{
            Toast.makeText(this, "Step count sensor not found", Toast.LENGTH_SHORT);
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        //stop TTS and unregister step sensor
        stopTTS();
        mSensorManager.unregisterListener(this, mSensor);
    }

    @Override
    public void onPause(){
        super.onPause();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //count steps when in active phase only
        if(!boolRest && timerStarted){
            detectorCounter++;
            tvSteps.setText(String.valueOf(detectorCounter) + " steps");//update textview

            Log.d("Step value", String.valueOf(detectorCounter));

            //calculate calories based on steps taken
            if(detectorCounter % 20 == 0){
                calories++;
                tvCals.setText(String.valueOf(calories) + " cals");
            }

            //set distance to average distance per steps taken
            tvDistance.setText(String.valueOf((Math.round(detectorCounter/13.1))/100.0) + " km");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    //Timer Class
    public class CountDownTimerActivity extends CountDownTimer {

        String warning;

        //constructor
        public CountDownTimerActivity(long time, long interval) {
            super(time, interval);

        }

        @Override
        public void onFinish() {
            //alternate between round and rest when timer is up
            if(!boolRest) {
                boolRest = true;
                round++;
            }
            else if(boolRest){
                boolRest = false;
            }

            //set up timers according to which phase to active/rest phase, notify via TTS
            if(boolRest) {
                vibrate(1000);
                restTimer.start();
                textView2.setText("REST");
                tts.speak(textView2.getText().toString(), TextToSpeech.QUEUE_ADD, null);
            }
            else {
                vibrate(1000);
                countDownTimer.start();
                textView2.setText("Round " + round);
                tts.speak(textView2.getText().toString(), TextToSpeech.QUEUE_ADD, null);
            }

            soundPool.play(soundID, leftVolume, rightVolume, priority, no_loop, normal_playback_rate);
        }

        @Override
        public void onTick(final long millisUntilFinished) {
            //calculate seconds from milliseconds
            seconds = (int) (millisUntilFinished / 1000);

            //update textview
            minutes = seconds / 60;
            seconds = seconds % 60;
            textView.setText("" + String.format("%d:%02d", minutes, seconds));

            //update progressbar
            if(!boolRest && warnEndRound && minutes == 0 && seconds <= 30){
                circleProgressBar.setColor(Color.YELLOW);
            }else if (boolRest){
                circleProgressBar.setColor(Color.RED);
            }else if(!boolRest){
                circleProgressBar.setColor(Color.GREEN);
            }

            //set progressBar to remaining time with animation
            if(!boolRest){
                circleProgressBar.setProgressWithAnimation((int)((millisUntilFinished/1000.0)/(roundTime/1000.0) * 100));
            }else if(boolRest){
                circleProgressBar.setProgressWithAnimation((int)((millisUntilFinished/1000.0)/(restTime/1000.0) * 100));
            }

            //voice notification
            if(!tts.isSpeaking()){
                if(!boolRest && warnHalfRound && (millisUntilFinished/1000) == (roundTime/2000)){
                    warning = "Half time";
                    tts.speak(warning, TextToSpeech.QUEUE_ADD, null);
                    vibrate(500);
                }
                if(!boolRest && warnInterval
                    && ((roundTime/1000) - (millisUntilFinished/1000)) % 45 == 0
                    && !(minutes == 0 && seconds == 0)){

                    warning = "45 seconds have passed";
                    tts.speak(warning, TextToSpeech.QUEUE_ADD, null);
                    vibrate(500);
                }
                if(!boolRest && warnEndRound && minutes == 0 && seconds == 30){
                    warning = "30 seconds remaining";
                    tts.speak(warning, TextToSpeech.QUEUE_ADD, null);
                    vibrate(1000);
                }
                if(boolRest && warnEndRest && minutes == 0 && seconds == 10){
                    warning = "10 seconds rest remaining, get ready";
                    tts.speak(warning, TextToSpeech.QUEUE_ADD, null);
                    vibrate(500);
                }
            }
        }

    }

    //method to start up TTS
    public void startTTS(){
        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR){
                    tts.setLanguage(Locale.CANADA);
                }
            }
        });
    }

    //method to mute then shutdown TTS
    public void stopTTS(){
        if(tts != null){
            tts.stop();
            tts.shutdown();
        }
    }

    //method to hideFragment
    public void showHideFragment(final Fragment fragment){

        FragmentTransaction ft = getFragmentManager().beginTransaction();

        //animation commented out (looks annoying)
        //ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);

        if (fragment.isHidden()) {
            ft.show(fragment);
            Log.d("hidden", "Show");
        } else {
            ft.hide(fragment);
            Log.d("Shown", "Hide");
        }

        ft.commit();
    }

    //hide or show timer elements if settings screen is shown/hidden
    public void showHideTimer(){
        if(settingsActive){
            textView.setVisibility(View.GONE);
            textView2.setVisibility(View.GONE);
            circleProgressBar.setVisibility(View.GONE);
            tvSteps.setVisibility(View.GONE);
            tvDistance.setVisibility(View.GONE);
            tvCals.setVisibility(View.GONE);


        }else if(!settingsActive) {
            textView.setVisibility(View.VISIBLE);
            textView2.setVisibility(View.VISIBLE);
            circleProgressBar.setVisibility(View.VISIBLE);
            circleProgressBar.setColor(Color.GRAY);
            circleProgressBar.setProgress(0);
            circleProgressBar.setProgressWithAnimation(100);
            tvSteps.setVisibility(View.VISIBLE);
            tvDistance.setVisibility(View.VISIBLE);
            tvCals.setVisibility(View.VISIBLE);

            textView.setText("" + String.format("%d:%02d", roundTime / 60000, (roundTime / 1000) % 60));
            textView2.setText("Round 1");
            tvCals.setText("0 cals");
            tvSteps.setText("0 steps");
            tvDistance.setText("0.00 km");


            circleProgressBar.startAnimation(animAlpha);
            textView.startAnimation(animAlpha);
            textView2.startAnimation(animAlpha);
            tvCals.startAnimation(animAlpha);
            tvSteps.startAnimation(animAlpha);
            tvDistance.startAnimation(animAlpha);
        }
    }

    //method to make device vibrate
    private void vibrate(final long millis){
        if(boolVib){
            Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(millis);
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    //receive data from settings fragment
    @Override
    public void onDataPass(String data) {
        int minutes, seconds;

        //set round ("ro") or rest ("re")
        if(data.contains("ro")){
            roundTime = Integer.valueOf(data.substring(2)) * 1000;
            countDownTimer = new CountDownTimerActivity(roundTime, interval);
            textView2.setText("Round " + round);
            Toast.makeText(MainActivity.this, "Round time: " + roundTime/60000 +" minutes", Toast.LENGTH_SHORT).show();
            minutes = (int)roundTime / 60000;
            seconds = (int)roundTime % 60000;
            textView.setText("" + String.format("%d:%02d", minutes, seconds));
        }else if(data.contains("re")) {
            restTime = Integer.valueOf(data.substring(2)) * 1000;
            restTimer.cancel();
            restTimer = new CountDownTimerActivity(restTime, interval);
            Toast.makeText(MainActivity.this, "Rest time: " + restTime/1000 + " seconds", Toast.LENGTH_SHORT).show();
            minutes = (int)roundTime / 60000;
            seconds = (int)roundTime % 60000;
            textView.setText("" + String.format("%d:%02d", minutes, seconds));
        }

        //set feedback/notifications
        else if(data.contains("ce")){
            if(data.startsWith("1")) {
                warnEndRound = true;
                Toast.makeText(MainActivity.this, "30 second round notification activated", Toast.LENGTH_SHORT).show();
            }
            else if(data.startsWith("0")) {
                warnEndRound = false;
                Toast.makeText(MainActivity.this, "30 second round notification deactivated", Toast.LENGTH_SHORT).show();
            }
        }else if(data.contains("cr")){
            if(data.startsWith("1")){
                warnEndRest = true;
                Toast.makeText(MainActivity.this, "10 second rest notification activated", Toast.LENGTH_SHORT).show();
            }else if(data.startsWith("0")){
                warnEndRest = false;
                Toast.makeText(MainActivity.this, "10 second rest notification deactivated", Toast.LENGTH_SHORT).show();
            }
        }else if(data.contains("ch")){
            if(data.startsWith("1")) {
                warnHalfRound = true;
                Toast.makeText(MainActivity.this, "Half round notification activated", Toast.LENGTH_SHORT).show();
            }
            else if(data.startsWith("0")) {
                warnHalfRound = false;
                Toast.makeText(MainActivity.this, "Half round notification deactivated", Toast.LENGTH_SHORT).show();
            }
        }else if(data.contains("ci")){
            if(data.startsWith("1")) {
                warnInterval = true;
                Toast.makeText(MainActivity.this, "45 second interval notification activated", Toast.LENGTH_SHORT).show();
            }
            else if(data.startsWith("0")) {
                warnInterval = false;
                Toast.makeText(MainActivity.this, "45 second interval notification deactivated", Toast.LENGTH_SHORT).show();
            }
        }else if(data.contains("vib")){
            if(data.startsWith("1")) {
                boolVib = true;
                Toast.makeText(MainActivity.this, "Haptic feedback activated", Toast.LENGTH_SHORT).show();
            }
            else {
                boolVib = false;
                Toast.makeText(MainActivity.this, "Haptic feedback deactivated", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
