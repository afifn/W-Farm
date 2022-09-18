package com.kuycoding.wijayafarm.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.kuycoding.wijayafarm.databinding.ItemPengeluaranBinding;
import com.kuycoding.wijayafarm.model.Pengeluaran;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class PengeluaranAdapter extends FirestoreRecyclerAdapter<Pengeluaran, PengeluaranAdapter.viewHolder> {
    private PengeluaranAdapter.onItemClick itemClick;
    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
     * FirestoreRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public PengeluaranAdapter(@NonNull FirestoreRecyclerOptions<Pengeluaran> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull PengeluaranAdapter.viewHolder holder, int position, @NonNull Pengeluaran model) {
        NumberFormat format = new DecimalFormat("#,###,###");
        double harga = Double.parseDouble(model.getBiaya());
        String biaya = format.format(harga);

        holder.binding.txtNama.setText(model.getKeperluan());
        holder.binding.txtPetugas.setText(model.getNama());
        holder.binding.txtHarga.setText(biaya);
        holder.binding.txtTanggal.setText(model.getTanggal());
        holder.binding.txtSatuan.setText(model.getJumlah());
    }

    @NonNull
    @Override
    public PengeluaranAdapter.viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPengeluaranBinding itemPengeluaranBinding = ItemPengeluaranBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new viewHolder(itemPengeluaranBinding);
    }

    public class viewHolder extends RecyclerView.ViewHolder {
        private final ItemPengeluaranBinding binding;
        public viewHolder(@NonNull ItemPengeluaranBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void deleteItem() {
            itemClick.handleItemDelete(getSnapshots().getSnapshot(getAbsoluteAdapterPosition()));
        }
    }
    public interface onItemClick {
        void handleItemDelete(DocumentSnapshot snapshot);
    }

    public void setOnClickListener(PengeluaranAdapter.onItemClick listener) {
        this.itemClick = listener;
    }
}
