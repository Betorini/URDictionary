package com.urdictionary.data.repository

import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.urdictionary.data.DictionarySource
import com.urdictionary.data.InterpretationRow
import com.urdictionary.data.SearchMode
import com.urdictionary.data.local.DatabaseHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "URRepo"

@Singleton
class URDictionaryRepository @Inject constructor(
    private val dbHelper: DatabaseHelper
) {

    // ─── Main lookup ──────────────────────────────────────────────────────────
    //
    // Replicates PlanetaryDAO.getAllplanetary() exactly:
    //
    //   trim    = selected text lowercase, e.g. "su ju"
    //   str2    = reversed pair, e.g.  "ju su"
    //   For ruth/rutheng/symphony → uppercase both
    //
    //   SQL: SELECT * FROM '{table}' WHERE aa='{trim}' OR aa='{str2}'
    //
    //   Result format for combine/ruth/rutheng:
    //     col[1][0..2].upper + "+" + col[1][3..5].upper + "-"
    //     + col[2][0..2].upper + " " + col[2][3..]
    //
    //   Result format for symphony:
    //     col[1][0..2].upper + "/" + col[1][3..5].upper + " : " + col[2]
    //
    suspend fun query(
        selectedCodes: List<String>,   // max 2 codes, e.g. ["SU","JU"]
        source: DictionarySource
    ): List<InterpretationRow> = withContext(Dispatchers.IO) {
        if (selectedCodes.isEmpty()) return@withContext emptyList()

        val db = dbHelper.openDatabase()
        try {
            queryInternal(db, selectedCodes, source)
        } finally {
            db.close()
        }
    }

    private fun queryInternal(
        db: SQLiteDatabase,
        codes: List<String>,
        source: DictionarySource
    ): List<InterpretationRow> {
        if (codes.size == 1) {
            // Single planet: query aa = 'su su' style (like symphony SU SU)
            // OR for combine: aa = 'su su'
            val code = codes[0]
            val aaPair = if (source.isUpperCase) "$code $code" else "${code.lowercase()} ${code.lowercase()}"
            return fetchRows(db, source, aaPair, null)
        }

        // Two planets selected
        val (a, b) = codes
        // combine uses lowercase; ruth/rutheng/symphony use uppercase
        val trim: String
        val str2: String
        if (source.isUpperCase) {
            trim = "$a $b"
            str2 = "$b $a"
        } else {
            trim = "${a.lowercase()} ${b.lowercase()}"
            str2 = "${b.lowercase()} ${a.lowercase()}"
        }

        return fetchRows(db, source, trim, str2)
    }

    private fun fetchRows(
        db: SQLiteDatabase,
        source: DictionarySource,
        aa: String,
        aaReversed: String?
    ): List<InterpretationRow> {
        val results = mutableListOf<InterpretationRow>()

        val sql = if (aaReversed != null) {
            "SELECT * FROM '${source.tableName}' WHERE aa=? OR aa=?"
        } else {
            "SELECT * FROM '${source.tableName}' WHERE aa=?"
        }
        val args = if (aaReversed != null) arrayOf(aa, aaReversed) else arrayOf(aa)

        Log.d(TAG, "SQL: $sql  args=${args.toList()}")

        try {
            val cursor = db.rawQuery(sql, args)
            cursor.use { c ->
                c.moveToFirst()
                while (!c.isAfterLast) {
                    val col1 = c.getString(1) ?: ""
                    val col2 = c.getString(2) ?: ""

                    if (col1.length >= 5 && col2.length >= 3) {
                        val displayKey: String
                        val content: String

                        if (source == DictionarySource.SYMPHONY) {
                            // "SU/JU : keywords…"
                            displayKey = col1.substring(0, 2).uppercase() +
                                    "/" + col1.substring(3, 5).uppercase()
                            content = col2
                        } else {
                            // "SU+JU-XX meaning…"
                            displayKey = col1.substring(0, 2).uppercase() +
                                    "+" + col1.substring(3, 5).uppercase() +
                                    "-" + col2.substring(0, 2).uppercase()
                            content = col2.substring(3)
                        }

                        if (content.isNotBlank()) {
                            results.add(InterpretationRow(displayKey, content.trim(), source))
                        }
                    }
                    c.moveToNext()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Query error: ${e.message}")
        }

        Log.d(TAG, "Returned ${results.size} rows from ${source.tableName}")
        return results
    }

    // ─── Keyword search (Dicsearch) ───────────────────────────────────────────
    //
    // Replicates Dicsearch.Search.getSearch() exactly.
    // Supports up to 3 space-separated keywords with AND/OR.
    //
    suspend fun keywordSearch(
        query: String,
        source: DictionarySource,
        mode: SearchMode
    ): List<InterpretationRow> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()

        val db = dbHelper.openDatabase()
        try {
            val words = query.trim().split(" ").filter { it.isNotBlank() }.take(3)
            val table = source.tableName
            val modeStr = if (mode == SearchMode.OR) "OR" else "AND"

            val sql = when (words.size) {
                1 -> "SELECT * FROM '$table' WHERE bb LIKE ?"
                2 -> "SELECT * FROM '$table' WHERE bb LIKE ? $modeStr bb LIKE ?"
                else -> "SELECT * FROM '$table' WHERE bb LIKE ? $modeStr bb LIKE ? $modeStr bb LIKE ?"
            }
            val args = words.map { "%$it%" }.toTypedArray()

            val results = mutableListOf<InterpretationRow>()
            val cursor = db.rawQuery(sql, args)
            cursor.use { c ->
                c.moveToFirst()
                while (!c.isAfterLast) {
                    val col1 = c.getString(1) ?: ""
                    val col2 = c.getString(2) ?: ""
                    if (col1.length >= 5 && col2.length >= 3) {
                        val displayKey: String
                        val content: String
                        if (source == DictionarySource.SYMPHONY) {
                            displayKey = col1.substring(0, 2).uppercase() + "/" + col1.substring(3, 5).uppercase()
                            content = col2
                        } else {
                            displayKey = col1.substring(0, 2).uppercase() + "+" +
                                    col1.substring(3, 5).uppercase() + "-" +
                                    col2.substring(0, 2).uppercase()
                            content = col2.substring(3)
                        }
                        if (content.isNotBlank()) results.add(InterpretationRow(displayKey, content.trim(), source))
                    }
                    c.moveToNext()
                }
            }
            results
        } finally {
            db.close()
        }
    }

    // ─── Favorites ────────────────────────────────────────────────────────────

    suspend fun getFavorites(): List<FavoriteRow> = withContext(Dispatchers.IO) {
        val db = dbHelper.openDatabase()
        try {
            val results = mutableListOf<FavoriteRow>()
            val c = db.rawQuery("SELECT * FROM favorite", null)
            c.use {
                it.moveToFirst()
                while (!it.isAfterLast) {
                    val sourceTag = it.getString(1) ?: "1"
                    val content = it.getString(2) ?: ""
                    results.add(FavoriteRow(sourceTag, content))
                    it.moveToNext()
                }
            }
            results
        } finally {
            db.close()
        }
    }

    suspend fun saveFavorite(sourceTag: String, content: String) = withContext(Dispatchers.IO) {
        // Use writable DB for mutations
        val db = dbHelper.openDatabase()
        try {
            db.execSQL("INSERT INTO favorite (aa, bb) VALUES (?, ?)", arrayOf(sourceTag, content))
        } finally {
            db.close()
        }
    }

    suspend fun deleteFavorite(content: String) = withContext(Dispatchers.IO) {
        val db = dbHelper.openDatabase()
        try {
            db.execSQL("DELETE FROM favorite WHERE bb=?", arrayOf(content))
        } finally {
            db.close()
        }
    }

    suspend fun deleteAllFavorites() = withContext(Dispatchers.IO) {
        val db = dbHelper.openDatabase()
        try {
            db.execSQL("DELETE FROM favorite")
        } finally {
            db.close()
        }
    }
}

data class FavoriteRow(
    val sourceTag: String,   // "1" = combine, "2" = rutheng, "3" = symphony
    val content: String
)
