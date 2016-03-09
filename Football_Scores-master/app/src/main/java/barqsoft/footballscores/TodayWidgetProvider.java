package barqsoft.footballscores;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by HP on 06-03-2016.
 */
public class TodayWidgetProvider extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        // Log.v("Widget Service Called ", "On UPDATE");
        context.startService(new Intent(context, WidgetIntentService.class));
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(ScoresProvider.BROADCAST_INTENT.equals(intent.getAction())){
           // Log.v("Widget Service Called ", "On RECEIVE");
            context.startService(new Intent(context, WidgetIntentService.class));
        }
        super.onReceive(context, intent);
    }
}
