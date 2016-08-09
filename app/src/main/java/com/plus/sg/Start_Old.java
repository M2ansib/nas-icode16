package com.plus.sg;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.plus.sg.Parsers.NEA.NEA_2HRNWCST;

import java.util.ArrayList;

/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
Datasets            |   8-Character Naming Convention
2-hour Nowcast      |   "2hrnwcst"
24-hour Forecast    |   "24hrfcst"
4-days Outlook      |   "4dyoutlk"
Heavy Rain Warning  |   "hvrnwarn"
UV Index            |   "uvindex0"
Earthquake Advisory |   "earthquk"
PSI Update          |   "psiupdte"
PM 2.5 Update       |   "pm25updt"
+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/
public class Start_Old extends AppCompatActivity {

    private ProgressBar ProgressBar_Download;
    private Button fetchData;
    private ListView ListView_2hrnwcst;

    public DataStorage dataStorage;

    private NEA_2HRNWCST NEA_2HRNWCST;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_old);
        setTitle("Start");
        fetchData = (Button) findViewById(R.id.Button_FetchData);
        ProgressBar_Download = (ProgressBar) findViewById(R.id.ProgressBar_Download);
        ListView_2hrnwcst = (ListView) findViewById(R.id.oo);
        Initializer initializer = new Initializer();
        initializer.Initialize(this);
        dataStorage = initializer.dataStorage;
        NEA_2HRNWCST = initializer.NEA_2HRNWCST;
        CreateArrayAdapter(ListView_2hrnwcst, NEA_2HRNWCST.Array_Area);
        if (fetchData != null) {
            fetchData.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fetchData.setVisibility(View.GONE);
                    ProgressBar_Download.setVisibility(View.VISIBLE);
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            NEA_2HRNWCST.Fetch();
                        }
                    });
                    thread.start();
                    HandleData();
                }
            });
        }
    }

    protected void HandleData () {
        Thread thread = new Thread (new Runnable() {
            public void run() {
                ListView_2hrnwcst = (ListView) findViewById(R.id.oo);
                while (!NEA_2HRNWCST.showData && !NEA_2HRNWCST.ExceptionEncountered) {
                    //Log.e("APPLOG", "Download in progress...");
                }
                if (NEA_2HRNWCST.ExceptionEncountered) {
                    Runnable run = new Runnable() {
                        @Override
                        public void run() {
                            ProgressBar_Download.setVisibility(View.GONE);
                            fetchData.setVisibility(View.VISIBLE);
                            AlertDialog alertDialog = new AlertDialog.Builder(Start_Old.this).create();
                            alertDialog.setTitle("Whoops!");
                            alertDialog.setMessage("It seems like you've started SG+ for the first time...\nPlease use SG+ in Online mode at least once before using it in Offline mode.");
                            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });
                            alertDialog.show();
                        }
                    };
                    runOnUiThread(run);
                    NEA_2HRNWCST.ExceptionEncountered = false;
                }
                else {
                    Runnable run = new Runnable() {
                        @Override
                        public void run() {
                            RefreshList(ListView_2hrnwcst);
                            ProgressBar_Download.setVisibility(View.GONE);
                            Toast.makeText(getApplicationContext(), dataStorage.Debug_FileLocationMessage, Toast.LENGTH_LONG).show();
                        }
                    };
                    runOnUiThread(run);
                }
            }
        });
        thread.start();
    }

    private void RefreshList (ListView listView) {
        ArrayAdapter adapter = (ArrayAdapter) listView.getAdapter();
        adapter.notifyDataSetChanged();
        listView.invalidateViews();
        listView.refreshDrawableState();
    }

    public ArrayAdapter CreateArrayAdapter(ListView listView, ArrayList<String> arrayList) {
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, arrayList);
        listView.setAdapter(arrayAdapter);
        return arrayAdapter;
    }

}
