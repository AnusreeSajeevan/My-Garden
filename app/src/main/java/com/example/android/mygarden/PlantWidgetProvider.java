package com.example.android.mygarden;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SearchRecentSuggestionsProvider;
import android.widget.RemoteViews;

import com.example.android.mygarden.provider.PlantContract;
import com.example.android.mygarden.ui.MainActivity;
import com.example.android.mygarden.ui.PlantDetailActivity;

import static com.example.android.mygarden.ui.PlantDetailActivity.EXTRA_PLANT_ID;

/**
 * Implementation of App Widget functionality.
 */
public class PlantWidgetProvider extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int imgRes,
                                int appWidgetId, long plantId, boolean showWater) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.plant_widget);

        /**
         * create a pending intent to load the {@link com.example.android.mygarden.ui.PlantDetailActivity} for the plantID,
         * launch {@link MainActivity} for invalid plant id
         */
        Intent intent;
        if (plantId == PlantContract.INVALID_PLANT_ID){
            intent = new Intent(context, MainActivity.class);
        }
        else
        {
            intent = new Intent(context, PlantDetailActivity.class);
            intent.putExtra(EXTRA_PLANT_ID, plantId);
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        //update widget image
        views.setImageViewResource(R.id.widget_plant_image, imgRes);
        // Widgets allow click handlers to only launch pending intents
        views.setOnClickPendingIntent(R.id.widget_plant_image, pendingIntent);

        views.setTextViewText(R.id.widget_plant_name, String.valueOf(plantId));


        Intent intent1 = new Intent(context, PlantWateringService.class);
        intent1.setAction(PlantWateringService.ACTION_WATER_PLANT);
        PendingIntent pendingIntent1 = PendingIntent.getService(context, 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widget_water_plant, pendingIntent1);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        //Start the intent service update widget action, the service takes care of updating the widgets UI
        PlantWateringService.startActionUpdatePlantWidgets(context);

    }


    public static void updatePlantWidgets(Context context, AppWidgetManager appWidgetManager, int imgRes, int[] appWidgetIds,
                                          long plantId, boolean showWater){
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, imgRes, appWidgetId, plantId, showWater);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

