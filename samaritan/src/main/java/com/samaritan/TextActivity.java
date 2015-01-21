// Original author
// /u/thetorine
// Created app interface.
// Basically made this entire app possible
//
// Contributing authors
// /u/personofinterestfan
// Added comments
// Removed ability to add phrases to simplify interface.
// Added speech recognition and simple response.
//
//

package com.samaritan;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;

import com.samaritan.animations.AnimateAlpha;
import com.samaritan.animations.AnimateLine;
import com.samaritan.animations.AnimateScale;
import com.samaritan.animations.AnimateText;
import com.samaritan.utilities.DynamicTextView;
import com.samaritan.utilities.Storage;
import com.thetorine.samaritan.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

public class TextActivity extends Activity implements Runnable {
    // Constants that will not be changed.
    private final int DISPLAY_TIME = 500;

    private final String[] ADMIN_IDENTITY = {"Arthur Claypool", "Henry McCarthy", "Rudigar Smoot"};

    // Group 1: Repeat
    // Group 2: Greetings
    // Group 3: Identify yourself
    // Group 4: Time?
    // Group 5: Tell me what's next
    // Group 6: Say a name.
    private static final String[][] INPUT_IDENTIFIER = new String[][]{
            {"repeat"},
            {"hi","hello"},
            {"your name","address you","identify","who are you"},
            {"time"},
            {"your command"},
            {"shaw", "sameen shaw", "samantha groves", "groves", "harold finch", "finch", "john reese","reese"},
            {"who is admin", "identify admin"},
            {"help", "protect", "protection", "assistance", "send back up"},
            {"tell me a joke"}
    };

    // Samaritan responses:
    // Find the machine
    // Calculating response
    // Investigation ongoing
    // What are your commands?
    // I will protect you now.


    // Behind-the-scenes objects/variables.
    private List<List<String>> mWords = new ArrayList<>();
    private int mWordIndex;
    private int loopIndex = -1;
    private Handler mHandler = new Handler();
    private boolean mDestroyed;
    private boolean mRun;
    private boolean mTriangleStatus;
    public static Storage storage;

    // Objects from XML view.
    private ImageView triangle;

    // Speech recognizer
    private SpeechRecognizer sr = null;
    private Intent recognizerIntent = null;
    private String spokenWords = "";
    private String lastCommand = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text);

        // Set color scheme
        this.setTheme(R.style.TextBlackTheme);
        View view = findViewById(R.id.black_line);
        view.setBackgroundColor(getResources().getColor(android.R.color.white));

        DynamicTextView textView = (DynamicTextView) findViewById(R.id.displayText);
        textView.setTextColor(getResources().getColor(android.R.color.white));

        // Set font
        Typeface font = Typeface.createFromAsset(this.getAssets(), "fonts/magdacleanmono-regular.otf");
        ((DynamicTextView) findViewById(R.id.displayText)).setTypeface(font);

        storage = new Storage();
        mHandler.postDelayed(this, 100);

        // Fetch triangle from view
        triangle = (ImageView) findViewById(R.id.triangle);

        // Speech recognizer
        sr = SpeechRecognizer.createSpeechRecognizer(this);
        sr.setRecognitionListener(new SpeechListener());
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mDestroyed = true;
        finish();
    }

    // Private inner class for speech recognition
    private class SpeechListener implements RecognitionListener {
        @Override
        public void onResults(Bundle results) {
            // Pick the first match
            ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            spokenWords = matches.get(0).toLowerCase();

            if (!mRun) {
                boolean guessedPhrase= false;

                // Guess sentence.
                int i = 0;
                for(String[] group : INPUT_IDENTIFIER){
                    for(String input : group) {
                        if (spokenWords.contains(input)) {
                            switch(i) {
                                case 0: // User requests repeat message
                                    break;
                                case 1: // Greetings
                                    lastCommand = input;
                                    break;
                                case 2: // Ask for name.
                                    lastCommand = "I am Samaritan";
                                    break;
                                case 3: // Get current time.
                                    Calendar calendar = Calendar.getInstance();
                                    lastCommand = calendar.getTime().toString().split(" ")[3];
                                    break;
                                case 4: // User mentions Samaritan's commands
                                    lastCommand = "Find the machine";
                                    break;
                                case 5: // User mentions a member of Team Machine
                                    lastCommand = "Disregard Non Threat";
                                    break;
                                case 6: // Identify admin
                                    Random r = new Random();
                                    lastCommand = "Admin is " + ADMIN_IDENTITY[r.nextInt(ADMIN_IDENTITY.length)];
                                    break;
                                case 7: // Request help
                                    if(spokenWords.contains("do not " + input) || spokenWords.contains("don't " + input))
                                        lastCommand = "Ok";
                                    else{
                                        if(spokenWords.contains("her") || spokenWords.contains("him") || spokenWords.contains("them"))
                                            lastCommand = "I will protect them now";
                                        else
                                            lastCommand = "I will protect you now";
                                    }
                                    break;
                                case 8:
                                    lastCommand = "Calculating Response";
                                    break;
                            }

                            guessedPhrase = true;
                            break;
                        }
                    }
                    i++;
                }

                if(!guessedPhrase)
                    lastCommand = "Calculating Response";

                parseText(lastCommand);
                displayPhrase();
            }
        }

        // Unused methods.
        @Override
        public void onBeginningOfSpeech() {}
        @Override
        public void onBufferReceived(byte[] buffer) {}
        @Override
        public void onEndOfSpeech(){}
        @Override
        public void onError(int errorCode){}
        @Override
        public void onEvent(int arg0, Bundle arg1){}
        @Override
        public void onPartialResults(Bundle arg0){}
        @Override
        public void onReadyForSpeech(Bundle arg0){}
        @Override
        public void onRmsChanged(float arg0){}
    }

    @Override
    public void run() {
        DynamicTextView message = (DynamicTextView) findViewById(R.id.displayText);
        if (mRun) {
            if (mWordIndex < mWords.get(loopIndex).size()) {
                changeText(message);
                animateLine();
            } else {
                mRun = false;
            }
            openCloseTriangle(triangle, 0);
            blinkTriangle(triangle);
        } else {
            openCloseTriangle(triangle, 1);
            blinkTriangle(triangle);
            animateLine();
        }
        if (!mDestroyed) {
            mHandler.postDelayed(this, 10);
        }
    }

    // Runs animation for app.
    // The parameter indicates which aspect of the app to animate.
    private void runAnimation(int animation) {
        switch (animation) {
            // Animate text
            case 0: {
                DynamicTextView tv = (DynamicTextView) findViewById(R.id.displayText);
                String word = mWords.get(loopIndex).get(mWordIndex);

                AnimateText animateText = new AnimateText(tv, word);
                animateText.setDuration(DISPLAY_TIME);
                tv.startAnimation(animateText);
                mWordIndex++;
                break;
            }
            // Animate triangles
            case 1: {
                AnimateAlpha animateTriangle = new AnimateAlpha(triangle);
                animateTriangle.setDuration(DISPLAY_TIME);
                triangle.startAnimation(animateTriangle);
                break;
            }
            // I don't know what this is.
            case 2: {
                AnimateScale animateScale = new AnimateScale(triangle, getResources().getDisplayMetrics().density);
                animateScale.setDuration(200);
                triangle.startAnimation(animateScale);
                break;
            }
            // Animate line
            case 3: {
                View view = findViewById(R.id.black_line);

                AnimateLine animateLine = new AnimateLine(view, storage.mWidth);
                animateLine.setDuration((long) Math.ceil(DISPLAY_TIME * 0.3D));
                view.startAnimation(animateLine);
            }
        }
    }

    // Changes the word being displayed.
    private void changeText(DynamicTextView tv) {
        Animation a = tv.getAnimation();
        if (a != null) {
            if (a.hasEnded()) {
                runAnimation(0);
            }
        } else {
            runAnimation(0);
        }
    }

    // Animates the line.
    private void animateLine() {
        if (!(storage.mLastWidth == storage.mWidth)) {
            storage.mLastWidth = storage.mWidth;
            runAnimation(3);
        }
    }

    // Checks whether to open or close the triangle
    private void openCloseTriangle(ImageView iv, int method) {
        if (method == 0) {
            if (!mTriangleStatus) {
                runAnimation(2);
                mTriangleStatus = true;
                iv.setAlpha(1f);
            }
        } else {
            if (mTriangleStatus) {
                runAnimation(2);
                mTriangleStatus = false;
                iv.setAlpha(1f);
                mWordIndex = 0;
            }
        }
    }

    // Blinks the triangle
    private void blinkTriangle(ImageView iv) {
        Animation animation = iv.getAnimation();
        if (animation != null) {
            if (animation.hasEnded()) {
                runAnimation(1);
            }
        } else {
            runAnimation(1);
        }
    }

    // When user taps screen.
    public void onClick(View view) {
        sr.startListening(recognizerIntent);
    }

    // Splits words and punctuation apart, so each word and punctuation is displayed separately.
    public void parseText(String text) {
        String[] loops = text.split("-");
        for (String s : loops) {
            List<String> allWords = new ArrayList<>();
            for (String w : s.toUpperCase().split(" ")) {
                w = " " + w + " ";
                if (w.contains("?")) {
                    allWords.add(w.replace("?", ""));
                    allWords.add(" ? ");
                } else if (w.contains("!")) {
                    allWords.add(w.replace("!", ""));
                    allWords.add(" ! ");
                } else {
                    allWords.add(w);
                }
            }
            allWords.add("   ");
            mWords.add(allWords);
        }
    }
    // Displays the phrase for the variable mWords
    private void displayPhrase(){
        View line = findViewById(R.id.black_line);
        if (line.getAnimation() != null) {
            if (line.getAnimation().hasEnded()) {
                mRun = true;
                loopIndex++;
                if (loopIndex == mWords.size()) {
                    loopIndex = 0;
                }
                mWordIndex = 0;
            }
        } else {
            mRun = true;
            loopIndex++;
            if (loopIndex == mWords.size()) {
                loopIndex = 0;
            }
            mWordIndex = 0;
        }
    }
}
