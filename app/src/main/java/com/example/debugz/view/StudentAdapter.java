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

    public interface OnAddFriendClickListener {
        void onAddClick(Account account);
    }

    private final List<Account> accountList;
    private final OnAddFriendClickListener listener;
    private List<String> currentFriendIds = new ArrayList<>();

    public StudentAdapter(List<Account> accountList, OnAddFriendClickListener listener) {
        this.accountList = accountList;
        this.listener    = listener;
    }

    /**
     * Call this after loading the current user's friend list so the adapter
     * can show "Added" on accounts that are already friends.
     */
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
        if (alreadyFriend) {
            holder.btnAdd.setText("Added");
            holder.btnAdd.setEnabled(false);
            holder.btnAdd.setAlpha(0.5f);
        } else {
            holder.btnAdd.setText("Add");
            holder.btnAdd.setEnabled(true);
            holder.btnAdd.setAlpha(1.0f);
            holder.btnAdd.setOnClickListener(v -> {
                listener.onAddClick(account);
                // Immediately reflect the change in UI without waiting for Firestore
                currentFriendIds.add(account.getAccountId());
                notifyItemChanged(position);
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