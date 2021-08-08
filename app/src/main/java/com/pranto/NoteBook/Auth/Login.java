package com.pranto.NoteBook.Auth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class Login extends AppCompatActivity {

    EditText lEmail,lPass;
    Button loginNow;
    TextView forgetPass,createAcc;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;
    ProgressBar spinner;
    FirebaseUser user ;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Login to NoteBook");



        spinner = findViewById(R.id.progressBar3LogInId);

        lEmail = findViewById(R.id.logInEmailId);
        lPass = findViewById(R.id.logInPasswordId);
        loginNow = findViewById(R.id.loginBtnId);
        forgetPass = findViewById(R.id.forgotPaswordLogInId);
        createAcc = findViewById(R.id.createAccountLogInId);

        forgetPass.setVisibility(View.INVISIBLE);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();



        showWarning();

        createAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),Register.class));
                finish();
            }
        });

        loginNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String Email = lEmail.getText().toString();
                String Pass = lPass.getText().toString();

                if(Email.isEmpty() || Pass.isEmpty())
                {
                    Toast.makeText(getApplicationContext(),"All Fields Are required.",Toast.LENGTH_SHORT).show();
                    return;
                }
                spinner.setVisibility(View.VISIBLE);

                user = FirebaseAuth.getInstance().getCurrentUser();

                // dleete notes first
                if(user.isAnonymous())
                {
                    firebaseFirestore.collection("notes").document(user.getUid()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(getApplicationContext(),"All Temp Notes are Deleted.",Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(),"Temporary Account's Note couldn't be deleted.",Toast.LENGTH_SHORT).show();
                        }
                    });
                    user.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(getApplicationContext(),"Temporary user Deleted.",Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(),"Temporary User couldn't be deleted.",Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                firebaseAuth.signInWithEmailAndPassword(Email,Pass).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {

//                        Toast.makeText(getApplicationContext(),user1.getUid(),Toast.LENGTH_SHORT).show();


                        Toast.makeText(getApplicationContext(),"Success !",Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        finish();

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(),"Login Failed. "+e.getMessage(),Toast.LENGTH_LONG).show();


                        firebaseAuth.signInAnonymously().addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                Toast.makeText(getApplicationContext(),"Logged in With Temporary Account.",Toast.LENGTH_SHORT).show();
//                                startActivity(new Intent(getApplicationContext(),MainActivity.class));
//                                finish();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(),"Error ! "+e.getMessage(),Toast.LENGTH_SHORT).show();
//                                finish();
                            }
                        });
                        spinner.setVisibility(View.GONE);
                    }
                });

            }
        });

    }

    private void showWarning() {
        AlertDialog.Builder warning = new AlertDialog.Builder(this)
                .setTitle("Are you sure...?")
                .setMessage("Linking Existing Account Will Delete All The Temporary Notes. Create New Account To Save Them.")
                .setPositiveButton("Save Notes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(getApplicationContext(), Register.class));
                        finish();
                    }
                }).setNegativeButton("Its Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //nothing;
                    }
                });
        warning.show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        startActivity(new Intent(getApplicationContext(),MainActivity.class));
        return super.onOptionsItemSelected(item);
    }
}