package com.higashino_lab.t_amano.toyookauserapp;

import android.content.Context;
import android.graphics.Color;
import android.speech.tts.TextToSpeech;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.higashino_lab.t_amano.toyookauserapp.MessageFragment.OnListFragmentInteractionListener;

import java.util.List;

public class MySuggestionRecyclerViewAdapter extends RecyclerView.Adapter<MySuggestionRecyclerViewAdapter.ViewHolder> {

    private final List<MessageModel> messageModelList;
    private final OnListFragmentInteractionListener mListener;
    public MainActivity mainActivity;


    public MySuggestionRecyclerViewAdapter(List<MessageModel> items, OnListFragmentInteractionListener listener, Context context) {
        this.mListener = listener;
        this.messageModelList = items;
        this.mainActivity = (MainActivity) context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_suggestion, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.messageModel = this.messageModelList.get(position);
        // holder.mIdView.setText(mValues.get(position).id);
        // holder.mContentView.setText(mValues.get(position).content);

        if (holder.messageModel.message_type.equals("suggestion")) {
            holder.bookingButton.setEnabled(true);
            holder.mIdView.setText(holder.messageModel.text);
            holder.mContentView.setText(holder.messageModel.getSuggestedTime());
            holder.bookingButton.setText("予約する");
            holder.suggestionLayout.setVisibility(View.VISIBLE);
            holder.mContentView.setVisibility(View.VISIBLE);
            holder.mIdView.setVisibility(View.VISIBLE);
            holder.mIdView.setTextColor(Color.BLACK);

            if (holder.messageModel.bookingId != null) {
                String time = holder.messageModel.getSuggestedTime();
                holder.mIdView.setText("バスを予約しました。" + time + "に向かいます。");
                holder.mContentView.setVisibility(View.GONE);
                holder.mIdView.setTextColor(Color.RED);
                holder.bookingButton.setText("予約をキャンセルする");
            }

        } else if (holder.messageModel.message_type.equals("normal")) {
            holder.mIdView.setText(holder.messageModel.text);
            holder.mContentView.setVisibility(View.GONE);
            holder.bookingButton.setVisibility(View.GONE);
            holder.suggestionLayout.setVisibility(View.GONE);
        } else {
            holder.mIdView.setVisibility(View.GONE);
            holder.mContentView.setVisibility(View.GONE);
            holder.bookingButton.setVisibility(View.GONE);
            holder.suggestionLayout.setVisibility(View.GONE);
        }

        holder.bookingButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Button button = (Button) v;
                button.setEnabled(false);

                if (holder.messageModel.bookingId != null) {
                    button.setText("キャンセル中...");

                } else {

                    button.setText("予約中...");

                }

                mainActivity.onBookingButtonClicked(holder.messageModel);
            }
        } );

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    mListener.onListFragmentInteraction(holder.messageModel);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.messageModelList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;

        public final TextView mIdView;
        public final TextView mContentView;

        public MessageModel messageModel;
        public Button bookingButton;
        public RelativeLayout suggestionLayout;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = (TextView) view.findViewById(R.id.message_text);
            mContentView = (TextView) view.findViewById(R.id.time);
            bookingButton = (Button) view.findViewById(R.id.booking_button);
            suggestionLayout = (RelativeLayout) view.findViewById(R.id.suggestion);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
