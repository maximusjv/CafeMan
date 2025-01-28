package com.example.cafeman.credentials;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.cafeman.R;
import com.example.cafeman.databinding.ActivityCredentialsInputBinding;
import com.example.cafeman.databinding.ActivityWelcomeBinding;

public class ActivityWelcome extends AppCompatActivity {

    private ActivityWelcomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityWelcomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.btnSingIn.setOnClickListener(v -> {
            Intent intent = new Intent(this, ActivityCredentialsInput.class);
            intent.putExtra("dialogueType", ActivityCredentialsInput.CredentialsDialogueType.SING_IN);
            startActivity(intent);
            finish();
        });
        binding.btnSingUp.setOnClickListener(v -> {
            Intent intent = new Intent(this, ActivityCredentialsInput.class);
            intent.putExtra("dialogueType", ActivityCredentialsInput.CredentialsDialogueType.SING_UP);
            startActivity(intent);
            finish();
        });
    }
}