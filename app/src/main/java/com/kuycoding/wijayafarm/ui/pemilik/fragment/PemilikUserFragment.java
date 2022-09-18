package com.kuycoding.wijayafarm.ui.pemilik.fragment;

import android.app.AlertDialog;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.kuycoding.wijayafarm.R;
import com.kuycoding.wijayafarm.adapter.UserAdapter;
import com.kuycoding.wijayafarm.databinding.DialogFormUserBinding;
import com.kuycoding.wijayafarm.databinding.FragmentPemilikUserBinding;
import com.kuycoding.wijayafarm.model.User;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class PemilikUserFragment extends Fragment {
    private FragmentPemilikUserBinding binding;
    private FirebaseFirestore fStore;
    private FirebaseAuth fAuth;
    private FirebaseAuth fAuth2;
    private UserAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPemilikUserBinding.inflate(LayoutInflater.from(container.getContext()), container, false);
        // Inflate the layout for this fragment
        return binding.getRoot();
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        FirebaseOptions firebaseOptions = new FirebaseOptions.Builder()
                .setDatabaseUrl("https://wijaya-farm-default-rtdb.firebaseio.com")
                        .setApiKey("AIzaSyAQqmzB7bCNtn7tqCP2tpC46cflAvBaZRc")
                                .setApplicationId("wijaya-farm")
                                        .build();
        try {
            FirebaseApp firebaseApp = FirebaseApp.initializeApp(requireActivity(), firebaseOptions, "wijaya");
            fAuth2 = FirebaseAuth.getInstance(firebaseApp);
        }catch (IllegalStateException e) {
            fAuth2 = FirebaseAuth.getInstance(FirebaseApp.getInstance("wijaya"));
        }

        binding.fab.setOnClickListener(v -> addUser());
        initData();
    }

    private void initData() {
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        Query query = fStore.collection("user").orderBy("role").orderBy("nama", Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<User> options = new FirestoreRecyclerOptions.Builder<User>().setQuery(query, User.class).build();

        adapter = new UserAdapter(options);
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
                    UserAdapter.viewHolder viewHolder1 = (UserAdapter.viewHolder) viewHolder;
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
//                        .addPadding(TypedValue.COMPLEX_UNIT_DIP, 16, 16, 16)
                        .create()
                        .decorate();
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        }).attachToRecyclerView(binding.recyclerView);
        adapter.setOnClickListener(snapshot -> {
            DocumentReference reference = snapshot.getReference();
            User user = snapshot.toObject(User.class);

            reference.delete().addOnSuccessListener(unused -> Snackbar.make(binding.getRoot(), "Hapus Data Berhasil", Snackbar.LENGTH_SHORT)
                    .setAction("BATAL", view -> {
                        if (user != null) {
                            reference.set(user);
                        }
                    }).show());

        });
    }

    private void addUser() {
        DialogFormUserBinding userBinding = DialogFormUserBinding.inflate(getLayoutInflater());
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(userBinding.getRoot());

        AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        String[] roleList = new String[] {
                "pemilik", "petugas"
        };
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, roleList);
        userBinding.edtRole.setAdapter(arrayAdapter);

        userBinding.btnTambah.setOnClickListener(v -> {
            String er = "Tidak boleh kosong!";
            String nama = Objects.requireNonNull(userBinding.edtNama.getText()).toString();
            String email = Objects.requireNonNull(userBinding.edtEmail.getText()).toString();
            String pass = Objects.requireNonNull(userBinding.edtPass.getText()).toString();
            String role = userBinding.edtRole.getText().toString();

            if (TextUtils.isEmpty(nama)) {
                userBinding.edtNama.setError(er);
            } else if (TextUtils.isEmpty(email)) {
                userBinding.edtEmail.setError(er);
            } else if (TextUtils.isEmpty(pass)) {
                userBinding.edtPass.setError(er);
            } else if (pass.length() < 5) {
                userBinding.edtPass.setError("Password kurang dari 5 Karakter");
            } else if (TextUtils.isEmpty(role)) {
                userBinding.edtRole.setError(er);
            } else {
                alertDialog.dismiss();
                binding.progressBar.setVisibility(View.VISIBLE);
                fAuth2.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid = Objects.requireNonNull(fAuth2.getCurrentUser()).getUid();

                        DocumentReference reference = fStore.collection("user").document(uid);
                        Map<String , Object> user = new HashMap<>();
                        user.put("nama", nama);
                        user.put("email", email);
                        user.put("password", pass);
                        user.put("role", role);

                        reference.set(user).addOnSuccessListener(documentSnapshot -> Log.d("TAG", "onSuccess: "));
                        Snackbar.make(binding.getRoot(), "Berhasil buat akun, silakan login", Snackbar.LENGTH_SHORT).show();
                        alertDialog.dismiss();
                        binding.progressBar.setVisibility(View.GONE);
                        fAuth2.signOut();
                    } else {
                        alertDialog.dismiss();
                        Snackbar.make(binding.getRoot(), "Gagal buat akun", Snackbar.LENGTH_SHORT).show();
                        binding.progressBar.setVisibility(View.GONE);
                    }
                });
                Snackbar.make(binding.getRoot(), "Tunggu sebentar", Snackbar.LENGTH_SHORT).show();
            }
        });
        alertDialog.show();
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.startListening();
    }

    @Override
    public void onPause() {
        super.onPause();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}