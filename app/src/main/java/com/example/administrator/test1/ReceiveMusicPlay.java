package com.example.administrator.test1;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ResultReceiver;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class ReceiveMusicPlay extends AppCompatActivity {
    Button play;
    File file;
    SeekBar seekbar;
    ImageView albumart;
    MediaPlayer music = new MediaPlayer();
    private BackPressCloseHandler backPressCloseHandler;
    ResultReceiver serverReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.receivemusic_play);
        backPressCloseHandler = new BackPressCloseHandler(this);
        play = (Button) findViewById(R.id.b);
        play.setEnabled(false);
        play.setVisibility(View.INVISIBLE);
        //broadcastReceiver.

        albumart = (ImageView)findViewById(R.id.album);

        seekbar = (SeekBar) findViewById(R.id.seekBar1);
        file = new File("/storage/emulated/0/Download/a.mp3");
       // albumart.setImageBitmap(getCircleBitmap(getAlbumArt(getApplicationContext(),file)));
        String a = file.getAbsolutePath();
        setFilename(a);
        music = MediaPlayer.create(getApplicationContext(),Uri.parse(a));

        play.callOnClick();

        seekbar.setMax(music.getDuration());
        Thread();




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

                }
            }
        };
        Thread thread = new Thread(task);
        thread.start();
    }
    public void setFilename(String file_name){
        TextView tx = (TextView)findViewById(R.id.Filename);;
        String fileName = new File(file_name).getName();
        tx.setText("공유받고 있는 파일" + "\n" + "\n"+ fileName);
    }
    public void b (View v) {
        if (music.isPlaying()) {
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
            seekbar.setProgress(music.getCurrentPosition());
        } else {
// 재생중이 아니면 실행될 작업 (재생)

            music.start();

        }
    }
    @Override
    public void onBackPressed() {
        backPressCloseHandler.onBackPressed();
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
    public void exit(){
        finish();
    }


}
