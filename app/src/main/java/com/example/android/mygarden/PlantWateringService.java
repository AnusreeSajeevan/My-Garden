package com.example.android.mygarden;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.example.android.mygarden.provider.PlantContract;
import com.example.android.mygarden.utils.PlantUtils;

import static com.example.android.mygarden.provider.PlantContract.BASE_CONTENT_URI;
import static com.example.android.mygarden.provider.PlantContract.PATH_PLANTS;

/**
 * An {@link IntentService} sub class is used to
 * handle an asynchrounus task in a service on a seperate handler thread
 */
public class PlantWateringService extends IntentService {

    /**
     * To keep things organized, it’s best to define the actions that the IntentService can handle
     */
    public static final String ACTION_WATER_PLANTS = "com.example.android.mygarden.action.water_plants";
    public static final String ACTION_UPDATE_PLANT_WIDGETS = "com.example.android.mygarden.action.update_plant_widgets";

    public PlantWateringService() {
        super("PlantWateringService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null){
            if (intent.getAction().equalsIgnoreCase(ACTION_WATER_PLANTS)){
                handleActionWaterPlants();
            }else if (ACTION_UPDATE_PLANT_WIDGETS.equals(intent.getAction())) {
                handleActionUpdatePlantWidgets();
            }
        }
    }


    /**
     * Handle action UpdatePlantWidgets in the provided background thread
     */
    private void handleActionUpdatePlantWidgets() {
        //Query to get the plant that's most in need for water (last watered)
        Uri PLANT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_PLANTS).build();
        Cursor cursor = getContentResolver().query(
                PLANT_URI,
                null,
                null,
                null,
                PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME
        );
        // Extract the plant details
        int imgRes = R.drawable.grass; // Default image in case our garden is empty
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            int createTimeIndex = cursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_CREATION_TIME);
            int waterTimeIndex = cursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME);
            int plantTypeIndex = cursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_PLANT_TYPE);
            long timeNow = System.currentTimeMillis();
            long wateredAt = cursor.getLong(waterTimeIndex);
            long createdAt = cursor.getLong(createTimeIndex);
            int plantType = cursor.getInt(plantTypeIndex);
            cursor.close();
            imgRes = PlantUtils.getPlantImageRes(this, timeNow - createdAt, timeNow - wateredAt, plantType);
        }
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, PlantWidgetProvider.class));
        //Now update all widgets
        PlantWidgetProvider.updatePlantWidgets(this, appWidgetManager, imgRes, appWidgetIds);
    }

    /**
     *  To water all plants we just run an update query setting the last watered time to now, but only for those plants that are still alive. To check if a plant is still alive, you can compare the last watered time with the time now and if the difference is larger than MAX_AGE_WITHOUT_WATER, then the plant is dead!
     */
    private void handleActionWaterPlants() {

        Uri PLANTS_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_PLANTS).build();
        ContentValues contentValues = new ContentValues();
        long timeNow = System.currentTimeMillis();
        contentValues.put(PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME, timeNow);
        // Update only plants that are still alive
        getContentResolver().update(
                PLANTS_URI,
                contentValues,
                PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME+">?",
                new String[]{String.valueOf(timeNow - PlantUtils.MAX_AGE_WITHOUT_WATER)});
    }

    /**
     * allows explicitly triggering the Service to perform this action, inside simply create an intent
     * that refers to the same class and set the action to ACTION_WATER_PLANTS and call start service
     * @param context
     */
    public static void setActionWaterPlants(Context context){
        Intent intent = new Intent(context, PlantWateringService.class);
        intent.setAction(ACTION_WATER_PLANTS);
        context.startService(intent);
    }

    /**
     * Starts this service to perform UpdatePlantWidgets action with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionUpdatePlantWidgets(Context context) {
        Intent intent = new Intent(context, PlantWateringService.class);
        intent.setAction(ACTION_UPDATE_PLANT_WIDGETS);
        context.startService(intent);
    }
}
