package com.plus.sg;

import android.app.Activity;
import android.content.Context;

import com.plus.sg.Parsers.NEA.NEA_2HRNWCST;

/**
 * Created by Mansib on 7/8/2016.
 */
public class Initializer {

    public NEA_2HRNWCST NEA_2HRNWCST;
    public DataStorage dataStorage;

    public void Initialize(Context context) {
        NEA_2HRNWCST = new NEA_2HRNWCST(context);
        dataStorage = new DataStorage();
        NEA_2HRNWCST.SetDataStorage(dataStorage);
    }

}
