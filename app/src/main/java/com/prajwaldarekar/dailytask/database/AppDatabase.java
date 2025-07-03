package com.prajwaldarekar.dailytask.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.prajwaldarekar.dailytask.models.Task;
import com.prajwaldarekar.dailytask.models.TaskCompletion; // ✅ Add this import
import com.prajwaldarekar.dailytask.utils.Converters;

@Database(
        entities = {
                Task.class,
                TaskCompletion.class // ✅ Include TaskCompletion entity
        },
        version = 5, // ✅ Increment version to trigger Room migration
        exportSchema = false
)
@TypeConverters(Converters.class)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract TaskDao taskDao();
    public abstract TaskCompletionDao taskCompletionDao(); // ✅ Add DAO method

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "daily_task_db"
                            )
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
