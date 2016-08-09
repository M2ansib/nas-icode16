package com.plus.sg;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Mansib on 29/7/2016.
 */

public class DataStorage {

    public enum DataType{THNC, FDO, HRW, UVI, EA, PSIU, PMTPFU}
    public File DATA_2HRNWCST; // 2-hour Nowcast [0]
    public File DATA_4DYOUTLK; // 4-day Outlook [1]
    public File DATA_HVRNWARN; // Heavy Rain Warning [2]
    public File DATA_UVINDEX0; // UV Index [3]
    public File DATA_EARTHQUK; // Earthquake [4]
    public File DATA_PSIUPDTE; // PSI Update [5]
    public File DATA_PM25UPDT; // PM 2.5 Update [6]
    public String File_LastModified;
    public boolean connectionEstablished;

    public String Debug_FileLocationMessage;

    public boolean[] CreateNew;

    public void StoreData (DataType dataType) {
        String storageLocation = "data/data/com.plus.sg/";
        CreateNew = new boolean[8];
        connectionEstablished = false;

        if (dataType == DataType.THNC) {
            DATA_2HRNWCST = new File(storageLocation + "data_2hrnwcst");
            CreateNew[0] = CreateFile(DATA_2HRNWCST, new SimpleDateFormat("hh dd-MM-yyyy"));
        }
        else if (dataType == DataType.FDO) {
            DATA_4DYOUTLK = new File(storageLocation + "data_4dyoutlk");
            CreateNew[1] = CreateFile(DATA_4DYOUTLK, new SimpleDateFormat("hh dd-MM-yyyy"));
        }
        else if (dataType == DataType.HRW) {
            DATA_HVRNWARN = new File(storageLocation + "data_hvrnwarn");
            CreateNew[2] = CreateFile(DATA_HVRNWARN, new SimpleDateFormat("hh dd-MM-yyyy"));
        }
        else if (dataType == DataType.UVI) {
            DATA_UVINDEX0 = new File(storageLocation + "data_uvindex0");
            CreateNew[3] = CreateFile(DATA_UVINDEX0, new SimpleDateFormat("hh dd-MM-yyyy"));
        }
        else if (dataType == DataType.EA) {
            DATA_EARTHQUK = new File(storageLocation + "data_earthquk");
            CreateNew[4] = CreateFile(DATA_EARTHQUK, new SimpleDateFormat("hh dd-MM-yyyy"));
        }
        else if (dataType == DataType.PSIU) {
            DATA_PSIUPDTE = new File(storageLocation + "data_psiupdte");
            CreateNew[5] = CreateFile(DATA_PSIUPDTE, new SimpleDateFormat("hh dd-MM-yyyy"));
        }
        else if (dataType == DataType.PMTPFU) {
            DATA_PM25UPDT = new File(storageLocation + "data_pm25updt");
            CreateNew[6] = CreateFile(DATA_PM25UPDT, new SimpleDateFormat("hh dd-MM-yyyy"));
        }

    }

    private boolean CreateFile (File file, SimpleDateFormat format) {

        TimeZone timezone = TimeZone.getTimeZone("GMT+8");
        TimeZone.setDefault(timezone);
        Calendar calendar = Calendar.getInstance();
        File_LastModified = "" + format.format(calendar.getTime());

        boolean bool = false;
        NetworkTester networkTester = new NetworkTester();

        if (!file.exists() || file.length() == 0) {
            try {
                file.createNewFile();
                connectionEstablished = networkTester.TestNetwork();
                if (connectionEstablished) {
                    bool = true;
                    Debug_FileLocationMessage = "Downloading data...";
                }
                else {
                    bool = false;
                    Debug_FileLocationMessage = "No data found.";
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            Date lastModified = new Date(file.lastModified());

            if (format.format(lastModified).equals(File_LastModified)) {
                // If file is up to date, do...
                bool = false;
                Debug_FileLocationMessage = "Retrieving data from local storage...";
            }
            else {
                // If file is not up to date, do...

                connectionEstablished = networkTester.TestNetwork();

                if (connectionEstablished) {
                    Log.e("APPLOG", format.format(lastModified) + " != " + File_LastModified);
                    file.delete();
                    try {
                        file.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    bool = true;
                    Debug_FileLocationMessage = "Downloading data...";
                }
                else {
                    bool = false;
                    Debug_FileLocationMessage = "Retrieving data from local storage...";
                    SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");
                    Debug_FileLocationMessage += "\nWorking in offline mode.\nData shown has not been validated, and may therefore be inaccurate.\nLast update: " + dateFormat.format(file.lastModified());
                }
            }
        }

        Debug_FileLocationMessage += "\nAccessing file located at \"" + file.getAbsolutePath() + "\"...";

        return bool;

    }

}
