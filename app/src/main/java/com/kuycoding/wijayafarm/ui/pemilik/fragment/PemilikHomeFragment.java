package com.kuycoding.wijayafarm.ui.pemilik.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.kuycoding.wijayafarm.databinding.DialogLogoutBinding;
import com.kuycoding.wijayafarm.databinding.FragmentPemilikHomeBinding;
import com.kuycoding.wijayafarm.model.Ayam;
import com.kuycoding.wijayafarm.model.Panen;
import com.kuycoding.wijayafarm.model.Pengeluaran;
import com.kuycoding.wijayafarm.model.User;
import com.kuycoding.wijayafarm.ui.start.LoginActivity;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class PemilikHomeFragment extends Fragment {
    private FragmentPemilikHomeBinding binding;
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    View view;
    private NumberFormat format;
    private String date;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPemilikHomeBinding.inflate(inflater, container, false);
        view = binding.getRoot();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        format = new DecimalFormat("#,###,###");
        date = new SimpleDateFormat("MMM, yyyy", Locale.getDefault()).format(new Date());

        fStore.collection("user").document(Objects.requireNonNull(fAuth.getUid())).get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                DocumentSnapshot snapshot = task.getResult();
                                if (snapshot.exists()) {
                                    User user = snapshot.toObject(User.class);
                                    if (user != null) {
                                        binding.txtNameUser.setText(user.getNama());
                                    }
                                }
                            }
                        });

        binding.btnLogout.setOnClickListener(view1 -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            DialogLogoutBinding logoutBinding = DialogLogoutBinding.inflate(getLayoutInflater());
            builder.setView(logoutBinding.getRoot());

            AlertDialog dialog = builder.create();
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            logoutBinding.bntTidak.setOnClickListener(view2 -> dialog.dismiss());
            logoutBinding.btnYa.setOnClickListener(view2 -> {
                fAuth.signOut();
                startActivity(new Intent(requireActivity(), LoginActivity.class));
                requireActivity().finish();
            });
            dialog.show();
        });
        dataAyam();
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
                    binding.txtAyamHidup.setText(ayam.getHidup());
                    binding.txtAyamMati.setText(ayam.getMati());
                }
            }
        });
        fStore.collection("pengeluaran").orderBy("periode", Query.Direction.ASCENDING).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                int harga = 0;
                String hargaTtl = "0";
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
        fStore.collection("panen").orderBy("periode", Query.Direction.ASCENDING).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                int harga = 0;
                String hargaTtl = "0";
                for (QueryDocumentSnapshot snapshot : task.getResult()) {
                    Panen panen = snapshot.toObject(Panen.class);
                    String total = panen.getHarga_total();
                    int totalHarga = Integer.parseInt(total);
                    harga += totalHarga;
                    double hargaTotal = harga;
                    hargaTtl = format.format(hargaTotal);
                }
                binding.txtTotalPanen.setText(hargaTtl);
            }
        }).addOnFailureListener(e -> Log.e("TAG", "failure panen: ", e));
    }
}