package com.kuycoding.wijayafarm.ui.petugas;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.kuycoding.wijayafarm.R;
import com.kuycoding.wijayafarm.databinding.ActivityPetugasBinding;
import com.kuycoding.wijayafarm.ui.start.LoginActivity;

public class PetugasActivity extends AppCompatActivity {
    private ActivityPetugasBinding binding;
    private FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPetugasBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NavController navController = Navigation.findNavController(this, R.id.fragment);
        NavigationUI.setupWithNavController(binding.bottomNavigationView, navController);
    }
}