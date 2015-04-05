package ca.rjreid.guitartuner;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import org.puredata.android.io.AudioParameters;
import org.puredata.android.service.PdService;
import org.puredata.android.utils.PdUiDispatcher;
import org.puredata.core.PdBase;
import org.puredata.core.PdListener;
import org.puredata.core.utils.IoUtils;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

public class MainActivity extends ActionBarActivity {

    private PdUiDispatcher dispatcher;
    private PdService pdService = null;
    private TextView nearestNote;
    private TextView notePitch;
    private TextView currentPitch;

    protected int tunePitch;

    private int [] lowNotes =  {31,32,33,34,35,36,37,38,39,40,41,42};
    private int [] bassNotes = {43,44,45,46,47,48,49,50,51,52,53,54};
    private int [] middleNotes = {55,56,57,58,59,60,61,62,63,64,65,66};
    private int [] trebleNotes = {67,68,69,70,71,72,73,74,75,76,77,78};
    private int [] highNotes = {79,80,81,82,83,84,85,86,87,88,89,90};
    private String [] noteNames = {"G ","G# ", "A ", "A# ", "B ", "C ", "C# ", "D ", "D# ", "E ", "F ", "F# "};
    private double bound = 0.5;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initGui();
        bindService(new Intent(this, PdService.class), pdConnection, BIND_AUTO_CREATE);
    }

    private void initGui() {
        setContentView(R.layout.activity_main);
        nearestNote = (TextView) findViewById(R.id.nearestNote);
        notePitch = (TextView) findViewById(R.id.notePitch);
        currentPitch = (TextView) findViewById(R.id.currentPitch);
    }

    private void initPd() throws IOException {
        int sampleRate = AudioParameters.suggestSampleRate();
        pdService.initAudio(sampleRate, 1, 2, 10.0f);


        dispatcher = new PdUiDispatcher();

        dispatcher.addListener("pitch", new PdListener.Adapter() {
            @Override
            public void receiveFloat(String source, float x) {
                whatNote(x);
            }
        });

        PdBase.setReceiver(dispatcher);

        start();
    }

    private void start() {
        if (!pdService.isRunning()) {
            Intent intent = new Intent(MainActivity.this, MainActivity.class);
            pdService.startAudio(intent, R.drawable.icon, "GuitarTuner", "Return to GuitarTuner");
        }
    }

    private void loadPatch() throws IOException {
        File dir = getFilesDir();
        IoUtils.extractZipResource(getResources().openRawResource(R.raw.patches), dir, true);
        File patchFile = new File(dir, "tuner.pd");
        PdBase.openPatch(patchFile.getAbsolutePath());
    }

    public int getTune(int x)
    {
        tunePitch = x;
        return tunePitch;
    }

    private void whatNote(float x) {

        if(x >lowNotes[0]-bound && x <=lowNotes[lowNotes.length-1]+bound) {
            pitchNotes(lowNotes, x);
        }else if(x >bassNotes[0]-bound && x <=bassNotes[bassNotes.length-1]+bound) {
            pitchNotes(bassNotes, x);
        }else if(x >middleNotes[0]-bound && x <=middleNotes[middleNotes.length-1]+bound) {
            pitchNotes(middleNotes, x);
        }else if(x >trebleNotes[0]-bound && x <=trebleNotes[trebleNotes.length-1]+bound) {
            pitchNotes(trebleNotes, x);
        }else if(x >highNotes[0]-bound && x <=highNotes[highNotes.length-1]+bound) {
            pitchNotes(highNotes, x);
        }
    }

    private void pitchNotes(int [] octave, float x)
    {
        for(int i = 0; i < octave.length; i++)
        {
            if(x >octave[i]-bound && x <=octave[i]+bound)
            {
                nearestNote.setText(noteNames[i]);
                notePitch.setText("" + octave[i]);
                getTune(octave[i]);
                formatCurrentPitch(x);
                inTune(x,tunePitch);
            }
        }
    }

    protected void formatCurrentPitch(float x)
    {
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        currentPitch.setText(""+ df.format(x));
    }

    public void inTune(float x, int tunePitch)
    {
        if(x >= tunePitch - 0.1 && x <= tunePitch + 0.1) {
            currentPitch.setTextColor(Color.GREEN);
            currentPitch.setText("In Tune");
        } else {
            currentPitch.setTextColor(Color.WHITE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService(pdConnection);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private final ServiceConnection pdConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            pdService = ((PdService.PdBinder)service).getService();
            try {
                initPd();
                loadPatch();
            } catch (IOException e) {
                Log.e("", e.toString());
                finish();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // this will never be called
        }
    };











































}




