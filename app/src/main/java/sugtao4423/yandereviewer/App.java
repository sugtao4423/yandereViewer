package sugtao4423.yandereviewer;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

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
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra(DownloadService.INTENT_KEY_POSTS, posts);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            startForegroundService(intent);
        }else{
            startService(intent);
        }
    }

}
