package com.seagate.bingo;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Muhammad Workstation on 11/11/2016.
 */

public class Session {
    private ArrayList<Integer> bLimit, iLimit, nLimit, gLimit, oLimit;
    private ArrayList<Integer> bCol, iCol, nCol, gCol, oCol;
    private ArrayList<Integer> callerArray;
    private int callCount = 0;
    private SoundPool soundPool;
    private Context mContext;
    private String filename;
    private Handler handler;
    private SessionListener listener;
    private ArrayList<Cell> cells;
    private int activeNumber;

    public Session(Context context, SessionListener listener) {

        mContext = context;
        this.listener = listener;
        cells = new ArrayList<>();
        createSoundPool();
        makeBingoLimits();
        generateColNumbers();
        createArray();
        startHandler();


    }

    public interface SessionListener {
         void onPlaySound(String name,int value);
    }

    private void createSoundPool() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            SoundPool.Builder builder = new SoundPool.Builder();

            AudioAttributes.Builder audioAttsBuilder = new AudioAttributes.Builder();
            audioAttsBuilder.setUsage(AudioAttributes.USAGE_GAME);
            audioAttsBuilder.setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION);
            AudioAttributes audioAttributes = audioAttsBuilder.build();
            builder.setAudioAttributes(audioAttributes);
            soundPool = builder.build();


        } else {
            soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        }
    }

    private void createArray() {
        callerArray = new ArrayList<>();
        for (int i = 0; i < 75; i++) {
            callerArray.add(i + 1);
        }
        Collections.shuffle(callerArray);


    }

    private int getNextNumber() {
        int i;
        if (callerArray.size()>0) {
             i = callerArray.get(0);
            callerArray.remove(0);
            activeNumber = i;
        }else {
            stopSoundPool();
            i=-1;
        }

        return i;
    }

    private void startHandler() {
        handler = new Handler();
        handler.postDelayed(voiceTask, 1000);
    }

    Runnable voiceTask = new Runnable() {
        @Override
        public void run() {
            playSound();
            handler.postDelayed(this, REF.SOUND_DELAY_TIME);
        }
    };

    private void playSound() {
        int id = getResourceIdFromName(getNextFileName());
        soundPool.load(mContext, id, 1);
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                soundPool.play(sampleId, 1, 1, 1, 0, 1);
                listener.onPlaySound(filename ,activeNumber);
            }
        });
    }

    private int getResourceIdFromName(String name) {
        if (name != null) {
            return mContext.getResources().getIdentifier(name, "raw", mContext.getPackageName());
        }
        return -1;
    }

    private void generateColNumbers() {
        bCol = bLimit;
        iCol = iLimit;
        nCol = nLimit;
        gCol = gLimit;
        oCol = oLimit;
        Collections.shuffle(bCol);
        Collections.shuffle(iCol);
        Collections.shuffle(nCol);
        Collections.shuffle(gCol);
        Collections.shuffle(oCol);

        for (int i = bLimit.size() - 1; i >= 5; i--) {
            bCol.remove(i);
            iCol.remove(i);
            nCol.remove(i);
            gCol.remove(i);
            oCol.remove(i);
        }
        ArrayList<ArrayList<Integer>> arrayLists = new ArrayList<>();
        arrayLists.add(bCol);
        arrayLists.add(iCol);
        arrayLists.add(nCol);
        arrayLists.add(gCol);
        arrayLists.add(oCol);
        int j = 1;
        for (ArrayList<Integer> list : arrayLists) {
            int count = 1;
            for (Integer i : list) {
                Cell cell = new Cell(count, j, i, false, false);
                count++;
                cells.add(cell);
            }
            j++;
        }
    }

    public void stopSoundPool() {
        soundPool.stop(getResourceIdFromName(filename));
        handler.removeCallbacks(voiceTask);
    }


    public ArrayList<Cell> getCells() {
        return cells;
    }

    public void setCells(ArrayList<Cell> cells) {
        this.cells = cells;
    }

    public ArrayList<Integer> getBCol() {
        return bCol;
    }


    public ArrayList<Integer> getICol() {
        return iCol;
    }


    public ArrayList<Integer> getNCol() {
        return nCol;
    }


    public ArrayList<Integer> getGCol() {
        return gCol;
    }


    public ArrayList<Integer> getOCol() {
        return oCol;
    }

    private void makeBingoLimits() {
        bLimit = new ArrayList<>();
        iLimit = new ArrayList<>();
        nLimit = new ArrayList<>();
        gLimit = new ArrayList<>();
        oLimit = new ArrayList<>();
        int bCounter = 1, iCounter = 16, nCounter = 31, gCounter = 46, oCounter = 61;
        for (int i = 0; i < 15; i++) {
            bLimit.add(bCounter);
            iLimit.add(iCounter);
            nLimit.add(nCounter);
            gLimit.add(gCounter);
            oLimit.add(oCounter);
            bCounter++;
            iCounter++;
            nCounter++;
            gCounter++;
            oCounter++;
        }
    }

    private String getNextFileName() {
        int j = getNextNumber();
        filename = null;
        if (j >= 1 && j <= 15) {
            filename = "b" + j;
        } else if (j >= 16 && j <= 30) {
            filename = "i" + j;
        } else if (j >= 31 && j <= 45) {
            filename = "n" + j;
        } else if (j >= 46 && j <= 60) {
            filename = "g" + j;
        } else if (j >= 61 && j <= 75) {
            filename = "o" + j;
        }
        return filename;
    }
}
