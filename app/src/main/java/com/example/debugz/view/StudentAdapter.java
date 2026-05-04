package com.example.debugz.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.debugz.R;
import com.example.debugz.models.Account;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for the Find Friends screen.
 * Works with Account objects from the "accounts" Firestore collection.
 */
public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {

    public interface OnFriendActionClickListener {
        void onActionClick(Account account, String action);
    }

    private final List<Account> accountList;
    private final OnFriendActionClickListener listener;
    private List<String> currentFriendIds = new ArrayList<>();
    private String currentUserId;

    public StudentAdapter(List<Account> accountList, String currentUserId, OnFriendActionClickListener listener) {
        this.accountList = accountList;
        this.currentUserId = currentUserId;
        this.listener    = listener;
    }

    public void setCurrentFriendIds(List<String> friendIds) {
        this.currentFriendIds = friendIds != null ? friendIds : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_student, parent, false);
        return new StudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        Account account = accountList.get(position);
        holder.tvName.setText(account.getName());

        String email = (account.getEmail() != null && !account.getEmail().isEmpty())
                ? account.getEmail() : account.getAccountId();
        holder.tvEmail.setText(email);

        boolean alreadyFriend = currentFriendIds.contains(account.getAccountId());
        boolean hasSentRequest = account.getFriendRequests() != null && account.getFriendRequests().contains(currentUserId);

        if (alreadyFriend) {
            holder.btnAdd.setText("Friends");
            holder.btnAdd.setEnabled(false);
            holder.btnAdd.setAlpha(0.6f);
        } else if (hasSentRequest) {
            holder.btnAdd.setText("Sent");
            holder.btnAdd.setEnabled(false);
            holder.btnAdd.setAlpha(0.6f);
        } else {
            holder.btnAdd.setText("Add");
            holder.btnAdd.setEnabled(true);
            holder.btnAdd.setAlpha(1.0f);
            holder.btnAdd.setOnClickListener(v -> {
                listener.onActionClick(account, "SEND_REQUEST");
                holder.btnAdd.setText("Sent");
                holder.btnAdd.setEnabled(false);
                holder.btnAdd.setAlpha(0.6f);
            });
        }
    }

    @Override
    public int getItemCount() {
        return accountList.size();
    }

    static class StudentViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail;
        Button   btnAdd;

        StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName  = itemView.findViewById(R.id.tvStudentName);
            tvEmail = itemView.findViewById(R.id.tvStudentEmail);
            btnAdd  = itemView.findViewById(R.id.btnAddFriend);
        }
    }
}