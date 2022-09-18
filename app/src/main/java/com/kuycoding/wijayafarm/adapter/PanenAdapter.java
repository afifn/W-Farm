package com.kuycoding.wijayafarm.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.kuycoding.wijayafarm.databinding.ItemPanenBinding;
import com.kuycoding.wijayafarm.model.Panen;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Currency;

public class PanenAdapter  extends FirestoreRecyclerAdapter<Panen, PanenAdapter.viewHolder> {
    private PanenAdapter.setOnClickListener listener;
    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
     * FirestoreRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public PanenAdapter(@NonNull FirestoreRecyclerOptions<Panen> options) {
        super(options);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onBindViewHolder(@NonNull PanenAdapter.viewHolder holder, int position, @NonNull Panen model) {
        NumberFormat format = new DecimalFormat("#,###,###");
        double harga = Double.parseDouble(model.getHarga_kg());
        double hargaTotal = Double.parseDouble(model.getHarga_total());
        String hargaSplit = format.format(harga);
        String hrgTotal = format.format(hargaTotal);

        holder.binding.txtNama.setText(model.getNama_pembeli());
        holder.binding.txtJumlahEkor.setText(model.getJumlah_ayam()+ " Ekor");
        holder.binding.txtTanggal.setText(model.getTanggal());
        holder.binding.txtBeban.setText(model.getTotal_berat() + " Kg");
        holder.binding.txtHargaKilo.setText("Rp " +hargaSplit+"/Kg");
        holder.binding.txtHarga.setText(hrgTotal);
    }

    @NonNull
    @Override
    public PanenAdapter.viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPanenBinding itemPanenBinding = ItemPanenBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new viewHolder(itemPanenBinding);
    }

    public class viewHolder extends RecyclerView.ViewHolder {
        private final ItemPanenBinding binding;
        public viewHolder(@NonNull ItemPanenBinding binding) {
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

    public void setOnClickListener(PanenAdapter.setOnClickListener listener) {
        this.listener = listener;
    }
}
