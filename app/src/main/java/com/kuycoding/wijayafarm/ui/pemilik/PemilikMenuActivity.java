package com.kuycoding.wijayafarm.ui.pemilik;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.kuycoding.wijayafarm.R;
import com.kuycoding.wijayafarm.databinding.ActivityPemilikMenuBinding;

public class PemilikMenuActivity extends AppCompatActivity {

    private ActivityPemilikMenuBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityPemilikMenuBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_pemilik_menu);

        NavigationUI.setupWithNavController(binding.navView, navController);
    }

}