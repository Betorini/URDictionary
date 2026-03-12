package com.urdictionary.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import java.io.File
import java.io.FileOutputStream

private const val TAG = "DatabaseHelper"
private const val DB_NAME = "planetary.sqlite"

/**
 * Manages the pre-built SQLite database shipped as res/raw/planetary.sqlite.
 * On first launch it copies the file to the app's database directory.
 * Subsequent launches detect the file exists and skip copying.
 */
class DatabaseHelper(private val context: Context) {

    private val dbPath: String
        get() = context.getDatabasePath(DB_NAME).absolutePath

    fun openDatabase(): SQLiteDatabase {
        ensureDatabaseCopied()
        return SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY)
    }

    private fun ensureDatabaseCopied() {
        val dbFile = File(dbPath)
        if (dbFile.exists()) {
            Log.d(TAG, "Database already exists at $dbPath")
            return
        }
        Log.d(TAG, "Copying database from raw resources…")
        dbFile.parentFile?.mkdirs()
        try {
            context.resources.openRawResource(R.raw.planetary).use { input ->
                FileOutputStream(dbFile).use { output ->
                    input.copyTo(output, bufferSize = 8192)
                    output.flush()
                }
            }
            Log.d(TAG, "Database copied successfully (${dbFile.length() / 1024} KB)")
        } catch (e: Exception) {
            dbFile.delete()
            throw RuntimeException("Failed to copy database from raw resources", e)
        }
    }

    /** Replace the database (e.g. after an app update ships a new DB version). */
    fun replaceDatabase() {
        File(dbPath).delete()
        ensureDatabaseCopied()
    }
}
