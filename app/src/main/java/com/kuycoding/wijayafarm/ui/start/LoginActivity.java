package com.kuycoding.wijayafarm.ui.start;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kuycoding.wijayafarm.databinding.ActivityLoginBinding;
import com.kuycoding.wijayafarm.ui.pemilik.PemilikMenuActivity;
import com.kuycoding.wijayafarm.ui.petugas.PetugasActivity;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    private static final String PREFER_NAME = "Reg";
    private ActivityLoginBinding binding;

    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        binding.btnLogin.setOnClickListener(v -> login());
    }
    void login() {
        final String email = Objects.requireNonNull(binding.edtEmail.getText()).toString().trim();
        final String passwd = Objects.requireNonNull(binding.edtPass.getText()).toString().trim();
//        final String emailPattern = "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\\\.[A-Z]{2,6}$";

        if (TextUtils.isEmpty(email)) {
            binding.edtEmail.setError("Email tidak boleh kosong!");
        } else if (TextUtils.isEmpty(passwd)) {
            binding.edtPass.setError("Password tidak boleh kosong!");
        } else if (passwd.length() < 5) {
            binding.edtPass.setError("Passowrd kurang dari 5 karakter!");
        } else {
            onShowProgress();
            fAuth.signInWithEmailAndPassword(email, passwd)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String uid = fAuth.getUid();
                            CollectionReference reference = fStore.collection("user");

                            if (uid != null) {
                                reference.document(uid).get().addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        FirebaseUser fUser = fAuth.getCurrentUser();
                                        if (fUser == null) {
                                            onHideProgress();
                                            Snackbar.make(binding.getRoot(), "Tidak dapat login, email/password salah", Snackbar.LENGTH_SHORT).show();
                                            fAuth.signOut();
                                        } else {
                                            DocumentSnapshot snapshot = task1.getResult();
                                            if (snapshot.exists()) {
                                                String role = snapshot.getString("role");
                                                if (role != null) {
                                                    if (role.equals("pemilik")) {
                                                        onHideProgress();
                                                        startActivity(new Intent(this, PemilikMenuActivity.class));
                                                        finish();
                                                    } else if (role.equals("petugas")) {
                                                        onHideProgress();
                                                        startActivity(new Intent(this, PetugasActivity.class));
                                                        finish();
                                                    } else {
                                                        onHideProgress();
                                                        Snackbar.make(binding.getRoot(), "Tidak dapat login, email/password salah", Snackbar.LENGTH_SHORT).show();
                                                    }
                                                }
                                            }
                                        }
                                    }
                                });
                            }
                        } else {
                            onHideProgress();
                            Snackbar.make(binding.getRoot(), "Tidak dapat login, email/password salah", Snackbar.LENGTH_SHORT).show();
                        }
                    });
        }
    }
    public void onShowProgress() {
        binding.loading.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    public void onHideProgress() {
        binding.loading.setVisibility(View.GONE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }
}