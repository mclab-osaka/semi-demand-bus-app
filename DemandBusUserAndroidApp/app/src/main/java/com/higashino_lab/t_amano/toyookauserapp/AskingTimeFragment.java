package com.higashino_lab.t_amano.toyookauserapp;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AskingTimeFragment extends Fragment implements Button.OnClickListener {

    private static final String TAG = "AskingTimeFragmnet";
    private static final String ARG_STOPS_TODAY  = "stopTimeList";
    private static final String ARG_STOPS_TOMORROW = "stopTimeListTomorrow";
    private static final String ARG_MESSAGE = "askingMessage";

    private OnAskingTimeFragmentInteractionListener mListener;
    private JSONArray stopTimeTodayJsonList;
    private JSONArray stopTimeTomorrowJsonList;
    private LinearLayout buttonLinearLayout;
    private String askingMessage;

    public AskingTimeFragment() {
        // Required empty public constructor
    }

    public static AskingTimeFragment newInstance(JSONArray stopTimeTodayJsonList, JSONArray stopTimeTomorrowJsonList, String askingMessage) {
        AskingTimeFragment fragment = new AskingTimeFragment();
        Bundle args = new Bundle();
        if (stopTimeTodayJsonList != null) {
            args.putString(ARG_STOPS_TODAY, stopTimeTodayJsonList.toString());
        }

        if (stopTimeTomorrowJsonList!= null) {
            args.putString(ARG_STOPS_TOMORROW, stopTimeTomorrowJsonList.toString());
        }
        args.putString(ARG_MESSAGE, askingMessage);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.askingMessage = getArguments().getString(ARG_MESSAGE);
            String stopJsonTodayListString = getArguments().getString(ARG_STOPS_TODAY);
            String stopJsonTomorrowListString = getArguments().getString(ARG_STOPS_TOMORROW);
            try {
                this.stopTimeTodayJsonList = new JSONArray(stopJsonTodayListString);
                this.stopTimeTomorrowJsonList = new JSONArray(stopJsonTomorrowListString);
            } catch (JSONException e) {
                Log.d(TAG, "JsonException");
                this.stopTimeTodayJsonList = new JSONArray();
                this.stopTimeTomorrowJsonList = new JSONArray();
            }
        } else {
            this.stopTimeTodayJsonList = new JSONArray();
            this.stopTimeTomorrowJsonList = new JSONArray();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_asking_time, container, false);
        TextView message = (TextView) view.findViewById(R.id.asking_time_message);
        message.setText(this.askingMessage);

        buttonLinearLayout = (LinearLayout) view.findViewById(R.id.asking_time_buttons);
        buttonLinearLayout.removeAllViews();
        // 今日の分
        for(int i =0; i < stopTimeTodayJsonList.length(); i++) {
            try {

                JSONObject stopJson = stopTimeTodayJsonList.getJSONObject(i);
                String timeString = stopJson.getString("stop_time");
                String[] splitedTimeSTring = timeString.split(":");
                timeString = "今日の" + splitedTimeSTring[0] + "時" + splitedTimeSTring[1] + "分";
                int stopId = stopJson.getInt("id");

                Button newButton = new Button((Context) getActivity());
                String tag = "today:"+stopId;
                newButton.setTag(tag);
                newButton.setText(timeString);
                newButton.setTextColor(Color.BLUE);
                newButton.setTextSize(32);
                newButton.setOnClickListener(this);
                buttonLinearLayout.addView(newButton);

            } catch(JSONException e) {
                Log.d(TAG, "JSONException");
            }
        }
        // 明日の分
        for(int i =0; i < stopTimeTomorrowJsonList.length(); i++) {
            try {

                JSONObject stopJson = stopTimeTomorrowJsonList.getJSONObject(i);
                String timeString = stopJson.getString("stop_time");
                String[] splitedTimeSTring = timeString.split(":");
                timeString = "明日の" + splitedTimeSTring[0] + "時" + splitedTimeSTring[1] + "分";
                int stopId = stopJson.getInt("id");

                Button newButton = new Button((Context) getActivity());
                String tag = "tomorrow:"+stopId;
                newButton.setTag(tag);
                newButton.setText(timeString);
                newButton.setTextColor(Color.RED);
                newButton.setTextSize(32);
                newButton.setOnClickListener(this);
                buttonLinearLayout.addView(newButton);

            } catch(JSONException e) {
                Log.d(TAG, "JSONException");
            }
        }
        return  view;
    }

    @Override
    public void onClick(View view) {
        Button clickedButton = (Button) view;
        String tag = (String) clickedButton.getTag();
        String[] splitedTag = tag.split(":");
        boolean tomorrowReservation = false;
        if (splitedTag[0].equals("tomorrow")) {
            tomorrowReservation = true;
        }
        int stopId = Integer.valueOf(splitedTag[1]);
        mListener.onTimeButtonClicked(tomorrowReservation, stopId);
        buttonLinearLayout.removeAllViews();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnAskingTimeFragmentInteractionListener) {
            mListener = (OnAskingTimeFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onAttach(Activity context) {
        super.onAttach(context);
        if (context instanceof OnAskingTimeFragmentInteractionListener) {
            mListener = (OnAskingTimeFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public interface OnAskingTimeFragmentInteractionListener {
        void onTimeButtonClicked(boolean isTomorrow, int id);
    }
}
