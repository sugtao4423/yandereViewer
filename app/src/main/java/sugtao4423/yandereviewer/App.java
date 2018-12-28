package sugtao4423.yandereviewer;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.content.PermissionChecker;

import yandere4j.data.Post;

public class App extends Application{

    private boolean clearedHistory = false;
    private boolean isRefreshTags = false;

    public void setClearedHistory(boolean clearedHistory){
        this.clearedHistory = clearedHistory;
    }

    public boolean getClearedHistory(){
        return clearedHistory;
    }

    public void setIsRefreshTags(boolean isRefreshTags){
        this.isRefreshTags = isRefreshTags;
    }

    public boolean getIsRefreshTags(){
        return isRefreshTags;
    }

    public void saveImage(Context context, Post post){
        Post[] posts = new Post[]{post};
        saveImages(context, posts);
    }

    public void saveImages(Context context, Post[] posts){
        int writeExternalStorage = PermissionChecker.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(writeExternalStorage != PackageManager.PERMISSION_GRANTED){
            Intent intent = new Intent(context, RequestPermissionActivity.class);
            intent.putExtra(RequestPermissionActivity.INTENT_EXTRA_GRANTED_SAVE_POSTS, posts);
            context.startActivity(intent);
            return;
        }

        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra(DownloadService.INTENT_KEY_POSTS, posts);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            startForegroundService(intent);
        }else{
            startService(intent);
        }
    }

}
