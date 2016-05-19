package com.example.administrator.test1;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.ResultReceiver;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016-05-12.
 */
public class MainActivity extends Activity{
    Intent intent;
    public Button btnFindpeer;  //피어찾기 버튼
    Button[] buttons; //찾은 피어들의 버튼
    Button button1,button2,button3; // 뒤로가기 재생 앞으로가기 버튼
    SeekBar seekbar; // 시크바
    MediaPlayer music = null; // 현재 재생되는 MediaPlayer
    ListView lvFileControl; // 공유할 음악 리스트
    ImageView mimage; // 앨범이미지 출력할 이미지뷰
    Drawable alpha;
    int Marg,Lleng=0;



    private BackPressCloseHandler backPressCloseHandler; // Back키 눌렀을때 처리하는 핸들러
    private List mFileList = new ArrayList(); // List뷰에서 음악파일 리스트
    private List mList = new ArrayList();    //   위와 같음.
    private File Musicfolder1 = new File(Environment.getExternalStorageDirectory() + "/Music","");  // 뮤직폴더에서 찾기위해
    private File Musicfolder2 = new File(Environment.getExternalStorageDirectory() + "/Download",""); // 다운로드폴더에서 찾기위해
    private static final String[] FTYPE = {"mp3","wav"}; // 찾는타입 (.mp3 , .wav)형식 찾음
    private static String file_nm = null;  //음악파일의 uri를 string으로 받음

    private boolean connectedAndReadyToSendFile;

    File sendtofile;


    Intent clientServiceIntent;
    Intent serverServiceIntent;


    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private IntentFilter wifiP2pIntentFilter;
    private WifiBroadcastReceiver wifiBroadcastReceiver;
    protected WifiP2pDeviceList wifiP2pDeviceList;


    //private WifiP2pDevice targetDevice;
    private WifiP2pInfo wifiInfo;
    private int currentpos = 100;
    private int port = 1111;
    private boolean firstclientmusicstart;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i("TAG","메인엑티비티 시작");
        btnFindpeer = (Button)findViewById(R.id.btnFindpeer);

        buttons = new Button[3];
        buttons[0] = (Button)findViewById(R.id.btnDevice1);
        buttons[1] = (Button)findViewById(R.id.btnDevice2);
        buttons[2] = (Button)findViewById(R.id.btnDevice3);

        Toast.makeText(this,"공유할 음악을 선택해 주세요.",Toast.LENGTH_SHORT).show();
        backPressCloseHandler = new BackPressCloseHandler(this);
        button1 = (Button) findViewById(R.id.button1);
        button2 = (Button) findViewById(R.id.button2);
        button3 = (Button) findViewById(R.id.button3);
        seekbar = (SeekBar) findViewById(R.id.seekBar1);
        mimage = (ImageView) findViewById(R.id.Mimage);
        //alpha = ((ImageView)findViewById(R.id.share)).getBackground();
        //alpha.setAlpha(55);

        button1.setEnabled(false);
        button2.setEnabled(false);
        button3.setEnabled(false);
        seekbar.setEnabled(false);
        mimage.setVisibility(View.INVISIBLE);


        lvFileControl = (ListView)findViewById(R.id.lvFileControl);
        firstclientmusicstart = true;


        mFileList.clear();
        loadAllAudioList(Musicfolder1);// music
        loadAllAudioList(Musicfolder2);// download

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, mFileList);
        lvFileControl.setAdapter(adapter);
        lvFileControl.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

                Lleng = mList.size();
                file_nm = (String) mList.get(arg2);
                Marg = arg2;
                setFilename(file_nm);
                mediaCreate();
                File file = new File(file_nm);
                //mimage.setImageBitmap(getCircleBitmap(getAlbumArt(getApplicationContext(), file)));
                sendtofile = file;
                sendfile();


                button1.setEnabled(true);
                button2.setEnabled(true);
                button3.setEnabled(true);
                seekbar.setEnabled(true);
                button1.callOnClick();
            }
        });


        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
                sendstate();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
                sendstate();
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                // TODO Auto-generated method stub
                if (fromUser)
                    music.seekTo(progress);
                currentpos = progress;
            }
        });

        for(Button button : buttons) {
            button.setVisibility(Button.INVISIBLE);
            button.setEnabled(false);
            button.setTag("");
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!((Button) v).getTag().equals("Connected")) {
                        for (WifiP2pDevice device : wifiP2pDeviceList.getDeviceList()) {
                            WifiP2pConfig wifiP2pConfig = new WifiP2pConfig();
                            wifiP2pConfig.deviceAddress = device.deviceAddress;
                            wifiP2pConfig.groupOwnerIntent = 0;
                            manager.connect(channel, wifiP2pConfig, new WifiP2pManager.ActionListener() {
                                @Override
                                public void onSuccess() {
                                    Log.i("TAG","와이파이 다이렉트가 연결됨");
                                }

                                @Override
                                public void onFailure(int reason) {
                                    Log.i("TAG","와이파이 다이렉트 연결에 실패함");
                                }
                            });
                        }
                    }
                }

            });
        }


        wifiP2pIntentFilter = new IntentFilter();
        wifiP2pIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        wifiP2pIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        wifiP2pIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        wifiP2pIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        connectedAndReadyToSendFile = false;

        manager = (WifiP2pManager)getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);

        wifiBroadcastReceiver = new WifiBroadcastReceiver(manager, channel, this);

        registerReceiver(wifiBroadcastReceiver, wifiP2pIntentFilter);


        btnFindpeer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manager.discoverPeers(channel,null);
                Log.i("TAG","피어찾기를 시작함");
            }
        });
    }

    //클라이언트 서비스 시작
    public void sendfile(){
        clientServiceIntent = new Intent(this, ClientService.class);
        clientServiceIntent.putExtra("port", Integer.valueOf(port));
        clientServiceIntent.putExtra("sendtofile",sendtofile);
        clientServiceIntent.putExtra("sendstate",true);
        clientServiceIntent.putExtra("musicpos", Integer.valueOf(currentpos));
        startService(clientServiceIntent);
    }

    public void sendstate(){
        clientServiceIntent = new Intent(this, ClientService.class);
        clientServiceIntent.putExtra("port", Integer.valueOf(port));
        clientServiceIntent.putExtra("state", "music state");
        clientServiceIntent.putExtra("sendstate", false);
        clientServiceIntent.putExtra("musicpos", Integer.valueOf(currentpos));
        startService(clientServiceIntent);
    }

    public void setNetworkToReadyState(boolean status, WifiP2pInfo info, WifiP2pDevice device)
    {
        Log.i("TAG","네트워크 정보가 저장됨");
        wifiInfo = info;
        //targetDevice = device;
        connectedAndReadyToSendFile = status;
        manager.stopPeerDiscovery(channel, null);
        btnFindpeer.setEnabled(false);
        buttons[0].setBackgroundColor(Color.GREEN);
        buttons[0].setTag("Connected");
        if(wifiInfo.isGroupOwner){
            Log.i("TAG","그룹 오너임");
            serverServiceIntent = new Intent(this,ServerService.class);
            serverServiceIntent.putExtra("port", Integer.valueOf(port));
            serverServiceIntent.putExtra("serverResult", new ResultReceiver(null) {
                @Override
                protected void onReceiveResult(int resultCode, final Bundle resultData) {

                    if(resultCode == port )
                    {
                        if(resultData == null) {
                            if (firstclientmusicstart) {
                                runOnUiThread(new Runnable() {

                                    public void run() {

                                        setContentView(R.layout.receivemusic_play);

                                    }

                                });
                                startmusic();
                                firstclientmusicstart = false;
                            } else {
                                startmusic();
                                Log.i("TAG", "이제 명령을 받는다");
                            }
                        }else{

                            int getpos = resultData.getInt("key");
                            setpos(getpos);
                        }

                    }

                }
            });
            startService(serverServiceIntent);// 서버 서비스 시작

        }else{
            Log.i("TAG", "그룹 오너가 아님");
        }
    }

    public void setTransferStatus(boolean status)
    {
        connectedAndReadyToSendFile = status;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            unregisterReceiver(wifiBroadcastReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    /*
    피어를 찾았을때 찾은 피어를 버튼으로 보여주는 함수
     */
    protected void displayPeerButtons(WifiP2pDeviceList peers) {
        ArrayList<String> deviceNameList = new ArrayList<String>();
        for(WifiP2pDevice device : peers.getDeviceList()) {
            deviceNameList.add(device.deviceName);
        }
        int iteration = deviceNameList.size();
        if(deviceNameList.size() <= 3) {

            for(int i = 0; i < 3; i++) {
                if(i < deviceNameList.size()) {
                    buttons[i].setEnabled(true);
                    buttons[i].setVisibility(Button.VISIBLE);
                    buttons[i].setText(deviceNameList.get(i));
                } else {
                    buttons[iteration].setEnabled(false);
                    buttons[iteration].setVisibility(Button.INVISIBLE);
                }
            }
        } else {
        }
    }

    @Override
    public void onBackPressed() {
        backPressCloseHandler.onBackPressed();
    }

    public void button1(View v){
        if(music.isPlaying()){
// 재생중이면 실행될 작업 (일시 정지)
            music.pause();
            try {
                music.prepare();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            music.getCurrentPosition();

            button1.setBackgroundResource(R.drawable.play);
            seekbar.setProgress(music.getCurrentPosition());
        }else {
// 재생중이 아니면 실행될 작업 (재생)

            music.start();

            mimage.setVisibility(View.VISIBLE);
            button1.setBackgroundResource(R.drawable.stop);

            Thread();


        }

    }

    public void button2(View v) {
        if(Marg==0){
            Marg=Lleng;
        }
        music.stop();
        Marg = Marg - 1;
        music = MediaPlayer.create(getApplicationContext(), Uri.parse((String) mList.get(Marg)));
        File file = new File((String)mList.get(Marg));
        sendtofile = file;
        sendfile();
      //  mimage.setImageBitmap(getCircleBitmap(getAlbumArt(getApplicationContext(), file)));
        seekbar.setMax(music.getDuration());
        button1.callOnClick();
        setFilename((String) mList.get(Marg));
    }

    public void button3(View v){
        if(Marg==Lleng-1){
            Marg=-1;
        }
        music.stop();
        Marg = Marg + 1;
        music = MediaPlayer.create(getApplicationContext(), Uri.parse((String) mList.get(Marg)));
        File file = new File((String)mList.get(Marg));
        sendtofile = file;
        sendfile();
        //mimage.setImageBitmap(getCircleBitmap(getAlbumArt(getApplicationContext(), file)));
        seekbar.setMax(music.getDuration());
        button1.callOnClick();
        setFilename((String) mList.get(Marg));
    }

    public void Thread(){
        Runnable task = new Runnable(){
            public void run(){
                /**
                 * while문을 돌려서 음악이 실행중일때 게속 돌아가게
                 */
                while(music.isPlaying()){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    /**
                     * music.getCurrentPosition()은 현재 음악 재생 위치를 가져오는 구문
                     */
                    seekbar.setProgress(music.getCurrentPosition());

                    music.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                        public void onCompletion(MediaPlayer mp) {
                            if(Marg==Lleng-1){
                                Marg=-1;
                            }
                            Marg = Marg + 1;
                            music = MediaPlayer.create(getApplicationContext(), Uri.parse((String) mList.get(Marg)));
                            File file = new File((String)mList.get(Marg));
                            //mimage.setImageBitmap(getCircleBitmap(getAlbumArt(getApplicationContext(),file)));
                            seekbar.setMax(music.getDuration());
                            button1.callOnClick();
                            setFilename((String) mList.get(Marg));

                        }
                    });
                }
            }
        };
        Thread thread = new Thread(task);
        thread.start();
    }


    private void loadAllAudioList(File file){
        if (file != null && file.isDirectory())
        {
            File[] children = file.listFiles();
            if (children != null)
            {

                for(int i = 0; i < children.length; i++)
                {
                    if (children[i] != null)
                    {
                        for(int j=0;j<FTYPE.length;j++){
                            if(FTYPE[j].equals(children[i].getName().substring(children[i].getName().lastIndexOf(".")+1,
                                    children[i].getName().length()))){
                                mFileList.add(children[i].getName());
                                mList.add(children[i].getAbsolutePath());

                            }
                        }
                    }
                    loadAllAudioList(children[i]);
                }
            }
        }
    }


    public void setFilename(String file_name){
        TextView tx = (TextView)findViewById(R.id.tvPath);
        //String path = file_name;
        String fileName = new File(file_name).getName();
        tx.setText(fileName);
    }


    public void mediaCreate() {
        if(music != null) {
            music.stop();
            button1.setBackgroundResource(R.drawable.play);
        }
        music = MediaPlayer.create(getApplicationContext(),Uri.parse(file_nm));
        music.setLooping(false);

        seekbar.setMax(music.getDuration());



    }


    public class BackPressCloseHandler {
        private long backKeyPressedTime = 0;
        private Toast toast;

        private Activity activity;


        public BackPressCloseHandler(Activity context) {
            this.activity = context;
        }

        public void onBackPressed() {
            if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
                backKeyPressedTime = System.currentTimeMillis();
                showGuide();
                return;
            }
            if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
                activity.finish();
                toast.cancel();
                System.exit(0);
            }
        }

        private void showGuide() {
            toast = Toast.makeText(activity, "뒤로 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT);
            toast.show();
        }
    }
    private Bitmap getAlbumArt(Context context, File mp3File) {
        Uri ArtworkUri = Uri.parse("content://media/external/audio/albumart");
        long albumId = 0;
        String mediaPath = mp3File.getAbsolutePath();
        String projection[] = { MediaStore.Audio.Media.ALBUM_ID };
        String selection = MediaStore.Audio.Media.DATA + " LIKE ? ";
        String selectionArgs[] = { mediaPath };
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, selectionArgs, null);
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                albumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
            }
            cursor.close();
        }
        if (albumId > 0) {
            Uri albumArtUri = ContentUris.withAppendedId(ArtworkUri, albumId);
            ContentResolver res = context.getContentResolver();
            Bitmap bitmap = null;
            try {
                InputStream input = res.openInputStream(albumArtUri);
                bitmap = BitmapFactory.decodeStream(res.openInputStream(albumArtUri));
                input.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }
        return null;
    }
    public Bitmap getCircleBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        int size = (bitmap.getWidth()/2);
        canvas.drawCircle(size, size, size, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

    public void startmusic(){
        if(music!=null){
            music.stop();
            music = null;
        }
        File file = new File("/storage/emulated/0/Download/a.mp3");
        String a = file.getAbsolutePath();
        music =  MediaPlayer.create(getApplicationContext(),Uri.parse(a));

        music.setLooping(false);
        music.start();

    }

    public void setpos(int pos){        ;
        music.seekTo(pos+100);
    }

}
