package com.kuycoding.wijayafarm.ui.petugas.fragment;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialog;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.kal.rackmonthpicker.MonthType;
import com.kal.rackmonthpicker.RackMonthPicker;
import com.kuycoding.wijayafarm.R;
import com.kuycoding.wijayafarm.databinding.DialogFormAddBibitBinding;
import com.kuycoding.wijayafarm.databinding.DialogFormHidupmatiBinding;
import com.kuycoding.wijayafarm.databinding.DialogFormSesuaikanKondisiBinding;
import com.kuycoding.wijayafarm.databinding.FragmentPetugasAyamBinding;
import com.kuycoding.wijayafarm.model.Ayam;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class PetugasAyamFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private final static String TAG = "debug";
    private FragmentPetugasAyamBinding binding;
    private FirebaseFirestore fStore;
    private View view;
    private RackMonthPicker rackMonthPicker;

    private Query query;
    private DialogFormAddBibitBinding addBibitBinding;
    private DialogFormHidupmatiBinding hidupmatiBinding;
    private DialogFormSesuaikanKondisiBinding sesuaikanKondisiBinding;

    private String select;
    private String date, dateNow;
    private final HashSet<String> hashSet = new HashSet<>();
    private TextWatcher textWatcher;
    private TextWatcher textWatcherHidupMati;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPetugasAyamBinding.inflate(inflater, container, false);
        view = binding.getRoot();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        date = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date());
        dateNow = new SimpleDateFormat("MMM, yyyy", Locale.getDefault()).format(new Date());
        fStore = FirebaseFirestore.getInstance();
        binding.btnTambahBibit.setOnClickListener(view1 -> addBibit());

        tampilDataPeriode();

        rackMonthPicker = new RackMonthPicker(requireActivity())
                .setMonthType(MonthType.TEXT)
                .setLocale(new Locale("ID"))
                .setColorTheme(R.color.red)
                .setPositiveButton((month, startDate, endDate, year, monthLabel) -> {
                    Log.d("TAG", "onDateMonth: " + monthLabel);
                    Log.d("TAG", "onDateMonth: " + month);
                    Log.d("TAG", "onDateMonth: " + year);
                    addBibitBinding.edtPerdoie.setText(monthLabel);
                })
                .setNegativeButton(AppCompatDialog::dismiss);
    }

    private void tampilDataPeriode() {
        query = fStore.collection("ayam").orderBy("periode", Query.Direction.DESCENDING);
        binding.swipe.setOnRefreshListener(this);

        List<String> listPeriode = new ArrayList<>();
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, listPeriode);
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

                    String uid = ayam.getUid();
                    binding.btnSesuaikanHidupmati.setOnClickListener(view1 -> sesuaikanHidupMati(uid));
                    binding.btnSesuaikanKondisi.setOnClickListener(view1 -> sesuaikanKondisi(uid));
                }
            }
        });
    }

    private void sesuaikanKondisi(String uid) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        sesuaikanKondisiBinding = DialogFormSesuaikanKondisiBinding.inflate(getLayoutInflater());
        View viewKondisi = sesuaikanKondisiBinding.getRoot();
        builder.setView(viewKondisi);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        sesuaikanKondisiBinding.btnAddSembuh.setOnClickListener(view1 -> {
            int ayamSembuh = Integer.parseInt(Objects.requireNonNull(sesuaikanKondisiBinding.edtSembuh.getText()).toString());
            int ayamSakit = Integer.parseInt(Objects.requireNonNull(sesuaikanKondisiBinding.edtSakit.getText()).toString());
            displaySembuh(ayamSembuh + 1);
            displaySakit(ayamSakit - 1);
        });sesuaikanKondisiBinding.btnMinSembuh.setOnClickListener(view1 -> {
            int ayamSembuh = Integer.parseInt(Objects.requireNonNull(sesuaikanKondisiBinding.edtSembuh.getText()).toString());
            int ayamSakit = Integer.parseInt(Objects.requireNonNull(sesuaikanKondisiBinding.edtSakit.getText()).toString());
            displaySembuh(ayamSembuh - 1);
            displaySakit(ayamSakit + 1);
        });

        sesuaikanKondisiBinding.btnAddSakit.setOnClickListener(view1 -> {
            int ayamSehat = Integer.parseInt(Objects.requireNonNull(sesuaikanKondisiBinding.edtSehat.getText()).toString());
            int ayamSakit = Integer.parseInt(Objects.requireNonNull(sesuaikanKondisiBinding.edtSakit.getText()).toString());
            displaySakit(ayamSakit + 1);
            displaySehat(ayamSehat - 1);
        });sesuaikanKondisiBinding.btnMinSakit.setOnClickListener(view1 -> {
            int ayamSehat = Integer.parseInt(Objects.requireNonNull(sesuaikanKondisiBinding.edtSehat.getText()).toString());
            int ayamSakit = Integer.parseInt(Objects.requireNonNull(sesuaikanKondisiBinding.edtSakit.getText()).toString());
            displaySakit(ayamSakit - 1);
            displaySehat(ayamSehat + 1);
        });

        textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String edSakit = Objects.requireNonNull(sesuaikanKondisiBinding.edtSakit.getText()).toString();
                String edSembuh = Objects.requireNonNull(sesuaikanKondisiBinding.edtSembuh.getText()).toString();

                // jika sakit 0 dan sembuh 0 makan btn sehat+ disable
                if (Integer.parseInt(edSakit) > 0 ) {
                    sesuaikanKondisiBinding.btnMinSakit.setEnabled(true);
                    sesuaikanKondisiBinding.btnAddSembuh.setEnabled(true);
                } else {
                    sesuaikanKondisiBinding.btnAddSembuh.setEnabled(false);
                    sesuaikanKondisiBinding.btnMinSakit.setEnabled(false);
                }

                sesuaikanKondisiBinding.btnMinSembuh.setEnabled(Integer.parseInt(edSembuh) > 0);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };

        fStore.collection("ayam").document(uid).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot snapshot = task.getResult();
                if (snapshot.exists()) {
                    Ayam ayam = snapshot.toObject(Ayam.class);
                    if (ayam != null) {
                        String jumlah = ayam.getJumlah();
                        int intJumlah = Integer.parseInt(jumlah);
                        sesuaikanKondisiBinding.edtSehat.setText(ayam.getSehat());
                        sesuaikanKondisiBinding.edtSembuh.setText(ayam.getSembuh());
                        sesuaikanKondisiBinding.edtSakit.setText(ayam.getSakit());

                        int jmSembuh = Integer.parseInt(ayam.getSembuh());
                        int jmSakit = Integer.parseInt(ayam.getSakit());
                        sesuaikanKondisiBinding.btnMinSakit.setEnabled(jmSakit > 0);
                        sesuaikanKondisiBinding.btnMinSembuh.setEnabled(jmSembuh > 0);
                        sesuaikanKondisiBinding.btnAddSembuh.setEnabled(jmSakit > 0);

                        sesuaikanKondisiBinding.btnSimpan.setOnClickListener(view -> {
                            String jmlSehat = Objects.requireNonNull(sesuaikanKondisiBinding.edtSehat.getText()).toString();
                            String jmlSembuh = Objects.requireNonNull(sesuaikanKondisiBinding.edtSembuh.getText()).toString();
                            String jmlSakit = Objects.requireNonNull(sesuaikanKondisiBinding.edtSakit.getText()).toString();

                            if (jmlSehat.isEmpty()) {
                                Snackbar.make(viewKondisi, "Field tidak boleh kosong", Snackbar.LENGTH_SHORT).show();
                            } else if (jmlSembuh.isEmpty()) {
                                Snackbar.make(viewKondisi, "Field tidak boleh kosong", Snackbar.LENGTH_SHORT).show();
                            } else if (jmlSakit.isEmpty()) {
                                Snackbar.make(viewKondisi, "Field tidak boleh kosong", Snackbar.LENGTH_SHORT).show();
                            } else {
                                int intSehat = Integer.parseInt(jmlSehat);
                                int intSembuh = Integer.parseInt(jmlSembuh);
                                int intSakit = Integer.parseInt(jmlSakit);

                                if (intSehat > intJumlah) {
                                    sesuaikanKondisiBinding.edtSehat.setError("Tidak boleh lebih dari " + intJumlah);
                                } else if (intSembuh > (intJumlah - intSehat)){
                                    sesuaikanKondisiBinding.edtSembuh.setError("Input data salah!");
                                } else if (intSakit > (intJumlah - intSehat - intSembuh)) {
                                    sesuaikanKondisiBinding.edtSakit.setError("Input data salah!");
                                } else {
                                    DocumentReference reference = fStore.collection("ayam").document(uid);
                                    Map<String, Object> sesuaikan = new HashMap<>();
                                    sesuaikan.put("sehat", jmlSehat);
                                    sesuaikan.put("sakit", jmlSakit);
                                    sesuaikan.put("sembuh", jmlSembuh);

                                    reference.update(sesuaikan).addOnSuccessListener(unused -> {
                                        Snackbar.make(view, "Berhasil menyesuaikan data", Snackbar.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                        query = fStore.collection("ayam").whereEqualTo("periode", select);
                                        attachData(query);
                                    });
                                }
                            }
                        });
                    }
                }
            }
        });
        dialog.show();
    }

    private void displaySehat(int i) {
        sesuaikanKondisiBinding.edtSehat.addTextChangedListener(textWatcher);
        sesuaikanKondisiBinding.edtSehat.setText(String.valueOf(i));
    }
    private void displaySembuh(int i) {
        sesuaikanKondisiBinding.edtSembuh.addTextChangedListener(textWatcher);
        sesuaikanKondisiBinding.edtSembuh.setText(String.valueOf(i));
    }
    private void displaySakit(int i) {
        sesuaikanKondisiBinding.edtSakit.addTextChangedListener(textWatcher);
        sesuaikanKondisiBinding.edtSakit.setText(String.valueOf(i));
    }

    private void sesuaikanHidupMati(String uid) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        hidupmatiBinding = DialogFormHidupmatiBinding.inflate(getLayoutInflater());
        builder.setView(hidupmatiBinding.getRoot());

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        hidupmatiBinding.btnAddHidup.setOnClickListener(view1 -> {
            int ayamHidup = Integer.parseInt(Objects.requireNonNull(hidupmatiBinding.edtHiup.getText()).toString());
            int ayamMati = Integer.parseInt(Objects.requireNonNull(hidupmatiBinding.edtMati.getText()).toString());
            displayHidup(ayamHidup + 1);
            displayMati(ayamMati - 1);
        });
        hidupmatiBinding.btnMinHidup.setOnClickListener(view1 -> {
            int ayamHidup = Integer.parseInt(Objects.requireNonNull(hidupmatiBinding.edtHiup.getText()).toString());
            int ayamMati = Integer.parseInt(Objects.requireNonNull(hidupmatiBinding.edtMati.getText()).toString());
            displayHidup(ayamHidup - 1);
            displayMati(ayamMati + 1);
        });

        hidupmatiBinding.btnAddMati.setOnClickListener(view1 -> {
            int ayamMati = Integer.parseInt(Objects.requireNonNull(hidupmatiBinding.edtMati.getText()).toString());
            int ayamHidup = Integer.parseInt(Objects.requireNonNull(hidupmatiBinding.edtHiup.getText()).toString());
            displayMati(ayamMati + 1);
            displayHidup(ayamHidup - 1);
        });
        hidupmatiBinding.btnMinMati.setOnClickListener(view1 -> {
            int ayamMati = Integer.parseInt(Objects.requireNonNull(hidupmatiBinding.edtMati.getText()).toString());
            int ayamHidup = Integer.parseInt(Objects.requireNonNull(hidupmatiBinding.edtHiup.getText()).toString());
            displayMati(ayamMati - 1);
            displayHidup(ayamHidup + 1);
        });

        textWatcherHidupMati = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                int edMati = Integer.parseInt(Objects.requireNonNull(hidupmatiBinding.edtMati.getText()).toString());

                hidupmatiBinding.btnMinMati.setEnabled(edMati > 0);
                hidupmatiBinding.btnAddHidup.setEnabled(edMati > 0);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };
        fStore.collection("ayam").document(uid).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot documentSnapshot = task.getResult();
                if (documentSnapshot.exists()) {
                    Ayam ayam = documentSnapshot.toObject(Ayam.class);
                    if (ayam != null) {
                        String jumlah = ayam.getJumlah();
                        int intJumlah = Integer.parseInt(jumlah);
                        hidupmatiBinding.edtHiup.setText(ayam.getHidup());
                        hidupmatiBinding.edtMati.setText(ayam.getMati());

                        int jmMati = Integer.parseInt(ayam.getMati());
                        hidupmatiBinding.btnMinMati.setEnabled(jmMati > 0);

                        int inputHidup = Integer.parseInt(Objects.requireNonNull(hidupmatiBinding.edtHiup.getText()).toString());
                        hidupmatiBinding.btnAddHidup.setEnabled(inputHidup < intJumlah);

                        hidupmatiBinding.btnSimpan.setOnClickListener(view1 -> {
                            String jmlHidup = Objects.requireNonNull(hidupmatiBinding.edtHiup.getText()).toString();
                            String jmlMati = Objects.requireNonNull(hidupmatiBinding.edtMati.getText()).toString();

                            if (jmlHidup.isEmpty()) {
                                Snackbar.make(hidupmatiBinding.getRoot(), "Field tidak boleh kosong", Snackbar.LENGTH_SHORT)
                                        .show();
                            } else if (jmlMati.isEmpty()) {
                                Snackbar.make(hidupmatiBinding.getRoot(), "Field tidak boleh kosong", Snackbar.LENGTH_SHORT).show();
                            } else {
                                int intHidup = Integer.parseInt(jmlHidup);
                                int intMati = Integer.parseInt(jmlMati);

                                if (TextUtils.isEmpty(jmlHidup)) {
                                    hidupmatiBinding.edtHiup.setError("Tidak boleh kosong!");
                                } else if (intHidup > intJumlah) {
                                    hidupmatiBinding.edtHiup.setError("Tidak boleh lebih dari " + jumlah);
                                } else if (TextUtils.isEmpty(jmlMati)) {
                                    hidupmatiBinding.edtMati.setError("Tidak boleh kosong!");
                                } else if (intMati > intJumlah){
                                    hidupmatiBinding.edtMati.setError("Tidak boleh lebih dari " + jumlah);
                                } else if (intMati > (intJumlah - intHidup)) {
                                    hidupmatiBinding.edtMati.setError("Input data salah");
                                }
                                else {
                                    DocumentReference reference = fStore.collection("ayam").document(uid);
                                    Map<String, Object> add = new HashMap<>();
                                    add.put("hidup", jmlHidup);
                                    add.put("mati", jmlMati);
                                    reference.update(add).addOnSuccessListener(unused -> Log.d("TAG", "onSuccess: " + unused));
                                    Snackbar.make(view, "Berhasil menyesuaikan data", Snackbar.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                    query = fStore.collection("ayam").whereEqualTo("periode", select);
                                    attachData(query);
                                }
                            }


                        });
                    }
                }
            }
        });
        dialog.show();
    }

    private void displayMati(int i) {
        hidupmatiBinding.edtMati.setText(String.valueOf(i));
        hidupmatiBinding.edtMati.addTextChangedListener(textWatcherHidupMati);
    }

    private void displayHidup(int i) {
        hidupmatiBinding.edtHiup.setText(String.valueOf(i));
        hidupmatiBinding.edtHiup.addTextChangedListener(textWatcherHidupMati);
    }

    private void addBibit() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        addBibitBinding = DialogFormAddBibitBinding.inflate(getLayoutInflater());
        builder.setView(addBibitBinding.getRoot());

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        // month year builder
        addBibitBinding.edtPerdoie.setOnClickListener(view1 -> rackMonthPicker.show());
        addBibitBinding.btnTambah.setOnClickListener(view1 -> {
            String jumlah = Objects.requireNonNull(addBibitBinding.edtJumlahAyam.getText()).toString();
            String periode = Objects.requireNonNull(addBibitBinding.edtPerdoie.getText()).toString();

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, 35);
            Date nextDate = calendar.getTime();
            @SuppressLint("SimpleDateFormat") String nextPeride = new SimpleDateFormat("dd MMM yyyy").format(nextDate);

            if (TextUtils.isEmpty(jumlah)) {
                addBibitBinding.edtJumlahAyam.setError("Harus diisi");
            } else if (TextUtils.isEmpty(periode)) {
                addBibitBinding.edtPerdoie.setError("Harus diisi");
            } else {
                if (dateNow.equals(periode)) {
                    CollectionReference reference = fStore.collection("ayam");
                    reference.get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            QuerySnapshot snapshot = task.getResult();
                            if (snapshot.isEmpty()) {
                                dialog.dismiss();
                                Snackbar.make(view, "Tunggu sebentar", Snackbar.LENGTH_SHORT).show();

                                DocumentReference ref = fStore.collection("ayam").document();
                                String id = ref.getId();
                                Map<String, Object> ayam = new HashMap<>();
                                ayam.put("uid", id);
                                ayam.put("jumlah", jumlah);
                                ayam.put("periode", periode);
                                ayam.put("tersedia", jumlah);
                                ayam.put("terjual", "0");
                                ayam.put("hidup", jumlah);
                                ayam.put("mati", "0");
                                ayam.put("sakit", "0");
                                ayam.put("sembuh", "0");
                                ayam.put("sehat", jumlah);
                                ayam.put("create_at", date);
                                ayam.put("next_periode", nextPeride);

                                ref.set(ayam).addOnSuccessListener(a -> {
                                    Log.d("TAG", "addBibit: sukses");
                                    Snackbar.make(view, "Berhasil menambahkan data", Snackbar.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                    tampilDataPeriode();
                                });
                            } else {
                                for (QueryDocumentSnapshot documentSnapshot : snapshot) {
                                    Ayam ayamDB = documentSnapshot.toObject(Ayam.class);
                                    if (ayamDB.getPeriode().equals(periode)){
                                        Log.d(TAG, "addBibit: uid " + ayamDB.getUid());
                                        dialog.dismiss();
                                        Snackbar.make(view, "Tunggu sebentar", Snackbar.LENGTH_SHORT).show();
                                        DocumentReference ref = fStore.collection("ayam").document(ayamDB.getUid());
                                        int inputJumlah = Integer.parseInt(jumlah);
                                        int baseJumlah = Integer.parseInt(ayamDB.getJumlah());
                                        int baseTersedia = Integer.parseInt(ayamDB.getTersedia());
                                        int baseHidup = Integer.parseInt(ayamDB.getHidup());
                                        int baseSehat = Integer.parseInt(ayamDB.getSehat());

                                        int totalJumlah = baseJumlah + inputJumlah;
                                        int totalTersedia = baseTersedia + inputJumlah;
                                        int totalHidup = baseHidup + inputJumlah;
                                        int totalSehat = baseSehat + inputJumlah;

                                        String mJumlah = String.valueOf(totalJumlah);
                                        String mTersedia = String.valueOf(totalTersedia);
                                        String mHidup = String.valueOf(totalHidup);
                                        String mSehat = String.valueOf(totalSehat);

                                        Map<String, Object> u = new HashMap<>();
                                        u.put("periode", periode);
                                        u.put("jumlah", mJumlah);
                                        u.put("tersedia", mTersedia);
                                        u.put("hidup", mHidup);
                                        u.put("sehat", mSehat);
                                        u.put("update_at", date);

                                        ref.update(u).addOnSuccessListener(a -> {
                                            Log.d("TAG", "addBibit: sukses");
                                            Snackbar.make(view, "Berhasil update data", Snackbar.LENGTH_SHORT).show();
                                            dialog.dismiss();
                                            tampilDataPeriode();
                                        });
                                    } else if (!periode.equals(ayamDB.getPeriode()) && !periode.equals(select)){
                                        dialog.dismiss();
                                        Snackbar.make(view, "Tunggu sebentar", Snackbar.LENGTH_SHORT).show();

                                        DocumentReference ref = fStore.collection("ayam").document();
                                        String id = ref.getId();
                                        Map<String, Object> ayam = new HashMap<>();
                                        ayam.put("uid", id);
                                        ayam.put("jumlah", jumlah);
                                        ayam.put("periode", periode);
                                        ayam.put("tersedia", jumlah);
                                        ayam.put("terjual", "0");
                                        ayam.put("hidup", jumlah);
                                        ayam.put("mati", "0");
                                        ayam.put("sakit", "0");
                                        ayam.put("sembuh", "0");
                                        ayam.put("sehat", jumlah);
                                        ayam.put("create_at", date);
                                        ayam.put("next_periode", nextPeride);

                                        ref.set(ayam).addOnSuccessListener(a -> {
                                            Log.d("TAG", "addBibit: sukses");
                                            Snackbar.make(view, "Berhasil menambahkan data", Snackbar.LENGTH_SHORT).show();
                                            dialog.dismiss();
                                            tampilDataPeriode();
                                        });
                                    }
                                }
                            }
                        }
                    });
                } else {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(requireActivity());
                    builder1.setMessage("Pilih periode dengan benar");
                    builder1.setNegativeButton("Ya", (dialogInterface, i) -> dialogInterface.dismiss());
                    builder1.show();
                }
            }
        });
        dialog.show();
    }

    @Override
    public void onRefresh() {
        new Handler().postDelayed(() -> {
            binding.swipe.setRefreshing(false);
            tampilDataPeriode();
        }, 1000);
    }
}