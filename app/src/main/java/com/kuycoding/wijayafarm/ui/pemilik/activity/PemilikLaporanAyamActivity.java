package com.kuycoding.wijayafarm.ui.pemilik.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.kuycoding.wijayafarm.databinding.ActivityPemilikLaporanAyamBinding;
import com.kuycoding.wijayafarm.model.Ayam;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

public class PemilikLaporanAyamActivity extends AppCompatActivity {
    private ActivityPemilikLaporanAyamBinding binding;
    private FirebaseFirestore fStore;

    private Query query;
    private String select;
    private final HashSet<String> hashSet = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPemilikLaporanAyamBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        fStore = FirebaseFirestore.getInstance();

        dataAyam();
    }

    private void dataAyam() {
        query = fStore.collection("ayam").orderBy("periode", Query.Direction.DESCENDING);

        List<String> listPeriode = new ArrayList<>();
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listPeriode);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerPeriode.setAdapter(arrayAdapter);
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
        binding.spinnerPeriode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                select = arrayAdapter.getItem(i);
                Log.d("TAG", "onItemSelected: " + select);
                query = fStore.collection("ayam").whereEqualTo("periode", select);
                attachData(query);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                query = fStore.collection("ayam");
                attachData(query);
            }
        });
    }

    private void attachData(Query query) {
        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                    Ayam ayam = snapshot.toObject(Ayam.class);
                    Log.d("TAG", "onSuccess: " + ayam.getPeriode());
                    binding.txtBibitAwal.setText(ayam.getJumlah());
                    binding.txtTersedia.setText(ayam.getTersedia());
                    binding.txtTerjual.setText(ayam.getTerjual());
                    binding.txtHidup.setText(ayam.getHidup());
                    binding.txtMati.setText(ayam.getMati());
                    binding.txtSehat.setText(ayam.getSehat());
                    binding.txtSakit.setText(ayam.getSakit());
                    binding.txtSembuh.setText(ayam.getSembuh());
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}