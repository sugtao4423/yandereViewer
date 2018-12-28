package sugtao4423.yandereviewer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import yandere4j.data.Post;

public class RequestPermissionActivity extends AppCompatActivity{

    public static final String INTENT_EXTRA_GRANTED_SAVE_POSTS = "grantedSavePosts";

    private static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 810;
    private Post[] savePosts;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        savePosts = (Post[])getIntent().getSerializableExtra(INTENT_EXTRA_GRANTED_SAVE_POSTS);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_WRITE_EXTERNAL_STORAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode != REQUEST_CODE_WRITE_EXTERNAL_STORAGE){
            return;
        }
        if(permissions[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE) && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            ((App)getApplicationContext()).saveImages(this, savePosts);
        }else{
            Toast.makeText(getApplicationContext(), R.string.permission_rejected, Toast.LENGTH_LONG).show();
        }
        finish();
    }
}
