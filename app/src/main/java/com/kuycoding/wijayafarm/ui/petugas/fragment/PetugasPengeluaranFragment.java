package com.kuycoding.wijayafarm.ui.petugas.fragment;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.kuycoding.wijayafarm.R;
import com.kuycoding.wijayafarm.adapter.PengeluaranAdapter;
import com.kuycoding.wijayafarm.databinding.DialogFormPengeluaranBinding;
import com.kuycoding.wijayafarm.databinding.FragmentPetugasPengeluaranBinding;
import com.kuycoding.wijayafarm.model.Ayam;
import com.kuycoding.wijayafarm.model.Pengeluaran;

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

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class PetugasPengeluaranFragment extends Fragment {
    private FragmentPetugasPengeluaranBinding binding;
    private View view;
    private PengeluaranAdapter adapter;

    private FirebaseFirestore fStore;
    private Query query;

    private String date;
    private String select;
    private String nama;
    private final HashSet<String> hashSet = new HashSet<>();
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPetugasPengeluaranBinding.inflate(inflater, container, false);
        view = binding.getRoot();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        nama = preferences.getString("nama","");

        date = new SimpleDateFormat("MMM, yyyy", Locale.getDefault()).format(new Date());
        fStore = FirebaseFirestore.getInstance();

        binding.fab.setOnClickListener(view1 -> addData());

        data();
    }

    private void data() {
        query = fStore.collection("pengeluaran").orderBy("periode", Query.Direction.DESCENDING);
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
        binding.recyclerView.setHasFixedSize(false);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
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
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                if (direction == ItemTouchHelper.LEFT) {
                    Snackbar.make(viewHolder.itemView, "Item " + "terhapus" + ".", Snackbar.LENGTH_LONG).show();
                    PengeluaranAdapter.viewHolder viewHolder1 = (PengeluaranAdapter.viewHolder) viewHolder;
                    viewHolder1.deleteItem();
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        .addBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.red))
                        .addActionIcon(R.drawable.ic_baseline_delete_24)
                        .addSwipeLeftLabel("Hapus")
                        .setSwipeLeftLabelColor(Color.WHITE)
                        .addCornerRadius(TypedValue.COMPLEX_UNIT_DIP, 16)
                        .addPadding(TypedValue.COMPLEX_UNIT_DIP, 16, 16, 16)
                        .create()
                        .decorate();
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        }).attachToRecyclerView(binding.recyclerView);
        adapter.setOnClickListener(snapshot -> {
            DocumentReference reference = snapshot.getReference();
            Pengeluaran pengeluaran = snapshot.toObject(Pengeluaran.class);
            reference.delete().addOnSuccessListener(unused -> Snackbar.make(binding.recyclerView, "Hapus sukses ", Snackbar.LENGTH_LONG)
                    .setAction("BATAL", view1 -> {
                        if (pengeluaran != null) {
                            reference.set(pengeluaran);
                        }
                    }).show());
        });
    }

    private void addData() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        DialogFormPengeluaranBinding binding1 = DialogFormPengeluaranBinding.inflate(getLayoutInflater());
        builder.setView(binding1.getRoot());

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog.OnDateSetListener dateSetListener = (datePicker, i, i1, i2) -> {
            calendar.set(Calendar.YEAR, i);
            calendar.set(Calendar.MONTH, i1);
            calendar.set(Calendar.DAY_OF_MONTH, i2);
            String myFormat="dd-MM-yyyy";
            SimpleDateFormat dateFormat=new SimpleDateFormat(myFormat, Locale.getDefault());
            binding1.edtTanggal.setText(dateFormat.format(calendar.getTime()));
        };
        binding1.edtTanggal.setOnClickListener(view -> new DatePickerDialog(requireActivity(), dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show());

        List<String > list = new ArrayList<>();
        ArrayAdapter<String> stringArrayAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, list);
        stringArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding1.edtPeriode.setAdapter(stringArrayAdapter);
        fStore.collection("ayam").get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                    String s = snapshot.getString("periode");
                    Log.d("TAG", "onSuccess: " + s);
                    list.add(s);
                }
                hashSet.addAll(list);
                list.clear();
                list.addAll(hashSet);
                stringArrayAdapter.notifyDataSetChanged();
            }
        });

        binding1.btnTambah.setOnClickListener(view -> {

            String tanggal = Objects.requireNonNull(binding1.edtTanggal.getText()).toString();
            String periode = Objects.requireNonNull(binding1.edtPeriode.getText()).toString();
            String keperluan = Objects.requireNonNull(binding1.edtKeperluan.getText()).toString();
            String jumlah_satuan = Objects.requireNonNull(binding1.edtSatuan.getText()).toString();
            String biaya = Objects.requireNonNull(binding1.edtBiaya.getText()).toString();

            if (tanggal.isEmpty()) {
                binding1.edtTanggal.setError("Tidak boleh kosong!");
            } else if (periode.isEmpty()) {
                binding1.edtPeriode.setError("Tidak boleh kosong!");
            } else if (keperluan.isEmpty()) {
                binding1.edtKeperluan.setError("Tidak boleh kosong!");
            } else if (jumlah_satuan.isEmpty()) {
                binding1.edtSatuan.setError("Tidak boleh kosong!");
            } else if (biaya.isEmpty()) {
                binding1.edtBiaya.setError("Tidak boleh kosong!");
            } else {
                fStore.collection("ayam").whereEqualTo("periode", periode).get().addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                            Ayam ayam = snapshot.toObject(Ayam.class);
                            String periodeDB = ayam.getPeriode();
                            if (periodeDB.equals(periode)) {
                                //ada
                                DocumentReference reference = fStore.collection("pengeluaran").document();
                                String id = reference.getId();
                                String uid = FirebaseAuth.getInstance().getUid();
                                Map<String, Object> insert = new HashMap<>();
                                insert.put("id", id);
                                insert.put("uid", uid);
                                insert.put("periode", periode);
                                insert.put("tanggal", tanggal);
                                insert.put("keperluan", keperluan);
                                insert.put("jumlah", jumlah_satuan);
                                insert.put("biaya", biaya);
                                insert.put("nama", nama);
                                reference.set(insert).addOnSuccessListener(unused -> {
                                    query = fStore.collection("pengeluaran").whereEqualTo("periode", date);
                                    dialog.dismiss();
                                    Snackbar.make(this.view, "Berhasil tambah data", Snackbar.LENGTH_SHORT).show();
                                    data();
                                }).addOnFailureListener(e -> Log.d("TAG", "onFailure: " + e.getMessage()));
                            }
                        }
                    } else {
                        Toast.makeText(requireContext(), "Periode tidak ditemukan", Toast.LENGTH_SHORT).show();
                    }
                });

            }
            Snackbar.make(binding.getRoot(), "Tunggu sebentar", Snackbar.LENGTH_SHORT).show();
        });
        dialog.show();
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.startListening();
    }
}