package com.kuycoding.wijayafarm.ui.start;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kuycoding.wijayafarm.R;
import com.kuycoding.wijayafarm.ui.pemilik.PemilikMenuActivity;
import com.kuycoding.wijayafarm.ui.petugas.PetugasActivity;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {
    private final FirebaseAuth fAuth = FirebaseAuth.getInstance();
    private final FirebaseFirestore fStore = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        long loadTime = 2000;
        new Handler().postDelayed(this::checkLogin, loadTime);
    }
    void checkLogin() {
        if (fAuth.getCurrentUser() == null) {
            Log.d("TAG", "checkLogin : signout ");
            startActivity(new Intent(this, LoginActivity.class));
            this.finish();
        } else {
            Log.d("TAG", "checkLogin : siggned ");
            String uid = fAuth.getCurrentUser().getUid();
            fStore.collection("user").document(uid).get().addOnCompleteListener(task -> {
                DocumentSnapshot documentSnapshot = task.getResult();
                if (documentSnapshot.exists()) {
                    String role = documentSnapshot.getString("role");
                    if (role != null) {
                        if (role.equals("pemilik")) {
                            Log.d("TAG", "checkLogin : siggned as pemilik");
                            startActivity(new Intent(this, PemilikMenuActivity.class));
                            finish();
                        } else if (role.equals("petugas")) {
                            Log.d("TAG", "checkLogin : siggned as petugas");
                            startActivity(new Intent(this, PetugasActivity.class));
                            finish();
                        } else {
                            startActivity(new Intent(this, LoginActivity.class));
                            finish();
                        }
                    }
                } else {
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                }
            });
        }
    }
}