package com.kuycoding.wijayafarm.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.kuycoding.wijayafarm.databinding.ActivityProfilBinding;

public class ProfilActivity extends AppCompatActivity {
    private ActivityProfilBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfilBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}