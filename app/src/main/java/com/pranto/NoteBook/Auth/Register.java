package com.pranto.NoteBook.Auth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.pranto.NoteBook.MainActivity;
import com.pranto.NoteBook.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class Register extends AppCompatActivity {

    EditText rUserName,rUserEmail,rUserPass,rUserConfPass;
    Button syncAccount;
    TextView loginHere;
    ProgressBar progressBar;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Create New Notebook Account");

        rUserName = findViewById(R.id.userNameRegisterId);
        rUserEmail = findViewById(R.id.userEmailRegisterId);
        rUserPass = findViewById(R.id.passwordRegisterId);
        rUserConfPass = findViewById(R.id.passwordConfirmRegisterId);
        syncAccount = findViewById(R.id.createAccountSyncNowRegisterId);
        loginHere = findViewById(R.id.loginRegisterId);
        progressBar = findViewById(R.id.progressBar4RegisterId);

        firebaseAuth = FirebaseAuth.getInstance();

        loginHere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),Login.class));
                finish();
            }
        });

        syncAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName = rUserName.getText().toString();
                String userEmail = rUserEmail.getText().toString();
                String userPass = rUserPass.getText().toString();
                String userConfirmPass = rUserConfPass.getText().toString();

                if(userEmail.isEmpty() || userName.isEmpty() || userPass.isEmpty() || userConfirmPass.isEmpty())
                {
                    Toast.makeText(getApplicationContext(),"All Fields Are Required.",Toast.LENGTH_SHORT).show();
                    return;
                }

                if(!userPass.equals(userConfirmPass))
                {
                    rUserConfPass.setError("Password Do not Match");
                    return;
                }
                progressBar.setVisibility(View.VISIBLE);
                AuthCredential credential = EmailAuthProvider.getCredential(userEmail,userPass);
                firebaseAuth.getCurrentUser().linkWithCredential(credential).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Toast.makeText(getApplicationContext(),"Notes are Synced.",Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(),MainActivity.class));

                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                                .setDisplayName(userName)
                                .build();
                        user.updateProfile(request);
                        startActivity(new Intent(getApplicationContext(),MainActivity.class));

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(),"Failed to Connect. Try Again."+e.getMessage(),Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    }
                });

            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        finish();
        return super.onOptionsItemSelected(item);
    }
}