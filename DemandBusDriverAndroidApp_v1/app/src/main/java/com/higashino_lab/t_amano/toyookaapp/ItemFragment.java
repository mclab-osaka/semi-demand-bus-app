package com.higashino_lab.t_amano.toyookaapp;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ItemFragment extends Fragment {

    private static final String TAG = "ItemFragment";

    // private ArrayList<ReservationModel> reservationModelList;
    private JSONArray reservationJsonArray;
    private OnListFragmentInteractionListener mListener;
    private ReservationRecyclerViewAdapter reservationRecyclerViewAdapter;
    private RecyclerView recyclerView;

    public ItemFragment() { }

    public static ItemFragment newInstance() {
        ItemFragment fragment = new ItemFragment();
        return fragment;
    }

    public void setNewReservationJsonArray(JSONArray newReservationJsonArray) {

        if (this.recyclerView == null || newReservationJsonArray == null) {
            Log.d(TAG, "recycleView is null or reservationmodelslist isn ull");
            return;
        }
        int len = this.reservationJsonArray.length();
        Log.d(TAG,"OLD:"+ this.reservationJsonArray.toString());
        while (this.reservationJsonArray.length() > 0) {
            Log.d(TAG, "" + this.reservationJsonArray.length());
            this.reservationJsonArray.remove(0);
        }

        Log.d(TAG, "NEW:"+this.reservationJsonArray.toString());
        for (int i=0; i<newReservationJsonArray.length(); i++) {
            try {
            Log.d(TAG,"ADD" + i);
                JSONObject res = newReservationJsonArray.getJSONObject(i);
                this.reservationJsonArray.put(res);
            } catch (JSONException e) {
                Log.d(TAG, "JOSNExceptin");
            }

        }
        this.reservationRecyclerViewAdapter.notifyDataSetChanged();
        Log.d(TAG, "setNewReservationJsonArray");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");

        View view = inflater.inflate(R.layout.fragment_item_list, container, false);
        this.recyclerView = (RecyclerView) view.findViewById(R.id.list);
        this.reservationJsonArray = new JSONArray();

        // Set the adapter
        if (this.recyclerView != null) {
            Context context = view.getContext();
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            this.reservationRecyclerViewAdapter = new ReservationRecyclerViewAdapter(this.reservationJsonArray, mListener, (MainActivity) context);
            recyclerView.setAdapter(reservationRecyclerViewAdapter);
        }
        mListener.onListFragmentCreateViewReady();
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach");

        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(JSONObject reservationJson);
        void onListFragmentCreateViewReady();
    }
}
