package com.kuycoding.wijayafarm.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.kuycoding.wijayafarm.databinding.ItemUserBinding;
import com.kuycoding.wijayafarm.model.User;

public class UserAdapter extends FirestoreRecyclerAdapter<User, UserAdapter.viewHolder> {
    private setOnClickListener listener;
    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
     * FirestoreRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public UserAdapter(@NonNull FirestoreRecyclerOptions<User> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull UserAdapter.viewHolder holder, int position, @NonNull User model) {
        holder.binding.txtNameUser.setText(model.getNama());
        holder.binding.txtEmail.setText(model.getEmail());
        holder.binding.txtRole.setText(model.getRole());
    }

    @NonNull
    @Override
    public UserAdapter.viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemUserBinding itemUserBinding = ItemUserBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new viewHolder(itemUserBinding);
    }

    public class viewHolder extends RecyclerView.ViewHolder {
        private final ItemUserBinding binding;
        public viewHolder(@NonNull ItemUserBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void deleteItem() {
            listener.handleDeleteItem(getSnapshots().getSnapshot(getAbsoluteAdapterPosition()));
        }
    }
    public interface setOnClickListener {
        void handleDeleteItem(DocumentSnapshot snapshot);
    }
    public void setOnClickListener(setOnClickListener listener) {
        this.listener = listener;
    }
}