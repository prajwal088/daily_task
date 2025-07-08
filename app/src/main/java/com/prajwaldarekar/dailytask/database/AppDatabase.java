package com.prajwaldarekar.dailytask.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.prajwaldarekar.dailytask.models.Task;
import com.prajwaldarekar.dailytask.models.TaskCompletion;
import com.prajwaldarekar.dailytask.utils.Converters;

@Database(
        entities = {
                Task.class,
                TaskCompletion.class
        },
        version = 6, // ✅ Current DB version
        exportSchema = false
)
@TypeConverters(Converters.class)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract TaskDao taskDao();
    public abstract TaskCompletionDao taskCompletionDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "daily_task_db"
                            )
                            .addMigrations(MIGRATION_5_6) // ✅ Add migration here
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * ✅ Migration from version 4 to 5:
     * Adds `completed_at` column to `task_completion` table.
     */
    private static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Adds completed_at column with type INTEGER (nullable)
            database.execSQL("ALTER TABLE task_completion ADD COLUMN completed_at INTEGER");
        }
    };
}
