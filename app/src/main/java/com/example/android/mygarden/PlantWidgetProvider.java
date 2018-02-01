package com.example.android.mygarden;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SearchRecentSuggestionsProvider;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.example.android.mygarden.provider.PlantContract;
import com.example.android.mygarden.ui.MainActivity;
import com.example.android.mygarden.ui.PlantDetailActivity;

import static com.example.android.mygarden.ui.PlantDetailActivity.EXTRA_PLANT_ID;

/**
 * Implementation of App Widget functionality.
 */
public class PlantWidgetProvider extends AppWidgetProvider {

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int imgRes,
                                int appWidgetId, long plantId, boolean showWater) {
        Log.d("CheckResizeee","updatePlantWidgets");

        //get the width to decide on single plant view and garden grid view
        Bundle bundle = appWidgetManager.getAppWidgetOptions(appWidgetId);

        //get width
        int width = bundle.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);

        RemoteViews views;
        if (width < 300)
            views = getSinglePlantRemoteView(context, imgRes, plantId, showWater);
        else
            views = getGardenGridRemoteView(context);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private static RemoteViews getSinglePlantRemoteView(Context context, int imgRes, long plantId, boolean showWater) {
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
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        //update widget image
        views.setImageViewResource(R.id.widget_plant_image, imgRes);
        // Widgets allow click handlers to only launch pending intents
        views.setOnClickPendingIntent(R.id.widget_plant_image, pendingIntent);

        views.setTextViewText(R.id.widget_plant_name, String.valueOf(plantId));

        // Show/Hide the water drop button
        if (showWater) views.setViewVisibility(R.id.widget_water_plant, View.VISIBLE);
        else views.setViewVisibility(R.id.widget_water_plant, View.GONE);

        Intent intent1 = new Intent(context, PlantWateringService.class);
        intent1.setAction(PlantWateringService.ACTION_WATER_PLANT);
        // Add the plant ID as extra to water only that plant when clicked
        intent1.putExtra(PlantWateringService.EXTRA_PLANT_ID, plantId);
        PendingIntent pendingIntent1 = PendingIntent.getService(context, 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widget_water_plant, pendingIntent1);

        return views;
    }

    private static RemoteViews getGardenGridRemoteView(Context context) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_grid_view);

        /**
         * set {@link GridWidgetService} to act as the adapter for the gridview
         */
        Intent intentAdapter = new Intent(context, GridWidgetService.class);
        views.setRemoteAdapter(R.id.grid_view_widget, intentAdapter);

        /**
         * set {@link PlantDetailActivity} to be launched on click
         */
        Intent intent = new Intent(context, PlantDetailActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setPendingIntentTemplate(R.id.grid_view_widget, pendingIntent);

        //Handle empty views
        views.setEmptyView(R.id.grid_view_widget, R.id.layout_empty);

        return views;
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        //Start the intent service update widget action, the service takes care of updating the widgets UI
        PlantWateringService.startActionUpdatePlantWidgets(context);

    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
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

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        PlantWateringService.startActionUpdatePlantWidgets(context);
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }
}

