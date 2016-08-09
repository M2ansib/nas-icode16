package com.plus.sg;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
//import android.support.design.widget.FloatingActionButton;
//import android.support.design.widget.Snackbar;
import android.support.design.internal.NavigationMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.plus.sg.Parsers.NEA.NEA_2HRNWCST;

import java.io.File;
import java.util.ArrayList;

public class Start extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private ProgressBar ProgressBar_Download;
    private Button fetchData;
    private ListView ListView_2hrnwcst;

    public DataStorage dataStorage;

    private NEA_2HRNWCST NEA_2HRNWCST;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        setTitle("Start");
        NavigationView Nav_View = (NavigationView) findViewById(R.id.nav_view);
        if (Nav_View != null) {
            Nav_View.setCheckedItem(R.id.nav_start);
        }

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

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            });
        }*/

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
            this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        if (drawer != null) {
            drawer.setDrawerListener(toggle);
        }
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
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
                            AlertDialog alertDialog = new AlertDialog.Builder(Start.this).create();
                            alertDialog.setTitle(NEA_2HRNWCST.Error_Title);
                            alertDialog.setMessage(NEA_2HRNWCST.Error_Message);
                            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });
                            Log.e("ERROR", NEA_2HRNWCST.Error_Message);
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

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.start, menu);
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
        } else if (id == R.id.action_deletefile) {
            File file = new File("data/data/com.plus.sg/data_2hrnwcst");
            if (file.exists()) {
                file.delete();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.

        int id = item.getItemId();

        if (id == R.id.nav_weather) {
            // Handle the weather action
        } else if (id == R.id.nav_traffic) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null) {
            drawer.closeDrawer(GravityCompat.START);
        }
        return true;
    }
}
