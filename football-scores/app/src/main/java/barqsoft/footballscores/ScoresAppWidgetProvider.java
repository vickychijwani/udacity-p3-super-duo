package barqsoft.footballscores;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.annotation.DrawableRes;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ScoresAppWidgetProvider extends AppWidgetProvider {

    private static final String TAG = ScoresAppWidgetProvider.class.getSimpleName();

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int i = 0, len = appWidgetIds.length; i < len; ++i) {
            int appWidgetId = appWidgetIds[i];

            Intent launchAppIntent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, launchAppIntent, 0);

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.scores_appwidget);
            views.setOnClickPendingIntent(R.id.appwidget_root, pendingIntent);

            Date yesterday = new Date(System.currentTimeMillis() - 86400000);
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Cursor cursor = context.getContentResolver().query(
                    DatabaseContract.scores_table.buildScoreWithDate(),
                    null, null, new String[] { dateFormat.format(yesterday) }, null);

            if (cursor != null) {
                try {
                    // display the last match results from yesterday, if any
                    if (cursor.moveToLast()) {
                        // home team
                        String homeTeam = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.scores_table.HOME_COL));
                        views.setTextViewText(R.id.appwidget_home_name, homeTeam);
                        @DrawableRes int homeTeamCrest = Utilies.getTeamCrestByTeamName(homeTeam);
                        views.setImageViewResource(R.id.appwidget_home_crest, homeTeamCrest);

                        // away team
                        String awayTeam = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.scores_table.AWAY_COL));
                        views.setTextViewText(R.id.appwidget_away_name, awayTeam);
                        @DrawableRes int awayTeamCrest = Utilies.getTeamCrestByTeamName(awayTeam);
                        views.setImageViewResource(R.id.appwidget_away_crest, awayTeamCrest);

                        // score
                        StringBuilder score = new StringBuilder(5);
                        score.append(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.scores_table.HOME_GOALS_COL)));
                        score.append(" - ");
                        score.append(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.scores_table.AWAY_GOALS_COL)));
                        views.setTextViewText(R.id.appwidget_score, score);

                        views.setViewVisibility(R.id.appwidget_details, View.VISIBLE);
                        views.setViewVisibility(R.id.appwidget_empty, View.GONE);
                    } else {
                        views.setViewVisibility(R.id.appwidget_details, View.GONE);
                        views.setViewVisibility(R.id.appwidget_empty, View.VISIBLE);
                    }
                } catch (Exception e) {
                    Log.e(TAG, Log.getStackTraceString(e));
                } finally {
                    cursor.close();
                }
            }

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

}
