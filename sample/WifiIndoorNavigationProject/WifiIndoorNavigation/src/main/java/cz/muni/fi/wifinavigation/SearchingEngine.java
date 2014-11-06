package cz.muni.fi.wifinavigation;

import android.content.Context;
import android.hardware.SensorManager;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.muni.fi.model.RssFingerprint;

/**
 * Created by Michal on 24.12.13.
 */
public class SearchingEngine {

    private final String TAG = "SearchingMachine";
    private SensorManager mSensorManager;
    private Context context;
    private ScanningEngine mScanningEngine;
    private final double BETA = 0.1;

    Map<String, Double> piVectors;

    public SearchingEngine(SensorManager mSensorManager, Context context) {
        this.mSensorManager = mSensorManager;
        this.context = context;
        mScanningEngine = WifiNavigationActivity.mScanningEngine;
    }

    public String getPosition(String rp) {
        final List<String> data = new ArrayList<String>();

        if (piVectors == null) {
            Log.d(TAG, "initializing piVectors");
            initializePiVector();
        }

        for (int i = 0; i < 1; i++) {
            data.add(getOnePosition(rp));
        }


        int count = 0;
        int maxCount = 0;
        String element = data.get(0);


        if (element == null) return "N/A";

        String mostOccurringElement = element;

        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).equals(element)){
                count++;
                if (count > maxCount) {
                    maxCount = count;
                    mostOccurringElement = element;
                }
            } else {
                count = 1;
            }
            element = data.get(i);
        }

        return mostOccurringElement;
    }

    public String getOnePosition(String accurateRp) {
        Map<String, Double> probability = new HashMap<String, Double>();
        List<RssFingerprint> actualScan = mScanningEngine.getScanResult();
        Map<String, List<RssFingerprint>> database = WifiNavigationActivity.datasource.getSortedMergedFingerprints(mScanningEngine.getOrientation());
        //Map<String, List<RssFingerprint>> database = WifiNavigationActivity.datasource.getSortedFingerprints(mScanningEngine.getOrientation());
        DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd-HH_mm_ss");
        Date d = new Date();

        String time = dateFormat.format(d);
        String textToWrite = time + ";" + accurateRp +";";

        for (Map.Entry<String, List<RssFingerprint>> entry : database.entrySet()) {
            double tempProbability = 1.0;
            //textToWrite += "\n counting pst for RP: " + entry.getKey() + "\n";
            //Log.d(TAG, "counting pst for RP: " + entry.getKey());
            textToWrite += entry.getKey() + ";";
            for (RssFingerprint apDatabase : entry.getValue()) {
                if (tempProbability == 0) break;

                for (RssFingerprint apActual : actualScan) {
                    if (tempProbability == 0) break;

                    if (apActual.getBssid().equals(apDatabase.getBssid())) {
                        //Log.d(TAG, "values for db: " + apDatabase.getBssid() + " and ac: " + apActual.getBssid() + " with" +
                        //        " level: " + apActual.getAverage() + ", average: " + apDatabase.getAverage() + ", deviation: " + apDatabase.getDeviation() + ", is: ");

                        textToWrite += "[" + apActual.getBssid() + "," + apDatabase.getBssid() + "],";

                        try {
                            double pstDenominator = calcSumPst(apDatabase.getAverage(), apDatabase.getDeviation());
                            //Log.d(TAG, "pstDenominator is: " + pstDenominator);

                            double pstNumerator = calculateIntegral(apActual.getAverage(), apDatabase.getAverage(), apDatabase.getDeviation());
                            //Log.d(TAG, "pstNumerator is: " + pstNumerator);

                            double pst = pstNumerator / pstDenominator;
                            //Log.d(TAG, "PST is: " + pst);
                            textToWrite += pst + ";";

                            tempProbability *= pst;
                            //Log.d(TAG, "total probability is: " + tempProbability);

                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                }
            }
            probability.put(entry.getKey(), tempProbability);
        }

        textToWrite += probability.toString();
        //Log.d(TAG, probability.toString());

        /*//BAYES
        double bayesDenominator = 0;
        Map<String, Double> vectors = new HashMap<String, Double>();

        Log.d(TAG, "piVectors before update: " + piVectors.toString());*/

        /*//Vypocet bayesova jmenovatele
        for (String s : probability.keySet()) {
            bayesDenominator += piVectors.get(s) * probability.get(s);
        }

        //Aktualizace vektoru
        for (Map.Entry<String, Double> entry : probability.entrySet()) {
            double vector = piVectors.get(entry.getKey());

            vectors.put(entry.getKey(), (vector * entry.getValue()) / bayesDenominator);

        }

        //piVectors = vectors;
        Log.d(TAG, "piVectors after update: " + vectors.toString());*/

        double temp = Double.MIN_VALUE;
        String rp = null;

        for (Map.Entry<String, Double> entry : probability.entrySet()) {
            if (entry.getValue() > temp) {
                temp = entry.getValue();
                rp = entry.getKey();
            }
        }

        writeToFile(textToWrite, time);
        return rp;
    }

    //aproximace ERF(x)
    public double calculateERF(double number) {
        final double ALFA = 0.14;
        double exponentNumerator = (-(number * number)) * ((4 / Math.PI) + (ALFA * number * number));
        double exponentDenominator = 1 + ALFA * number * number;
        double erf = Math.pow(1 - Math.pow(Math.E, exponentNumerator / exponentDenominator), 0.5);

        return erf;
    }

    //integral MATLAB
    public double calculateIntegral(int level, int average, int deviation) {
        double firstParam = (0.25) * ((Math.sqrt(2) * (-2 * level + 1 + 2 * average)) / (deviation));
        double secondParam = (0.25) * ((Math.sqrt(2) * (-2 * level - 1 + 2 * average)) / (deviation));

        double integral = (0.5 * calculateERF(firstParam)) - (0.5 * calculateERF(secondParam));

        return integral + BETA;
    }

    //integral WOLFRAM
    public double calculateIntegralTemp(int level, RssFingerprint ap) {
        double firstParam = (ap.getAverage() - level - 0.5) / (Math.sqrt(2) * ap.getDeviation());
        double secondParam = (ap.getAverage() - level + 0.5) / (Math.sqrt(2) * ap.getDeviation());

        Log.d(TAG, "first param for ap: " + ap.getBssid() + " is: " + firstParam);
        Log.d(TAG, "second param for ap: " + ap.getBssid() + " is: " + secondParam);

        double integral = 0.5 * (calculateERF(firstParam) - calculateERF(secondParam));
        Log.d(TAG, "integral for ap: " + ap.getBssid() + " is: " + integral);
        if (integral == 0) return 1;
        return integral;
    }

    public double calcSumPst(int average, int deviation) {
        double pst = 0.0;

        for (int i = 0; i <= 100; i++) {
            pst += calculateIntegral(i, average, deviation);
        }

        return pst;
    }

    private void initializePiVector() {
        piVectors = new HashMap<String, Double>();
        Map<String, List<RssFingerprint>> database = WifiNavigationActivity.datasource.getSortedMergedFingerprints(mScanningEngine.getOrientation());

        double vector = (double) 1 / database.size();

        for (String s : database.keySet()) {
            piVectors.put(s, vector);
        }
    }

    public boolean writeToFile(String text, String nameFile) {
        String name = "/win.txt";
        //String name = "/" + nameFile + ".txt";
        Log.d(TAG, name);
        try
        {

            String fullPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            File dir = new File(fullPath + File.separator + "/Logs");

            if (dir.exists()) {
                Log.d(TAG, "directory exists");
            } else {
                dir.mkdir();
                Log.d(TAG, "creating directory");
            }

            File myFile = new File(fullPath + File.separator + "/Logs" + name);

            if (myFile.exists()) {
                Log.d(TAG, "file exists");
            } else {
                myFile.createNewFile();
                Log.d(TAG, "creating file");
            }

            Log.d(TAG, context.getFilesDir().toString());

            FileOutputStream fOut = new FileOutputStream(myFile, true);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            BufferedWriter bw = new BufferedWriter(myOutWriter);
            try {
                bw.write(text);
                bw.newLine();
            }
            finally {
                bw.close();
            }
            return true;
        }
        catch (Exception e)
        {
            return false;
            //do your stuff here
        }
    }
}