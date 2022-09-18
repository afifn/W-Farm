package com.kuycoding.wijayafarm.ui.pemilik.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.kuycoding.wijayafarm.R;
import com.kuycoding.wijayafarm.adapter.PengeluaranAdapter;
import com.kuycoding.wijayafarm.databinding.ActivityPemilikLaporanPengeluaranBinding;
import com.kuycoding.wijayafarm.model.Pengeluaran;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashSet;
import java.util.List;

public class PemilikLaporanPengeluaranActivity extends AppCompatActivity {
    private ActivityPemilikLaporanPengeluaranBinding binding;
    private FirebaseFirestore fStore;
    private Query query;
    private String select;
    private final HashSet<String> hashSet = new HashSet<>();
    private PengeluaranAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPemilikLaporanPengeluaranBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        fStore = FirebaseFirestore.getInstance();
        tampilAyam();
    }

    private void tampilAyam() {
        query = fStore.collection("pengeluaran").orderBy("periode", Query.Direction.DESCENDING);
        List<String> listPeriode = new ArrayList<>();
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listPeriode);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinPeriode.setAdapter(arrayAdapter);
        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                    String s = snapshot.getString("periode");
                    listPeriode.add(s);
                }
                hashSet.addAll(listPeriode);
                listPeriode.clear();
                listPeriode.addAll(hashSet);
                arrayAdapter.notifyDataSetChanged();
            }
        });
        binding.spinPeriode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                select = arrayAdapter.getItem(i);
                query = fStore.collection("pengeluaran").whereEqualTo("periode", select);
                attachData(query);
                adapter.startListening();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                query = fStore.collection("pengeluaran");
                attachData(query);
            }
        });
        attachData(query);
    }

    private void attachData(Query query) {
        NumberFormat format = NumberFormat.getCurrencyInstance();
        format.setCurrency(Currency.getInstance("IDR"));
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                int harga = 0;
                String hargaTtl = "";
                for (QueryDocumentSnapshot snapshot : task.getResult()) {
                    Pengeluaran pengeluaran = snapshot.toObject(Pengeluaran.class);
                    int total = Integer.parseInt(pengeluaran.getBiaya());
                    harga += total;
                    double hargaTotal = harga;
                    hargaTtl = format.format(hargaTotal);
                }
                binding.txtTotalPengeluaran.setText(hargaTtl);
            }
        }).addOnFailureListener(e -> Log.e("TAG", "dataAyam: ", e));
        binding.recyclerView.setHasFixedSize(false);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        binding.recyclerView.setItemAnimator(null);

        FirestoreRecyclerOptions<Pengeluaran> options = new FirestoreRecyclerOptions.Builder<Pengeluaran>()
                .setQuery(query, Pengeluaran.class).build();

        adapter = new PengeluaranAdapter(options);
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                binding.recyclerView.scrollToPosition(adapter.getItemCount());
            }
        });
        binding.recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.startListening();
    }
}