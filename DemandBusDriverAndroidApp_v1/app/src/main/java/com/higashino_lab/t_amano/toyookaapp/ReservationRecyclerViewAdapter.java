package com.higashino_lab.t_amano.toyookaapp;

import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.higashino_lab.t_amano.toyookaapp.ItemFragment.OnListFragmentInteractionListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class ReservationRecyclerViewAdapter extends RecyclerView.Adapter<ReservationRecyclerViewAdapter.ViewHolder> {

    private final JSONArray reservationJsonArray;
    private final OnListFragmentInteractionListener mListener;
    private MainActivity mainActivity;

    private static final String TAG = "ReservationRecyclerViewAdapter";

    public ReservationRecyclerViewAdapter(JSONArray reservationJsonArray, OnListFragmentInteractionListener listener, MainActivity mainActivity) {
        this.reservationJsonArray = reservationJsonArray;
        this.mListener = listener;
        this.mainActivity = mainActivity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        holder.pickupButton.setEnabled(true);
        holder.absenceButton.setEnabled(true);
        holder.offBusButton.setEnabled(true);

        try {
            holder.reservationJson = reservationJsonArray.getJSONObject(position);
            String state = holder.reservationJson.getString("state");

            if (state.equals("on_bus")) {

                holder.offBusButton.setVisibility(View.VISIBLE);
                holder.pickupButton.setVisibility(View.INVISIBLE);
                holder.absenceButton.setVisibility(View.INVISIBLE);
                holder.stopNameView.setVisibility(View.INVISIBLE);
                holder.cardViewContainer.setBackgroundColor(Color.GREEN);
                String num_of_people = "("+ holder.reservationJson.getInt("num_of_people") +"人)";

                String username = holder.reservationJson.getJSONObject("user").getString("first_name");
                holder.userNameView.setText(username+"さん");
                holder.stopTimeView.setText("乗車中");
                holder.numOfPeopleView.setText(num_of_people);

            } else if (state.equals("reserved")) {

                holder.offBusButton.setVisibility(View.GONE);
                holder.pickupButton.setVisibility(View.VISIBLE);
                holder.absenceButton.setVisibility(View.VISIBLE);
                holder.stopNameView.setVisibility(View.VISIBLE);
                holder.cardViewContainer.setBackgroundColor(Color.WHITE);

                String username = holder.reservationJson.getJSONObject("user").getString("first_name");
                String stopname = holder.reservationJson.getJSONObject("stop_location").getString("name");
                String stoptime = holder.reservationJson.getJSONObject("stop_time").getString("display_time");
                String num_of_people = "("+ holder.reservationJson.getInt("num_of_people") +"人)";

                holder.stopTimeView.setText(stoptime);
                holder.stopNameView.setText(stopname);
                holder.userNameView.setText(username);
                holder.numOfPeopleView.setText(num_of_people);

                String busType = holder.reservationJson.getJSONObject("stop_time").getJSONObject("operating_time_window").getString("bus_type");

                if (busType.equals("going")) {
                    holder.stopNameView.setTextColor(Color.BLUE);
                } else {
                    holder.stopNameView.setTextColor(Color.RED);
                }
            }
        } catch (JSONException e) {
            Log.d("RECYCLE", "JOSN EXFCETJKTLJ:LK J:LKFJDSG:L ");

        }

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.reservationJson);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return reservationJsonArray.length();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public final View mView;
        public CardView cardView;
        public final TextView stopTimeView;
        public final TextView stopNameView;
        public final TextView userNameView;
        public final TextView numOfPeopleView;
        public RelativeLayout cardViewContainer;

        public final Button pickupButton;
        public final Button absenceButton;
        public final Button offBusButton;

        public JSONObject reservationJson;

        public ViewHolder(View view) {
            super(view);

            mView = view;
            cardView = (CardView) view.findViewById(R.id.card_view);
            cardViewContainer = (RelativeLayout) view.findViewById(R.id.card_view_container);

            stopTimeView = (TextView) view.findViewById(R.id.stop_datetime);
            stopNameView = (TextView) view.findViewById(R.id.stop_name);
            userNameView = (TextView) view.findViewById(R.id.user_name);
            numOfPeopleView = (TextView) view.findViewById(R.id.num_of_people);

            pickupButton = (Button) view.findViewById(R.id.pickup_button);
            absenceButton = (Button) view.findViewById(R.id.absent_button);
            offBusButton = (Button) view.findViewById(R.id.off_bus_button);

            pickupButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pickupButton.setEnabled(false);
                    absenceButton.setEnabled(false);
                    mainActivity.onPickupButtonClicked(reservationJson);
                    notifyDataSetChanged();
                }
            });

            absenceButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pickupButton.setEnabled(false);
                    absenceButton.setEnabled(false);
                    mainActivity.onAbsenceButtonClicked(reservationJson);
                    notifyDataSetChanged();
                }
            });
            offBusButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    offBusButton.setEnabled(false);
                    mainActivity.onOffBusButtonClicked(reservationJson);
                    notifyDataSetChanged();
                }
            });
        }

        @Override
        public String toString() {
            return super.toString() + " '" + stopNameView.getText() + "'";
        }
    }
}
