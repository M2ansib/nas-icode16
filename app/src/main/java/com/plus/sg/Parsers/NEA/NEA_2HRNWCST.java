package com.plus.sg.Parsers.NEA;

import android.content.Context;
import android.util.Log;

import com.plus.sg.DataStorage;
import com.plus.sg.R;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Mansib on 24/7/2016.
 */

public class NEA_2HRNWCST {

    public ArrayList<String> Array_Area = new ArrayList<>();
    public ArrayList<String> Array_Latitude = new ArrayList<>();
    public ArrayList<String> Array_Longitude = new ArrayList<>();
    public ArrayList<String> Array_Forecast = new ArrayList<>();
    public String ForecastIssue_Time;
    public String ForecastIssue_Date;
    private XmlPullParserFactory xmlFactoryObject;
    public HttpURLConnection conn;
    public volatile boolean parsingComplete = false;
    public volatile boolean showData = false;
    public boolean forceDownload = false;
    public DataStorage dataStorage;
    public boolean ExceptionEncountered;
    private String ErrorStatus;
    public String Error_Title;
    public String Error_Message;
    private Context context;

    public NEA_2HRNWCST (Context current) {
        context = current;
    }

    public void SetDataStorage (DataStorage storage) {
        dataStorage = storage;
    }

    public void Parse (XmlPullParser myParser) {
        int event;
        String text=null;

        try {
            event = myParser.getEventType();

            while (event != XmlPullParser.END_DOCUMENT) {
                String name=myParser.getName();

                switch (event){
                    case XmlPullParser.START_TAG:
                        break;

                    case XmlPullParser.TEXT:
                        text = myParser.getText();
                        break;

                    case XmlPullParser.END_TAG:

                        if (name.equals("forecastIssue")) {
                            ForecastIssue_Date = myParser.getAttributeValue(null, "date");
                            ForecastIssue_Time = myParser.getAttributeValue(null, "time");
                        }

                        else if(name.equals("area")){
                            Array_Area.add(myParser.getAttributeValue(null, "name"));
                            Array_Forecast.add(myParser.getAttributeValue(null, "forecast"));
                            Array_Latitude.add(myParser.getAttributeValue(null, "lat"));
                            Array_Longitude.add(myParser.getAttributeValue(null, "lon"));
                        }

                        break;
                }
                event = myParser.next();
            }
            parsingComplete = true;
        }
        catch (Exception e) {
            e.printStackTrace();
            if (ErrorStatus.equals("")) {
                ErrorStatus = "ERR_PARSE_FAILED";
            }
            ExceptionEncountered = true;
        }
    }

    public void Fetch () {
        final CountDownLatch latch = new CountDownLatch(1);
        final boolean[] value = new boolean[2];
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    dataStorage.StoreData(DataStorage.DataType.THNC);
                    File data = dataStorage.DATA_2HRNWCST;
                    if (dataStorage.CreateNew[0] || forceDownload) {
                        FileOutputStream dataOutput = new FileOutputStream(data);

                        URL url = new URL(context.getResources().getString(R.string.URL_NEA_2HRNWCST));
                        conn = (HttpURLConnection) url.openConnection();
                        conn.setReadTimeout(10000);
                        conn.setConnectTimeout(15000);
                        conn.setRequestMethod("GET");
                        conn.setDoInput(true);
                        conn.connect();

                        Log.d("CONNECT", "Success!");

                        InputStream stream = conn.getInputStream();
                        int bytesRead = -1;
                        byte[] buffer = new byte[8 * 1024];
                        while ((bytesRead = stream.read(buffer)) != -1) {
                            dataOutput.write(buffer, 0, bytesRead);
                        }

                        dataOutput.close();

                        InputStream dataInput = new FileInputStream(data);

                        xmlFactoryObject = XmlPullParserFactory.newInstance();
                        XmlPullParser parser = xmlFactoryObject.newPullParser();
                        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                        parser.setInput(dataInput, null);
                        Parse(parser);
                        if(Array_Area.size() < 47 && !forceDownload) {
                            forceDownload = true;
                            Fetch();
                        }
                        else if (Array_Area.size() < 47 && forceDownload) {
                            ErrorStatus = "ERR_DLOAD_FAILED";
                            value[1] = true;
                        }
                        stream.close();
                        dataInput.close();
                    } else {
                        InputStream dataInput = new FileInputStream(data);
                        xmlFactoryObject = XmlPullParserFactory.newInstance();
                        XmlPullParser myparser = xmlFactoryObject.newPullParser();
                        myparser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                        myparser.setInput(dataInput, null);
                        Parse(myparser);
                        if (Array_Area.size() < 47) {
                            Array_Area.clear();
                            if (ErrorStatus.equals("")) {
                                ErrorStatus = "ERR_NODATA";
                            }
                            value[1] = true;
                        }
                        dataInput.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    value[1] = true;
                    if (ErrorStatus.equals("")) {
                        ErrorStatus = "ERR_CONN_FAILED";
                    }
                }
                if (!value[1]) {
                    value[0] = true;
                }
                latch.countDown();
            }
        });
        thread.start();
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        showData = value[0];
        ExceptionEncountered = value[1];
        if (ExceptionEncountered) {
            ExceptionHandler();
        }
    }

    public void ExceptionHandler () {
        Error_Title = "Whoops!";
        if (ErrorStatus == "ERR_PARSE_FAILED") {
            Error_Message = "SG+ encountered an exception while trying to retrieve data from the Internet.\n\nPlease execute the action that caused the error again.\nIf this error persists, please report it to us.";
        }
        else if (ErrorStatus == "ERR_DLOAD_FAILED") {
            Error_Message = "SG+ couldn't retrieve the requested data.\n\nPlease ensure that your connection to the Internet is stable, then try again.";
        }
        else if (ErrorStatus == "ERR_NODATA") {
            Error_Message = "It seems that you're using SG+ for the first time...\n\nPlease use SG+ in Online mode atleast once before using it in Offline mode.";
        }
        else if (ErrorStatus == "ERR_CONN_FAILED") {
            Error_Message = "SG+ couldn't connect to the servers.\n\nPlease ensure that your connection to the Internet is stable, then try again.";
        }
        Error_Message += "\n(" + ErrorStatus + ")";
    }

}
