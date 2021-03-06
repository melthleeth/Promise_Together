package com.myfirstapp.logintest2;


import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.PopupMenu;
import android.view.View.OnClickListener;
import android.support.design.widget.FloatingActionButton;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

//daily task, missions 보여주기
public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private FirebaseFirestore db;

    private TextView mTodo, mNotice, mNoticeContent;
    private RecyclerView mTodoList;

    private String UID;

    private ImageButton mProfileButton, mCalendarButton;

    private int REQUEST_CAMERA = 1, SELECT_FILE = 0;


    private boolean fabExpanded = false;
    private FloatingActionButton fabSettings, fabMission, fabReminders;
    private LinearLayout layoutFabMission;
    private LinearLayout layoutFabReminders;
    private Button Save, SaveNow;

    Button DialogSave, Show;
    TextView Myname;
    Dialog thisDialog;

    private PendingIntent pendingIntent;

    private RecyclerView recyclerView;
    private Adapter_Our_Missions adapter;


   Button delete;

   int groupID = 1; //todo sample group id


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //이전 액티비티(LoginActivity.class)에서 넘겨준 사용자의 UID 값을 받아온다
        Intent intent = getIntent();
        UID = intent.getStringExtra(STATIC.EXTRA_UID);


        mTodo = (TextView)findViewById(R.id.main_todo);
        mNotice = (TextView)findViewById(R.id.main_notice);
        mNoticeContent = (TextView)findViewById(R.id.main_notice_content);

        //floating button
        fabSettings = (FloatingActionButton) this.findViewById(R.id.fabSetting);
        fabMission = (FloatingActionButton) this.findViewById(R.id.fabMission);
        fabReminders= (FloatingActionButton) this.findViewById(R.id.fabReminders);
        layoutFabMission = (LinearLayout) this.findViewById(R.id.layoutFabMission);
        layoutFabReminders = (LinearLayout) this.findViewById(R.id.layoutFabReminders);
        //layoutFabSettings = (LinearLayout) this.findViewById(R.id.layoutFabSettings);
        Save = (Button)this.findViewById( R.id.SaveNow );
        SaveNow = (Button)this.findViewById( R.id.SaveNow );

        delete = (Button)findViewById( R.id.delete );

        //폰트 설정
        initTypefaces();

        //여기서부터 이제 DB에서 정보 받아와서 지정하는 과정
        //Notice(Daily Reminders)를 다운로드 받고, 성공하면 mNoticeContent에 텍스트를 지정함
        downloadNoticeContent(groupID, new NoticeCallback() {
            @Override
            public void onSuccess(String data) {
                mNoticeContent.setText(data);
            }
        });

        //TodoList(Our Missions)를 다운로드 받고, 성공하면 mTodoList에 텍스트를 지정함
        final RecyclerView mTodoList = (RecyclerView) findViewById(R.id.main_todo_recyclerview);

        downloadTodoList(groupID, new DownloadTodoCallback() {
            @Override
            public void onSuccess(ArrayList<TODO> todos) {
                Adapter_Todo adapterTodo = new Adapter_Todo(getApplicationContext(), todos);
                mTodoList.setAdapter(adapterTodo);
            }

        });

        //profile picture's menu
        mProfileButton = (ImageButton) findViewById(R.id.main_profile);
        mProfileButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                //Creating the instance of PopupMenu
                PopupMenu popup = new PopupMenu(MainActivity.this, mProfileButton);
                //Inflating the Popup using xml file
                popup.getMenuInflater().inflate(R.menu.picture_menu, popup.getMenu());

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.take_picture:
                                final CharSequence[] items={"Camera","Gallery", "Cancel"};

                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
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
                                break;

                            case R.id.sign_out:
                                signOut();
                                break;

                        }

                        return true;
                    }

                });

                popup.show();//showing popup menu
            }
        });//closing the setOnClickListener method

        mCalendarButton = (ImageButton)findViewById(R.id.main_calendar);
        mCalendarButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v){
                Intent intentLoadNewActivity = new Intent(MainActivity.this, CalendarActivity.class);
                startActivity(intentLoadNewActivity);
            }
        });


        //When main Fab (Settings) is clicked, it expands if not expanded already.
        //Collapses if main FAB was open already.
        //This gives FAB (Settings) open/close behavior
        fabSettings.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fabExpanded){
                    closeSubMenusFab();
                } else {
                    openSubMenusFab();
                }
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(this);     // 여기서 this는 Activity의 this

        /*// 여기서 부터는 알림창의 속성 설정
        builder.setTitle("Promise")        // 제목 설정
                .setView(R.layout.reminder_dialog)
                .setCancelable(false)
                .setPositiveButton("확인", new DialogInterface.OnClickListener(){
                    // 확인 버튼 클릭시 설정
                    public void onClick(DialogInterface dialog, int whichButton){
                        finish();
                    }
                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener(){
                    // 취소 버튼 클릭시 설정
                    public void onClick(DialogInterface dialog, int whichButton){
                        dialog.cancel();
                    }
                });
        AlertDialog dialog = builder.create();    // 알림창 객체 생성
        dialog.show();    // 알림창 띄우기*/



        fabReminders.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                thisDialog = new Dialog(MainActivity.this);
                thisDialog.setTitle("Promise");
                thisDialog.setContentView(R.layout.reminder_dialog);
                final EditText Write = (EditText) thisDialog.findViewById(R.id.write);
                Button SaveMyName = (Button) thisDialog.findViewById(R.id.SaveNow);
                Write.setEnabled(true);
                SaveMyName.setEnabled(true);

                SaveMyName.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TextView Daily = (TextView)findViewById(R.id.main_notice_content);
                        Daily.setText(Daily.getText().toString() + Write.getText());
                        SharedPrefesSAVE(Write.getText().toString());
                        //디비 업로드
                        uploadReminderData(Write.getText().toString());
                        thisDialog.cancel();


                            String subject = "Reminder Edited!!!";
                            String body = Write.getText().toString().trim();

                            NotificationManager notif = (NotificationManager) getSystemService( Context.NOTIFICATION_SERVICE );
                            Notification notify = new Notification.Builder
                                    ( getApplicationContext() )
                                    .setContentText( body ).
                                            setContentTitle( subject )
                                    .setSmallIcon( R.drawable.ic_stat_icon ).build();

                            notify.flags |= Notification.FLAG_AUTO_CANCEL;
                            notif.notify( 0, notify );

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

        fabMission.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
               // startActivity(new Intent(StartActivity.this, Missions.class));
                thisDialog = new Dialog(MainActivity.this);
                thisDialog.setTitle("Promise");
                thisDialog.setContentView(R.layout.mission_dialog);
                final EditText Title = (EditText) thisDialog.findViewById(R.id.titleM);
                final EditText Minutes = (EditText) thisDialog.findViewById(R.id.minutes);
                final EditText Write = (EditText) thisDialog.findViewById(R.id.write);
                Button SaveMyName = (Button) thisDialog.findViewById(R.id.SaveNow);
                Title.setEnabled(true);
                SaveMyName.setEnabled(true);

                SaveMyName.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SharedPrefesSAVE(Title.getText().toString());
                        thisDialog.cancel();
                        String subject = "Mission Edited!!!";
                        String body = Title.getText().toString().trim();

                        NotificationManager notif = (NotificationManager) getSystemService( Context.NOTIFICATION_SERVICE );
                        Notification notify = new Notification.Builder
                                ( getApplicationContext() )
                                .setContentText( body ).
                                        setContentTitle( subject )
                                .setSmallIcon( R.drawable.ic_stat_icon ).build();

                        notify.flags |= Notification.FLAG_AUTO_CANCEL;
                        notif.notify( 0, notify );


                        TODO todo = new TODO();
                        todo.setTitle(Title.getText().toString());
                        todo.setContent(Write.getText().toString());

                        String minute = Minutes.getText().toString();
                        todo.setRequiredTime(Integer.parseInt(minute)); //여기에 숫자 안넣으면 다터짐
                        todo.setEndTime(new Date(System.currentTimeMillis() + 864000000L));
                        todo.setChecked(false);

                        uploadTodo(todo, new UploadTodoCallback() {
                            @Override
                            public void onSuccess() {
                                downloadTodoList(groupID, new DownloadTodoCallback() {
                                    @Override
                                    public void onSuccess(ArrayList<TODO> todos) {
                                        Adapter_Todo adapterTodo = new Adapter_Todo(getApplicationContext(), todos);
                                        mTodoList.setAdapter(adapterTodo);
                                    }

                                });
                            }
                        });
                    }
                });


                thisDialog.show();
            }

        });


        //Only main FAB is visible in the beginning
        closeSubMenusFab();


    }



    private interface NoticeCallback {
        void onSuccess(String data);
    }

    private interface DownloadTodoCallback {
        void onSuccess(ArrayList<TODO> todos);
    }

    private interface UploadTodoCallback {
        void onSuccess();
    }

    private void initDB() {
        db = FirebaseFirestore.getInstance();
    }

    private void downloadNoticeContent(int id, final NoticeCallback callback) {
        if (db == null) {
            initDB();
        }

        db.collection("promiseAlways")
                .whereEqualTo("groupId", id)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        ArrayList<String> titles = new ArrayList<>();

                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                Map<String, Object> data = document.getData();
                                titles.add(data.get("title").toString());
                            }
                        } else {
                            titles.add("Error getting documents: " + task.getException());
                        }

                        callback.onSuccess(getStringFromArrayListString(titles));
                    }
                });
    }

    private void downloadTodoList(int id, final DownloadTodoCallback callback) {
        if (db == null) {
            initDB();
        }

        db.collection("promiseOnce")
                .whereEqualTo("groupId", id)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        ArrayList<TODO> todos = new ArrayList<>();

                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                TODO todo = document.toObject(TODO.class);
                                todo.setDocumentId(document.getId());
                                todos.add(todo);
                            }
                        } else {
                            //titles.add("Error getting documents: " + task.getException());
                        }

                        callback.onSuccess(todos);
                    }
                });
    }

    public static void uploadTodoChange(String documentId, boolean checked) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("promiseOnce").document(documentId)
                .update("checked", checked)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully updated!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating document", e);
                    }
                });
    }

    public static void uploadTodoDelete(String documentId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("promiseOnce").document(documentId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully deleted!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error deleting document", e);
                    }
                });
    }

    private void uploadTodo(TODO todo, final UploadTodoCallback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("checked", false);
        data.put("content", todo.getContent());
        data.put("endTime", todo.getEndTime());
        data.put("groupId", 1); //todo groupId
        data.put("requiredTime", todo.getRequiredTime());
        data.put("title", todo.getTitle());

        db.collection("promiseOnce")
                .add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });
    }


    private void initTypefaces() {
        Typeface HoonTopBI = Typeface.createFromAsset(getAssets(), "fonts/HoonTop Bold italic.ttf");
        Typeface Binggrae = Typeface.createFromAsset(getAssets(), "fonts/Binggrae.ttf");
        //Typeface BebasNeue = Typeface.createFromAsset(getAssets(), "fonts/BebasNeue.otf");
        //Typeface HoonSaemaulundongR = Typeface.createFromAsset(getAssets(), "fonts/HoonSaemaulundongR.ttf");
        //Typeface BinggraeB = Typeface.createFromAsset(getAssets(), "fonts/Binggrae-Bold.ttf");

        mTodo.setTypeface(HoonTopBI);
        mNotice.setTypeface(HoonTopBI);
        mNoticeContent.setTypeface(Binggrae);
    }


    private String getStringFromArrayListString(ArrayList<String> strings) {
        String result = "";
        for (String string : strings) {
            result += string + "\n";
        }

        return result;
    }

    private void signOut(){
        AuthUI.getInstance().signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        startActivity(new Intent(MainActivity.this, LoginActivity_Deprecated.class));
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
        if (resultCode == Activity.RESULT_OK){

            if (requestCode == REQUEST_CAMERA){

                Bundle bundle = data.getExtras();
                final Bitmap bmp = (Bitmap) bundle.get("data");
                mProfileButton.setImageBitmap(bmp);
            }
            else if (requestCode == SELECT_FILE){
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
    }

    private void uploadReminderData(String title) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> promiseAlways = new HashMap<>();
        promiseAlways.put("groupId", 1); //todo groupId
        promiseAlways.put("title", title);

        db.collection("promiseAlways")
                .add(promiseAlways)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                        Toast.makeText(getApplicationContext(), "success", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                        Toast.makeText(getApplicationContext(), "fail", Toast.LENGTH_SHORT).show();
                    }
                });

        Calendar calendar = Calendar.getInstance();

        calendar.set( Calendar.HOUR_OF_DAY, 17 );
        calendar.set( Calendar.MINUTE,  18);
        calendar.set( Calendar.SECOND, 00 );

        Intent intent = new Intent( getApplicationContext(), MainActivity.class );
        PendingIntent pendingIntent = PendingIntent.getBroadcast( getApplicationContext(), 100, intent, PendingIntent.FLAG_UPDATE_CURRENT );
        AlarmManager alarmManager = (AlarmManager) getSystemService( ALARM_SERVICE );
        alarmManager.setRepeating( AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent );


    }



}
