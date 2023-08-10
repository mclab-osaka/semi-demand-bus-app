package com.higashino_lab.t_amano.toyookauserapp;

import android.app.Activity;
import android.content.Context;
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

import java.util.ArrayList;

public class AskingStopFragment extends Fragment implements Button.OnClickListener {

    private OnAskingStopFragmentInteractionListener mListener;
    private static final String TAG = "AskingStopFragment";
    private static final String ARG_STOPS  = "stopList";
    private static final String ARG_MESSAGE = "askingMessage";

    private JSONArray stopJsonList;
    private LinearLayout buttonLinearLayout;
    private String askingMessage;

    public AskingStopFragment() {
        // Required empty public constructor
    }

    public static AskingStopFragment newInstance(JSONArray stopJsonList, String askingMessage) {
        AskingStopFragment fragment = new AskingStopFragment();
        Bundle args = new Bundle();
        if (stopJsonList != null) {
            args.putString(ARG_STOPS, stopJsonList.toString());
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
            String stopJsonListString = getArguments().getString(ARG_STOPS);
            try {
                this.stopJsonList = new JSONArray(stopJsonListString);

            } catch (JSONException e) {
                Log.d(TAG, "JsonException");
                this.stopJsonList = new JSONArray();
            }
        } else {
            this.stopJsonList = new JSONArray();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_asking_stop, container, false);
        TextView message = (TextView) view.findViewById(R.id.asking_stop_message);
        message.setText(this.askingMessage);

        buttonLinearLayout = (LinearLayout) view.findViewById(R.id.asking_stop_buttons);
        buttonLinearLayout.removeAllViews();

        for(int i =0; i < stopJsonList.length(); i++) {
            try {
                JSONObject stopJson = stopJsonList.getJSONObject(i);
                Button newButton = new Button((Context) getActivity());
                newButton.setTag(stopJson.getInt(AskingActivity.JSON_KEY_ID));
                newButton.setText(stopJson.getString(AskingActivity.JSON_KEY_NAME));
                newButton.setTextSize(32);
                newButton.setOnClickListener(this);
                buttonLinearLayout.addView(newButton);
            } catch(JSONException e) {
                Log.d(TAG, "JSONException");
            }
        }
        return view;
    }

    @Override
    public void onClick(View view) {
        Button clickedButton = (Button) view;
        mListener.onStopLocationButtonClicked((int)clickedButton.getTag(), (String) clickedButton.getText());
        buttonLinearLayout.removeAllViews();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach");
        if (context instanceof OnAskingStopFragmentInteractionListener) {
            mListener = (OnAskingStopFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d(TAG, "onAttach");
        if (activity instanceof OnAskingStopFragmentInteractionListener) {
            mListener = (OnAskingStopFragmentInteractionListener) activity;
        } else {
            throw new RuntimeException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnAskingStopFragmentInteractionListener {
        void onStopLocationButtonClicked(int id, String name);
    }


}
