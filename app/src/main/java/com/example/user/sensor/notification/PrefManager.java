package com.example.user.sensor.notification;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by USER on 1/16/2018.
 */

public class PrefManager {
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context _context;
    /**
     * save the token in the database
     */
    // shared pref mode
    private int PRIVATE_MODE = 0;

    private static final String PREF_TOKEN = "token_id";

    private static final String TOKEN_ID = "TokenID";

    public PrefManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_TOKEN, PRIVATE_MODE);
        editor = pref.edit();
    }


    /**
     * saveing token to pref
     * @param token token of user
     */
    public void setToken(String token) {
        editor.putString(TOKEN_ID, token);
        editor.commit();
    }

    /**
     * get token that saved
     * @return token
     */
    public String getToken() {
        return pref.getString(TOKEN_ID, null);
    }
}

