package com.kuycoding.wijayafarm.ui.petugas.fragment;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.kuycoding.wijayafarm.adapter.PanenAdapter;
import com.kuycoding.wijayafarm.databinding.DialogFormPanenBinding;
import com.kuycoding.wijayafarm.databinding.FragmentPetugasPanenBinding;
import com.kuycoding.wijayafarm.model.Ayam;
import com.kuycoding.wijayafarm.model.Panen;

import java.text.ParseException;
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

public class PetugasPanenFragment extends Fragment {
    private FragmentPetugasPanenBinding binding;
    private DialogFormPanenBinding binding1;
    private View view;

    private FirebaseFirestore fStore;
    private String today;
    private String select;

    private PanenAdapter adapter;
    private final HashSet<String> hashSet = new HashSet<>();
    private Query query;
    private SharedPreferences.Editor editor;
    private SharedPreferences preferences;

    private ArrayAdapter<String> stringArrayAdapter;
    private List<String> list;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentPetugasPanenBinding.inflate(inflater, container, false);
        view = binding.getRoot();
        return view;
    }

    @SuppressLint("SimpleDateFormat")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fStore = FirebaseFirestore.getInstance();
        today = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date());

        preferences = PreferenceManager.getDefaultSharedPreferences(requireActivity());
        editor = preferences.edit();

        binding.fab.setOnClickListener(view1 -> addData());
        data();
    }

    private void data() {
        query = fStore.collection("panen").orderBy("periode", Query.Direction.DESCENDING);
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
                query = fStore.collection("panen").whereEqualTo("periode", select);
                attachData(query);
                adapter.startListening();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        attachData(query);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void attachData(Query query) {
        binding.recyclerView.setHasFixedSize(false);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        FirestoreRecyclerOptions<Panen> options = new FirestoreRecyclerOptions.Builder<Panen>()
                .setQuery(query, Panen.class)
                .build();
        adapter = new PanenAdapter(options);
        binding.recyclerView.setAdapter(adapter);
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                binding.recyclerView.scrollToPosition(itemCount);
            }
        });
        adapter.setOnClickListener(snapshot -> {
            DocumentReference reference = snapshot.getReference();
            Panen panen = snapshot.toObject(Panen.class);
            if (panen != null) {
                int ayamPanen = Integer.parseInt(panen.getJumlah_ayam());
                fStore.collection("ayam").document(panen.getId_ayam()).get().addOnSuccessListener(snapshot1 -> {
                    if (snapshot1.exists()) {
                        int terjualDB = Integer.parseInt(Objects.requireNonNull(snapshot1.getString("terjual")));
                        int tersediaDB = Integer.parseInt(Objects.requireNonNull(snapshot1.getString("tersedia")));

                        editor.putString("terjual",snapshot1.getString("terjual"));
                        editor.putString("tersedia",snapshot1.getString("tersedia"));
                        editor.apply();

                        int totalTerjual = terjualDB - ayamPanen;
                        int totalTersedia = tersediaDB + ayamPanen;

                        DocumentReference refAyam = fStore.collection("ayam").document(panen.getId_ayam());
                        String terjual = String.valueOf(totalTerjual);
                        String tersedia = String.valueOf(totalTersedia);
                        Map<String, Object> updateAyam = new HashMap<>();
                        updateAyam.put("terjual", terjual);
                        updateAyam.put("tersedia", tersedia);
                        refAyam.update(updateAyam).addOnSuccessListener(avoid -> Log.d("TAG", "onEvent: sukses udate data ayam"));
                    }
                });
            }

            reference.delete().addOnSuccessListener(unused -> Snackbar.make(binding.recyclerView, "Hapus sukses ", Snackbar.LENGTH_LONG)
                    .setAction("BATAL", view1 -> {
                        if (panen != null) {
                            reference.set(panen);

                            String terjual = preferences.getString("terjual" ,"");
                            String tersedia = preferences.getString("tersedia" ,"");
                            DocumentReference refAyam = fStore.collection("ayam").document(panen.getId_ayam());
                            Map<String, Object> restore = new HashMap<>();
                            restore.put("terjual", terjual);
                            restore.put("tersedia", tersedia);
                            refAyam.update(restore).addOnSuccessListener(avoid -> {
                                Log.d("TAG", "onEvent: sukses restore data ayam");
                                preferences.edit().remove("terjual").remove("tersedia").apply();
                            });
                        }
                    }).show());
        });
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                if (direction == ItemTouchHelper.LEFT) {
                    Snackbar.make(viewHolder.itemView, "Item " + "terhapus" + ".", Snackbar.LENGTH_LONG).show();
                    PanenAdapter.viewHolder viewHolder1 = (PanenAdapter.viewHolder) viewHolder;
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
        binding.recyclerView.getRecycledViewPool().clear();
        adapter.notifyDataSetChanged();
    }

    private void addData() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        binding1 = DialogFormPanenBinding.inflate(getLayoutInflater());
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

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String brt = Objects.requireNonNull(binding1.edtBerat.getText()).toString();
                String hargaKg = Objects.requireNonNull(binding1.edtHargaPerkilo.getText()).toString();

                if (!brt.isEmpty() && !hargaKg.isEmpty()) {
                    int ttl_berat = Integer.parseInt(brt);
                    int harga_kilo = Integer.parseInt(hargaKg);
                    int totalHarga = ttl_berat * harga_kilo;
                    binding1.edtHargaTotal.setText(String.valueOf(totalHarga));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };
        list = new ArrayList<>();
        stringArrayAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, list);
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

        binding1.edtHargaPerkilo.addTextChangedListener(textWatcher);
        binding1.edtBerat.addTextChangedListener(textWatcher);
        binding1.btnTambah.setOnClickListener(view -> {
            String tanggal = Objects.requireNonNull(binding1.edtTanggal.getText()).toString();
            String nama_pembeli = Objects.requireNonNull(binding1.edtNama.getText()).toString();
            String jumlah_ayam = Objects.requireNonNull(binding1.edtJumlahAyam.getText()).toString();
            String total_berat = Objects.requireNonNull(binding1.edtBerat.getText()).toString();
            String harga_kg = Objects.requireNonNull(binding1.edtHargaPerkilo.getText()).toString();
            String total = Objects.requireNonNull(binding1.edtHargaTotal.getText()).toString();
            String periode = Objects.requireNonNull(binding1.edtPeriode.getText()).toString();

            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");

            if (tanggal.isEmpty()) {
                binding1.edtTanggal.setError("Tidak boleh kosong!");
            } else if (periode.isEmpty()) {
                binding1.edtPeriode.setError("Tidak boleh kosong!");
            } else if (nama_pembeli.isEmpty()) {
                binding1.edtNama.setError("Tidak boleh kosong!");
            } else if (jumlah_ayam.isEmpty()) {
                binding1.edtJumlahAyam.setError("Tidak boleh kosong!");
            } else if (total_berat.isEmpty()) {
                binding1.edtBerat.setError("Tidak boleh kosong!");
            } else if (harga_kg.isEmpty()) {
                binding1.edtHargaPerkilo.setError("Tidak boleh kosong!");
            } else if (total.isEmpty()) {
                binding1.edtHargaTotal.setError("Tidak boleh kosong!");
            } else {
                Query query = fStore.collection("ayam").whereEqualTo("periode", periode);
                query.get().addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        long days = 0;
                        Date dateToday = null;
                        String id_ayam = "";
                        String periode_ayam = "";
                        int tersediaDB = 0;
                        int terjualDB = 0;
                        int jumlah_jual_ayam = Integer.parseInt(jumlah_ayam);
                        try {
                            dateToday = sdf.parse(today);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                            Ayam ayam = snapshot.toObject(Ayam.class);
                            String periodeAyam = ayam.getNext_periode();
                            id_ayam = ayam.getUid();
                            periode_ayam = ayam.getPeriode();
                            tersediaDB = Integer.parseInt(ayam.getTersedia());
                            terjualDB = Integer.parseInt(ayam.getTerjual());
                            try {
                                Date datePeriode = sdf.parse(periodeAyam);
                                assert dateToday != null;
                                assert datePeriode != null;
                                long h = dateToday.getTime() - datePeriode.getTime();
                                long seconds = h / 1000;
                                long minutes = seconds / 60;
                                long hours = minutes / 60;
                                days = hours / 24;
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            Log.d("TAG", "days: " + ayam.getPeriode());
                        }
                        Snackbar.make(this.view, "Tunggu sebentar", Snackbar.LENGTH_SHORT).show();
                        binding1.btnTambah.setEnabled(false);
                        DocumentReference ref = fStore.collection("panen").document();
                        DocumentReference refAyam = fStore.collection("ayam").document(id_ayam);

                        String id = ref.getId();
                        String uid = FirebaseAuth.getInstance().getUid();
                        Map<String, Object> insert = new HashMap<>();
                        insert.put("id", id);
                        insert.put("id_ayam", id_ayam);
                        insert.put("uid", uid);
                        insert.put("tanggal", tanggal);
                        insert.put("nama_pembeli", nama_pembeli);
                        insert.put("jumlah_ayam", jumlah_ayam);
                        insert.put("total_berat", total_berat);
                        insert.put("harga_kg", harga_kg);
                        insert.put("harga_total", total);
                        insert.put("create_at", dateToday);
                        insert.put("periode", periode);

                        if (jumlah_jual_ayam > tersediaDB) {
                            Toast.makeText(view.getContext(), "Jumlah ayam tidak boleh melebihi jumlah Persediaan Ayam!", Toast.LENGTH_SHORT).show();
                        } else {
                            ref.set(insert).addOnSuccessListener(unused -> {
                                data();
                                dialog.dismiss();
                                Snackbar.make(this.view, "Berhasil tambah data", Snackbar.LENGTH_SHORT).show();
                            }).addOnFailureListener(e -> Log.d("TAG", "onFailure: " + e));

                            int totalTersedia = tersediaDB - jumlah_jual_ayam;
                            int totalTerjual;
                            totalTerjual = terjualDB + jumlah_jual_ayam;
                            String insertTerjual = String.valueOf(totalTerjual);
                            String insertTersedia = String.valueOf(totalTersedia);

                            Map<String, Object> update = new HashMap<>();
                            update.put("tersedia", insertTersedia);
                            update.put("terjual", insertTerjual);
                            refAyam.update(update).addOnSuccessListener(unused -> Log.d("TAG", "onSuccess: " + unused));
                        }

                        /*if (days >= 0 && days <= 10) {
                            Log.d("TAG", "addData days: " + days);
                        } else {
                            dialog.dismiss();
                            Snackbar.make(this.view, "Tidak dalam periode panen", Snackbar.LENGTH_SHORT).show();
                        }*/
                    } else {
                        binding1.btnTambah.setEnabled(true);
                        Toast.makeText(requireActivity(), "Periode tidak ditemukan!", Toast.LENGTH_SHORT).show();
                    }
                });

            }
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