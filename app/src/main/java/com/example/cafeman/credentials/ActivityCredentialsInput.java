package com.example.cafeman.credentials;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.cafeman.R;
import com.example.cafeman.databinding.ActivityCredentialsInputBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class ActivityCredentialsInput extends AppCompatActivity {
    public enum CredentialsDialogueType {
        SING_IN,
        SING_UP,
        RESET_PASSWORD
    }
    ActivityCredentialsInputBinding binding;
    CredentialsDialogueType dialogueType = CredentialsDialogueType.SING_IN;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityCredentialsInputBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        if(extras != null)
            updateDialogType(extras.getSerializable("dialogueType", CredentialsDialogueType.class));

        binding.tvNav1.setOnClickListener(v -> {
                switch (dialogueType) {
                    case SING_IN:
                        updateDialogType(CredentialsDialogueType.SING_UP);
                        break;
                    case SING_UP:
                    case RESET_PASSWORD:
                        updateDialogType(CredentialsDialogueType.SING_IN);
                        break;
                }
        });

        binding.tvNav2.setOnClickListener(v -> {
            switch (dialogueType) {
                case RESET_PASSWORD:
                    updateDialogType(CredentialsDialogueType.SING_UP);
                    break;
                case SING_UP:
                case SING_IN:
                    updateDialogType(CredentialsDialogueType.RESET_PASSWORD);
                    break;
            }
        });

        binding.btnAction.setOnClickListener(v -> {
            credentialsCheck();
        });
    }

    private void credentialsCheck() {
        switch (dialogueType) {
            case RESET_PASSWORD:
                 resetPassword();
                break;
            case SING_IN:
                signIn();
                break;
            case SING_UP:
                signUp();
                break;
        }
    }

    private void signUp() {
        String name = getTextFromEditText(R.id.et_name);
        String email = getTextFromEditText(R.id.et_email);
        String password = getTextFromEditText(R.id.et_password);
        String passwordConfirm = getTextFromEditText(R.id.et_password_confirm);

        // Validate input fields (e.g., check if passwords match, etc.)

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                        user.updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(name).build())
                                .addOnCompleteListener(profileTask -> {
                                    if (profileTask.isSuccessful()) {
                                        showToast("Registration successful!");
                                        // Navigate to the next activity
                                    } else {
                                        logError("Error updating display name", profileTask.getException());
                                    }
                                });
                    } else {
                        logError("Error creating user", task.getException());
                        showToast("Registration failed. Please try again.");
                    }
                });
    }

    private void signIn() {
        String email = getTextFromEditText(R.id.et_email);
        String password = getTextFromEditText(R.id.et_password);
        boolean rememberMe = ((CheckBox)binding.frameDialogue.findViewById(R.id.cb_remember_me)).isChecked();

        // Validate input fields

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        showToast("Sign-in successful!");
                        // Navigate to the next activity

                        if (task.isSuccessful() && rememberMe) {
                            SharedPreferences sharedPrefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPrefs.edit();
                            editor.putString("email", email);

                            // Encrypt the password before storing
                            editor.putString("password", password);

                            editor.putBoolean("rememberMe", true);
                            editor.apply();
                        }

                    } else {
                        logError("Error signing in", task.getException());
                        showToast("Sign-in failed. Please try again.");
                    }
                });
    }

    private void resetPassword() {
        String email = getTextFromEditText(R.id.et_email);

        // Validate email address

        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseAuth.getInstance().sendPasswordResetEmail(email);
                        showToast("Password reset email sent. Check your inbox.");
                    } else {
                        logError("Error sending password reset email", task.getException());
                        showToast("Error sending password reset email. Please try again.");
                    }
                });
    }


    private String getTextFromEditText(int editTextId) {
        EditText editText = binding.frameDialogue.findViewById(editTextId);
        return editText.getText().toString().trim();
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void logError(String message, Exception exception) {
        Log.e("Firebase", message + ": " + exception.getMessage());
    }

    protected void updateDialogType(CredentialsDialogueType dialogType) {

        this.dialogueType = dialogType;

        LayoutInflater inflater = getLayoutInflater();
        binding.frameDialogue.removeAllViews();

        switch (dialogType) {
            case SING_IN:


                binding.tvTitle.setText(R.string.sing_in_text);
                binding.btnAction.setText(R.string.sing_in_text);
                inflater.inflate(R.layout.sing_in_dialogue,  binding.frameDialogue, true);

                SharedPreferences sharedPrefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                boolean rememberMe = sharedPrefs.getBoolean("rememberMe", false);
                if (rememberMe) {
                    String email = sharedPrefs.getString("email", "");
                    String password = sharedPrefs.getString("password", "");

                    EditText etEmail = binding.frameDialogue.findViewById(R.id.et_email);
                    EditText etPassword = binding.frameDialogue.findViewById(R.id.et_password);
                    CheckBox cbRememberMe = binding.frameDialogue.findViewById(R.id.cb_remember_me);

                    etEmail.setText(email);
                    etPassword.setText(password);
                    cbRememberMe.setChecked(true);
                }


                binding.tvNav1.setText(R.string.sing_up_text);
                binding.tvNav2.setText(R.string.reset_password_text);
                break;
            case SING_UP:
                binding.tvTitle.setText(R.string.sing_up_text);
                binding.btnAction.setText(R.string.sing_up_text);
                inflater.inflate(R.layout.sing_up_dialogue,  binding.frameDialogue, true);

                binding.tvNav1.setText(R.string.sing_in_text);
                binding.tvNav2.setText(R.string.reset_password_text);
                break;
            case RESET_PASSWORD:
                binding.tvTitle.setText(R.string.reset_password_text);
                binding.btnAction.setText(R.string.reset_password_text);
                inflater.inflate(R.layout.reset_password_dialogue,  binding.frameDialogue, true);

                binding.tvNav1.setText(R.string.sing_in_text);
                binding.tvNav2.setText(R.string.sing_up_text);
                break;
        }
    }
}