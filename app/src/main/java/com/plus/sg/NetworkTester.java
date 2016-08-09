package com.plus.sg;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Mansib on 30/7/2016.
 */

public class NetworkTester {

    public boolean TestNetwork () {
        final CountDownLatch latch = new CountDownLatch(1);
        final boolean[] value = new boolean[1];
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL("http://api.nea.gov.sg/api/");
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setConnectTimeout(10000);
                    urlConnection.connect();
                    value[0] = (urlConnection.getResponseCode() == 403);
                }
                catch (IOException e) {
                    e.printStackTrace();
                    value[0] = false;
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
        return value[0];
    }

}
