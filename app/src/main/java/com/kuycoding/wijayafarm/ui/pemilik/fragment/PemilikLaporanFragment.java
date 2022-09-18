package com.kuycoding.wijayafarm.ui.pemilik.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.kuycoding.wijayafarm.R;
import com.kuycoding.wijayafarm.databinding.FragmentPemilikLaporanBinding;
import com.kuycoding.wijayafarm.model.Ayam;
import com.kuycoding.wijayafarm.ui.pemilik.activity.PemilikLaporanAyamActivity;
import com.kuycoding.wijayafarm.ui.pemilik.activity.PemilikLaporanPanenActivity;
import com.kuycoding.wijayafarm.ui.pemilik.activity.PemilikLaporanPengeluaranActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PemilikLaporanFragment extends Fragment {
    FragmentPemilikLaporanBinding binding;
    private FirebaseFirestore fStore;
    private String date;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPemilikLaporanBinding.inflate(getLayoutInflater(), container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fStore = FirebaseFirestore.getInstance();
        date = new SimpleDateFormat("MMM, yyyy", Locale.getDefault()).format(new Date());

        dataAyam();
        binding.cardAyam.setOnClickListener(view1 -> startActivity(new Intent(getContext(), PemilikLaporanAyamActivity.class)));
        binding.cardPengeluaran.setOnClickListener(view1 -> startActivity(new Intent(getContext(), PemilikLaporanPengeluaranActivity.class)));
        binding.cardPanen.setOnClickListener(view1 -> startActivity(new Intent(getContext(), PemilikLaporanPanenActivity.class)));
    }

    @SuppressLint("SetTextI18n")
    private void dataAyam() {
        Query query = fStore.collection("ayam").orderBy("periode", Query.Direction.ASCENDING);
        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                    Ayam ayam = snapshot.toObject(Ayam.class);
                    Log.d("TAG", "dataAyam: " + ayam.getPeriode());
                    binding.txtPeriode.setText(" "+ ayam.getPeriode());
                }
            }
        });
    }
}