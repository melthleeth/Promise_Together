package com.myfirstapp.logintest2;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.CalendarContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.PopupMenu;
import android.view.View.OnClickListener;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.myfirstapp.logintest2.MainActivity;
import com.myfirstapp.logintest2.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;




//daily task, missions 보여주기
public class StartActivity extends AppCompatActivity {

    /*TextView txtPhone;
    ImageButton mProfileButton;

    ImageView imageView;
    Integer REQUEST_CAMERA=1, SELECT_FILE=0;

    private boolean fabExpanded = false;
    private FloatingActionButton fabSettings, fabMission, fabReminders;
    private LinearLayout layoutFabMission;
    private LinearLayout layoutFabReminders;

    Button DialogSave, Show;
    TextView Myname;
    Dialog thisDialog;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;


    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolBar);
        toolbar.setTitle("Promise Together");
        setSupportActionBar(toolbar);



        //profile picture's menu
        mProfileButton = (ImageButton) findViewById(R.id.profile);
        mProfileButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                //Creating the instance of PopupMenu
                PopupMenu popup = new PopupMenu(StartActivity.this, mProfileButton);
                //Inflating the Popup using xml file
                popup.getMenuInflater().inflate(R.menu.picture_menu, popup.getMenu());

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        Toast.makeText(StartActivity.this,"You Clicked : " + item.getTitle(),Toast.LENGTH_SHORT).show();

                        final CharSequence[] items={"Camera","Gallery", "Cancel"};

                        AlertDialog.Builder builder = new AlertDialog.Builder(StartActivity.this);
                        builder.setTitle("Add Image");

                        builder.setItems(items, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (items[i].equals("Camera")) {

                                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                    startActivityForResult(intent, REQUEST_CAMERA);

                                } else if (items[i].equals("Gallery")) {

                                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                    intent.setType("image/*");
                                    startActivityForResult(intent, SELECT_FILE);

                                } else if (items[i].equals("Cancel")) {
                                    dialogInterface.dismiss();
                                }
                            }
                        });
                        builder.show();

                        return true;
                    }

                });

                popup.show();//showing popup menu
            }
        });//closing the setOnClickListener method



        //floating button
        fabSettings = (FloatingActionButton) this.findViewById(R.id.fabSetting);
        fabMission = (FloatingActionButton) this.findViewById(R.id.fabMission);
        fabReminders= (FloatingActionButton) this.findViewById(R.id.fabReminders);
        layoutFabMission = (LinearLayout) this.findViewById(R.id.layoutFabMission);
        layoutFabReminders = (LinearLayout) this.findViewById(R.id.layoutFabReminders);
        //layoutFabSettings = (LinearLayout) this.findViewById(R.id.layoutFabSettings);

        //When main Fab (Settings) is clicked, it expands if not expanded already.
        //Collapses if main FAB was open already.
        //This gives FAB (Settings) open/close behavior
        fabSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fabExpanded == true){
                    closeSubMenusFab();
                } else {
                    openSubMenusFab();
                }
            }
        });


        fabReminders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                thisDialog = new Dialog(StartActivity.this);
                thisDialog.setTitle("Promise");
                thisDialog.setContentView(R.layout.reminder_dialog);
                final EditText Write = (EditText)thisDialog.findViewById(R.id.write);
                Button SaveMyName = (Button)thisDialog.findViewById(R.id.SaveNow);

                Write.setEnabled(true);
                SaveMyName.setEnabled(true);

                SaveMyName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SharedPrefesSAVE(Write.getText().toString());
                        thisDialog.cancel();
                    }
                });
                thisDialog.show();
            }
        });
      /*  Show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences SP = getApplicationContext().getSharedPreferences("NAME", 0);
                Myname.setText(SP.getString("Name", null));
            }
        });
*/

  /*      fabMission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(StartActivity.this, Missions.class));
            }
        });


        //Only main FAB is visible in the beginning
        closeSubMenusFab();


    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_sign_out){
            signOut();
        }

        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id2 = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id2 == R.id.action_settings) {
            return true;
        }


        return super.onOptionsItemSelected(item);

    }


    private void signOut(){

        AuthUI.getInstance().signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        startActivity(new Intent(StartActivity.this, MainActivity.class));
                        finish();
                    }
                });
    }



    public void calendar(View view) {

    }

    public void profile() {


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode== Activity.RESULT_OK){


            if(requestCode==REQUEST_CAMERA){

                Bundle bundle = data.getExtras();
                final Bitmap bmp = (Bitmap) bundle.get("data");
                mProfileButton.setImageBitmap(bmp);


            }else if(requestCode==SELECT_FILE){

                Uri selectedImageUri = data.getData();
                mProfileButton.setImageURI(selectedImageUri);

            }

        }
    }



    //closes FAB submenus
    private void closeSubMenusFab(){
        layoutFabMission.setVisibility(View.INVISIBLE);
        layoutFabReminders.setVisibility(View.INVISIBLE);
        fabSettings.setImageResource(R.drawable.ic_settings_black_24dp);
        fabExpanded = false;
    }

    //Opens FAB submenus
    private void openSubMenusFab(){
        layoutFabMission.setVisibility(View.VISIBLE);
        layoutFabReminders.setVisibility(View.VISIBLE);
        //Change settings icon to 'X' icon
        fabSettings.setImageResource(R.drawable.ic_close_black_24dp);
        fabExpanded = true;
    }


    public void SharedPrefesSAVE(String Name){
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("NAME", 0);
        SharedPreferences.Editor prefEDIT = prefs.edit();
        prefEDIT.putString("Name", Name);
        prefEDIT.commit();
    }*/
}

