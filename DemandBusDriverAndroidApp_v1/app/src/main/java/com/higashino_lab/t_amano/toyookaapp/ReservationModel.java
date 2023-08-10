package com.higashino_lab.t_amano.toyookaapp;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ReservationModel implements Serializable {

    private JSONObject reservationJson;
    private JSONObject userJson;
    private JSONObject stopJson;
    private JSONObject stopLocationJson;

    public String state;

    public String stopTimeString;
    public double longitude = 0;
    public double latitude = 0;

    public String busTimeType = "";
    public String busType;


    private String selectBusTimeType(String stopTimeString, String busType) {

        if (busType.equals("going")) {
            if (stopTimeString.compareTo("08:30:00") >= 0 && stopTimeString.compareTo("09:00:00") < 0) {
                return "going1";
            } else if (stopTimeString.compareTo("09:00:00") >= 0 && stopTimeString.compareTo("10:00:00") < 0) {
                return "going2";
            } else if (stopTimeString.compareTo("12:30") >= 0 && stopTimeString.compareTo("13:00:00") < 0) {
                return "going3";
            }
        } else {
            if (stopTimeString.compareTo("10:30:00") >= 0 && stopTimeString.compareTo("11:00:00") < 0) {
                return "returning1";
            } else if (stopTimeString.compareTo("13:30:00") >= 0 && stopTimeString.compareTo("14:00:00") < 0) {
                return "returning2";
            } else if (stopTimeString.compareTo("16:30:00") >= 0 && stopTimeString.compareTo("17:00:00") < 0) {
                return "returning3";
            }
        }
        return "";
    }

    public ReservationModel(JSONObject reservationJson) {

        this.reservationJson = reservationJson;
        try {
            this.userJson = reservationJson.getJSONObject("user");
            this.stopJson = reservationJson.getJSONObject("stop");
            this.stopLocationJson = reservationJson.getJSONObject("stop_location");
            this.state = reservationJson.getString("state");

            this.stopTimeString = stopJson.getString("stop_time");
            this.longitude = stopLocationJson.getDouble("longitude");
            this.latitude = stopLocationJson.getDouble("latitude");

            this.busType = reservationJson.getString("bus_type");
            this.busTimeType = selectBusTimeType(stopTimeString, busType);

        } catch (JSONException e) {

        }
    }

    @Override
    public String toString() {
        return "sopt";
    }

    public int getReservationId() {
        int id = -1;

        try{
            id = this.reservationJson.getInt("id");
        } catch (JSONException e) {

        }
        return id;

    }

    public String getUserName() {

        String userName = "";
        try{
            userName = this.userJson.getString("screen_name");
        } catch (JSONException e) {
            Log.d("reservation model", "JSONE");

        }
        return userName;
    }

    public String getStopTime() {
        String[] splitedStopTime = stopTimeString.split(":");
        return splitedStopTime[0] + "時" + splitedStopTime[1] + "分";
    }

    public String getStopName() {

        String busType = "";
        String stopName = "";
        String resultString = "";
        try {
            stopName = stopLocationJson.getString("name");
            busType = reservationJson.getString("bus_type");
            if (busType.equals("going")) {
                resultString = "往路 - " + stopName;

            } else if (busType.equals("returning")) {
                resultString = "復路 - " + stopName;
            }

        } catch (JSONException e) {
            Log.d("reservation model", "JSONE");

        }
        return resultString;
    }


    public boolean isGoing() {
        if (busType.equals("going")) {
            return true;

        } else {
            return false;
        }
    }
}

class ReservationComparator implements java.util.Comparator {
    public int compare(Object s, Object t) {

        String time1 = ((ReservationModel) s).getStopTime();
        String time2 = ((ReservationModel) t).getStopTime();

        return time1.compareTo(time2);
    }
}
