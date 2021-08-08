package com.pranto.NoteBook;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.pranto.NoteBook.Auth.Login;
import com.pranto.NoteBook.Auth.Register;
import com.pranto.NoteBook.Note.AddNote;
import com.pranto.NoteBook.Note.EditNote;
import com.pranto.NoteBook.Note.NoteDetailsActivity;
import com.pranto.NoteBook.model.Adapter;
import com.pranto.NoteBook.model.Note;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    DrawerLayout drawerLayout;
    ActionBarDrawerToggle toggle;
    NavigationView nav_view;
    RecyclerView noteLists;
    Adapter adapter;
    FirebaseFirestore firebaseFirestore;
    FirestoreRecyclerAdapter<Note,NoteViewHolder> noteAdapter;
    FirebaseAuth firebaseAuth;
    FirebaseUser user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Note Book");

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        Query query = firebaseFirestore.collection("notes").document(user.getUid()).collection("myNotes").orderBy("title",Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<Note> allNotes = new FirestoreRecyclerOptions.Builder<Note>()
                .setQuery(query,Note.class).build();

        noteAdapter = new FirestoreRecyclerAdapter<Note, NoteViewHolder>(allNotes) {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            protected void onBindViewHolder(@NonNull NoteViewHolder noteViewHolder, int i, @NonNull Note note) {

                noteViewHolder.noteTitle.setText(note.getTitle());
                noteViewHolder.noteContent.setText(note.getContent());
                int colorCode = getRandomColor();
                String docId  = noteAdapter.getSnapshots().getSnapshot(i).getId();
                noteViewHolder.cardView.setCardBackgroundColor(noteViewHolder.view.getResources().getColor(colorCode,null));
                noteViewHolder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(v.getContext(), NoteDetailsActivity.class);
                        intent.putExtra("content",note.getContent());
                        intent.putExtra("title",note.getTitle());
                        intent.putExtra("colorCode",colorCode);
                        intent.putExtra("docId",docId);
                        v.getContext().startActivity(intent);
                    }
                });
                ImageView menuIcon = noteViewHolder.view.findViewById(R.id.menuIconId);
                menuIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        Toast.makeText(getApplicationContext(),"Clicked",Toast.LENGTH_SHORT).show();
                        String docId = noteAdapter.getSnapshots().getSnapshot(i).getId();
                        PopupMenu menu = new PopupMenu(v.getContext(),v);
                        menu.setGravity(Gravity.END);
                        menu.getMenu().add("Edit").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                Intent intent = new Intent(v.getContext(), EditNote.class);
                                intent.putExtra("title",note.getTitle());
                                intent.putExtra("content",note.getContent());
                                intent.putExtra("docId",docId);
                                intent.putExtra("colorCode",colorCode);
                                startActivity(intent);
                                return false;
                            }
                        });
                        menu.getMenu().add("Delete").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                DocumentReference docRef = firebaseFirestore.collection("notes").document(user.getUid()).collection("myNotes").document(docId);
                                docRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(getApplicationContext(),"Note Deleted Successfully",Toast.LENGTH_LONG).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getApplicationContext(),"Error in Deleting Note",Toast.LENGTH_SHORT).show();
                                    }
                                });
                                return false;
                            }
                        });
                        menu.show();
                    }
                });
            }

            @NonNull
            @Override
            public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_view_layout,parent,false);

                return new NoteViewHolder(view);
            }
        };


        noteLists = findViewById(R.id.noteListId);
        drawerLayout = findViewById(R.id.drawer);
        nav_view = findViewById(R.id.nav_view);
        nav_view.setNavigationItemSelectedListener(this);

        toggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.open,R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.setDrawerIndicatorEnabled(true);
        toggle.syncState();

        noteLists.setLayoutManager(new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL));
        noteLists.setAdapter(noteAdapter);

        View headerView = nav_view.getHeaderView(0);
        TextView userName = headerView.findViewById(R.id.userDisplayName);
        TextView userEmail = headerView.findViewById(R.id.userDisplayEmail);

        if(user.isAnonymous())
        {
            userEmail.setVisibility(View.GONE);
            userName.setText("Temporary User");
        }
        else
        {
            userEmail.setText(user.getEmail());
            userName.setText(user.getDisplayName());
        }

        FloatingActionButton fab = findViewById(R.id.addFabId);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), AddNote.class);
                startActivity(intent);
            }
        });

    }

    private class NoteViewHolder extends RecyclerView.ViewHolder {

        TextView noteTitle,noteContent;
        View view;
        CardView cardView;
        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            noteTitle = itemView.findViewById(R.id.titleId);
            noteContent = itemView.findViewById(R.id.contentId);
            view = itemView;
            cardView = itemView.findViewById(R.id.notecardId);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        drawerLayout.closeDrawer(GravityCompat.START);

        switch (item.getItemId())
        {
            case R.id.noteId:

                startActivity(new Intent(getApplicationContext(),MainActivity.class));

                break;
            case R.id.addNoteId:
                startActivity(new Intent(this,AddNote.class));
                break;
            case R.id.logoutId:
                checkUser();
                break;
            case R.id.syncNote:
                if(user.isAnonymous())
                {
                    startActivity(new Intent(getApplicationContext(), Login.class));
                }
                else
                {
                    Toast.makeText(getApplicationContext(),"You are Connected.",Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.shareAppId:
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT,"Share this app");
                String app_url = "Download the app: \n https://play.google.com/store/apps/details?id=com.pranto.NoteBook";
                shareIntent.putExtra(Intent.EXTRA_TEXT,app_url);
                startActivity(Intent.createChooser(shareIntent,"Share Via"));
                break;
            case R.id.ratingId:

                rateApp();

                break;
            default:
                Toast.makeText(this,"coming soon",Toast.LENGTH_SHORT).show();
        }

        return false;
    }
    public void rateApp()
    {
//        try
//        {
//            Intent rateIntent = rateIntentForUrl("market://details?id=com.pranto.NoteBook");
//            startActivity(rateIntent);
//        }
//        catch (ActivityNotFoundException e)
//        {
            Intent rateIntent = rateIntentForUrl("https://play.google.com/store/apps/details");
            startActivity(rateIntent);
//        }
    }

    private Intent rateIntentForUrl(String url)
    {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("%s?id=%s", url, getPackageName())));
        int flags = Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_MULTIPLE_TASK;
        if (Build.VERSION.SDK_INT >= 21)
        {
            flags |= Intent.FLAG_ACTIVITY_NEW_DOCUMENT;
        }
        else
        {
            //noinspection deprecation
            flags |= Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET;
        }
        intent.addFlags(flags);
        return intent;
    }

    private void checkUser() {
        if(user.isAnonymous())
        {
            displayAlert();
        }else
        {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getApplicationContext(),SplashScreen.class));
            finish();
        }
    }

    private void displayAlert() {
        AlertDialog.Builder warning = new AlertDialog.Builder(this)
                .setTitle("Are you sure...?")
                .setMessage("You are logged in with Temporary Account. Logging out will Delete All the notes.")
                .setPositiveButton("Sync Note", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(getApplicationContext(), Register.class));
//                        finish();
                    }
                }).setNegativeButton("Logout", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // ToDo: delete all the notes created by the Anon user


                        // ToDo: delete the Anon user

                        user.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                startActivity(new Intent(getApplicationContext(),SplashScreen.class));
                                finish();
                            }
                        });
                    }
                });
        warning.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu,menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == R.id.settingsId)
        {
            Toast.makeText(this,"Coming Soon...",Toast.LENGTH_SHORT).show();
        }
        if(item.getItemId() == R.id.aboutMeId)
        {
            startActivity(new Intent(getApplicationContext(),AboutMe.class));
        }

        return super.onOptionsItemSelected(item);
    }
    private int getRandomColor() {
        List<Integer> colorCode = new ArrayList<>();
        colorCode.add(R.color.bg0);
        colorCode.add(R.color.bg1);
        colorCode.add(R.color.bg2);
        colorCode.add(R.color.bg3);
        colorCode.add(R.color.bg4);
        colorCode.add(R.color.bg5);
        colorCode.add(R.color.bg6);
        colorCode.add(R.color.bg7);
        colorCode.add(R.color.bg8);
        colorCode.add(R.color.bg9);
        colorCode.add(R.color.bg10);

        Random randomColor = new Random();
        int number = randomColor.nextInt(colorCode.size());
        return colorCode.get(number);
    }

    @Override
    protected void onStart() {
        super.onStart();
        noteAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(noteAdapter!=null)
        {
            noteAdapter.stopListening();
        }
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        if(this.drawerLayout.isDrawerOpen(GravityCompat.START))
        {
            this.drawerLayout.closeDrawer(GravityCompat.START);
            return;
        }
        showWarning();
    }
    private void showWarning() {
        AlertDialog.Builder warning = new AlertDialog.Builder(this)
                .setTitle("")
                .setMessage("Are you sure you want to exit..?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finishAffinity();
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //nothing;
                    }
                });
        warning.show();
    }
}