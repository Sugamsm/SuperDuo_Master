package barqsoft.footballscores;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

/**
 * Created by HP on 06-03-2016.
 */
public class WidgetIntentService extends IntentService {

    public WidgetIntentService() {
        super("WidgetIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);

        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, TodayWidgetProvider.class));

        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        Log.v("Date Found ", String.valueOf(format.format(date)));

        Uri fixs = DatabaseContract.scores_table.buildScoreWithDate();
        Cursor cursor = getContentResolver().query(fixs,
                null,
                null,
                new String[]{format.format(date)},
                DatabaseContract.scores_table.TIME_COL + " ASC");

        if (cursor == null) {
            return;
        }
        if (!cursor.moveToFirst()) {
            cursor.close();
            return;
        }

        String home = cursor.getString(cursor
                .getColumnIndex(DatabaseContract.scores_table.HOME_COL));
        String away = cursor.getString(cursor
                .getColumnIndex(DatabaseContract.scores_table.AWAY_COL));
        String time = cursor.getString(
                cursor.getColumnIndex(DatabaseContract.scores_table.TIME_COL));
        String home_goals = cursor.getString(
                cursor.getColumnIndex(DatabaseContract.scores_table.HOME_GOALS_COL)
        );

        String away_goals = cursor.getString(
                cursor.getColumnIndex(DatabaseContract.scores_table.AWAY_GOALS_COL)
        );


        Log.v("Home Team Name ", home);

        for (int appWidgetId : appWidgetIds) {
            int layoutId = R.layout.widget_fixs;
            RemoteViews views = new RemoteViews(getPackageName(), layoutId);

            views.setTextViewText(R.id.home_name, home);
            views.setTextViewText(R.id.away_name, away);
            views.setTextViewText(R.id.score_textview, Utilies.getScores(home_goals, away_goals));
            views.setTextViewText(R.id.data_textview, time);

            LoadImage(cursor.getString(cursor.getColumnIndex(DatabaseContract.scores_table.HOME_LOGO_COL)), views, R.id.home_crest);
            LoadImage(cursor.getString(cursor.getColumnIndex(DatabaseContract.scores_table.AWAY_LOGO_COL)), views, R.id.away_crest);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                setRemoteContentDescription(views, R.id.home_crest, home);
                setRemoteContentDescription(views, R.id.away_crest, away);
            }

            Intent launchIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
            views.setOnClickPendingIntent(R.id.widget, pendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, views);

        }
        cursor.close();
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    private void setRemoteContentDescription(RemoteViews views, int crest, String desciption) {
        views.setContentDescription(crest, desciption);
    }


    private void LoadImage(String url, RemoteViews views, int id) {
        Bitmap bitmap = null;

        try {
            bitmap = Glide.with(WidgetIntentService.this)
                    .load(url)
                    .asBitmap()
                    .placeholder(R.drawable.no_icon)
                    .error(R.drawable.no_icon)
                    .into(100, 100)
                    .get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        if (bitmap != null) {
            views.setImageViewBitmap(id, bitmap);
        }
    }

}
