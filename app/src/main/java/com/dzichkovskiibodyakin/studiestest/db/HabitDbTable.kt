package com.dzichkovskiibodyakin.studiestest.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.dzichkovskiibodyakin.studiestest.Habit
import java.io.ByteArrayOutputStream

class HabitDbTable(context: Context){

    private val TAG = HabitDbTable::class.java.simpleName

    private val dbHelper = HabitTrainerDb(context)

    fun store(habit: Habit): Long{
        val db = dbHelper.writableDatabase

        val values = ContentValues()
        with(values) {
            with(HabitEntry) {
                put(TITLE_COL, habit.title)
                put(DESCR_COL, habit.description)
                put(IMAGE_COL, toByteArray(habit.image))
            }
        }
        val id = db.transaction {
            insert(HabitEntry.TABLE_NAME, null, values)
        }

        Log.d(TAG, "Stored new habit to the DB $habit")

        return id
    }

    fun readAllHabits(): List<Habit> {

        with(HabitEntry) {
            val columns = arrayOf(_ID, TITLE_COL, DESCR_COL, IMAGE_COL)

            val db = dbHelper.readableDatabase

            val cursor = db.doQuery(TABLE_NAME, columns)

            return parseHabitFromCursor(cursor)
        }
    }

    private fun parseHabitFromCursor(cursor: Cursor): MutableList<Habit> {
        val habits = mutableListOf<Habit>()
        while (cursor.moveToNext()) {
            with(HabitEntry) {
                val title = cursor.getString(TITLE_COL)
                val desc = cursor.getString(DESCR_COL)
                val bitmap = cursor.getBitmap(IMAGE_COL)
                habits.add(Habit(title, desc, bitmap))
            }
        }
        cursor.close()
        return habits
    }

    private fun toByteArray(bitmap: Bitmap): ByteArray{
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream)
        return stream.toByteArray()
    }
}

private fun SQLiteDatabase.doQuery(table: String, columns: Array<String>, selection: String? = null,
                                   selectionArgs: Array<String>? = null, groupBy: String? = null,
                                   having: String? = null, orderBy: String? = null): Cursor {

    return query(table, columns, selection, selectionArgs, groupBy, having, orderBy)
}

private fun Cursor.getString(columnIndex: String) = getString(getColumnIndex(columnIndex))
private fun Cursor.getBitmap(columnIndex: String): Bitmap {
    val bytes = getBlob(getColumnIndex(columnIndex))
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}

private inline fun <T> SQLiteDatabase.transaction(function: SQLiteDatabase.() -> T): T {
    beginTransaction()
    val result = try{
        val returnValue = function()
        setTransactionSuccessful()

        returnValue
    } finally {
        endTransaction()
    }
    close()

    return result
}