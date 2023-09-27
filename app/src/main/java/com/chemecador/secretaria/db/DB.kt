package com.chemecador.secretaria.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import com.chemecador.secretaria.items.Friend
import com.chemecador.secretaria.items.Note
import com.chemecador.secretaria.items.NotesList
import com.chemecador.secretaria.items.Task
import com.chemecador.secretaria.logger.Logger
import com.chemecador.secretaria.utils.PreferencesHandler
import java.lang.ref.WeakReference
import java.time.LocalDateTime
import java.time.ZoneOffset

/**
 * Información importante acerca de la base de datos:
 *
 *
 *
 *
 * Task:
 * status: 0 -> sin terminar (default)
 * 1 -> terminada
 *
 *
 * List:    type: 0 -> lista normal (default)
 * 1 -> check list/ to do list
 *
 *
 * Note:    status: 0 -> lista normal (default)
 * 1 -> check list no terminada
 * 2 -> check list terminada
 */
class DB private constructor(context: Context, databaseName: String) :
    SQLiteOpenHelper(context, databaseName, null, DATABASE_VERSION) {
    private val context: Context

    init {
        this.context = context.applicationContext
        online = PreferencesHandler.isOnline(context)
    }

    override fun onCreate(db: SQLiteDatabase) {

        // Crear la tabla de Tareas
        var sql = ("CREATE TABLE IF NOT EXISTS tasks("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "title TEXT NOT NULL,"
                + "content TEXT DEFAULT '',"
                + "start_time INTEGER DEFAULT (strftime('%s', 'now'))"
                + ")")
        db.execSQL(sql)
        // Crear la tabla de Notas
        sql = ("CREATE TABLE IF NOT EXISTS notes("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "list_id INTEGER NOT NULL DEFAULT 0,"
                + "title TEXT NOT NULL,"
                + "content TEXT,"
                + "status INTEGER DEFAULT 0"
                + ")")
        db.execSQL(sql)

        // Crear la tabla de listas
        sql = ("CREATE TABLE IF NOT EXISTS lists("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "name TEXT UNIQUE NOT NULL,"
                + "privacy INTEGER DEFAULT 1,"
                + "type INTEGER DEFAULT 0"
                + ")")
        db.execSQL(sql)

        // Crear la tabla de amigos
        sql = ("CREATE TABLE IF NOT EXISTS friends("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "username TEXT UNIQUE NOT NULL,"
                + "since INTEGER DEFAULT (strftime('%s', 'now'))"
                + ")")
        db.execSQL(sql)
        if (!PreferencesHandler.isOnline(context)) {
            sql = "INSERT INTO lists (id, name) VALUES (1, 'Contraseñas')"
            db.execSQL(sql)
            sql = "INSERT INTO lists (id, name, type) VALUES (2, 'Lista de la compra', 1)"
            db.execSQL(sql)
            sql = "INSERT INTO lists (id, name, type) VALUES (3, 'Tareas pendientes', 1)"
            db.execSQL(sql)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TASKS")
        db.execSQL("DROP TABLE IF EXISTS $LISTS")
        db.execSQL("DROP TABLE IF EXISTS $NOTES")
        db.execSQL("DROP TABLE IF EXISTS $FRIENDS")
        onCreate(db)
    }

    /* SELECTS */
    fun getTasksByDay(day: LocalDateTime): MutableList<Task> {
        val taskList: MutableList<Task> = ArrayList()
        val projection = arrayOf(
            "id",
            "title",
            "content",
            "start_time"
        )
        val selection = "start_time >= ? AND start_time < ?"

        // Obtener el valor Unix para la medianoche (00:00)
        val unixStart = day.withHour(0).withMinute(0).withSecond(0).withNano(0).toEpochSecond(ZoneOffset.UTC)

        // Obtener el valor Unix para las 23:59
        val unixEnd = day.withHour(23).withMinute(59).withSecond(59).withNano(999999999).toEpochSecond(ZoneOffset.UTC)

        day.plusDays(1)

        val selectionArgs = arrayOf(unixStart.toString(), unixEnd.toString())
        try {
            readableDatabase.use { db ->
                db.query(
                    TASKS,
                    projection,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    "start_time"
                ).use { cursor ->
                    while (cursor.moveToNext()) {
                        val id: Int = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                        val title: String = cursor.getString(cursor.getColumnIndexOrThrow("title"))
                        val content: String =
                            cursor.getString(cursor.getColumnIndexOrThrow("content"))
                        val startTime = cursor.getLong(cursor.getColumnIndexOrThrow("start_time"))
                        val task = Task(id, title, content, startTime)
                        taskList.add(task)
                    }
                }
            }
        } catch (e: SQLiteException) {
            Logger.e(className, "Error al obtener las tareas", e)
        }
        return taskList
    }

    fun getNotesByList(listId: Int): MutableList<Note> {
        val notes: MutableList<Note> = ArrayList()
        try {
            val db: SQLiteDatabase = readableDatabase
            val projection = arrayOf(
                "id",
                "list_id",
                "title",
                "content",
                "status"
            )
            val selection = "list_id = ?"
            val selectionArgs = arrayOf(listId.toString())
            val cursor: Cursor = db.query(
                NOTES,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
            )
            while (cursor.moveToNext()) {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                val title = cursor.getString(cursor.getColumnIndexOrThrow("title"))
                val content = cursor.getString(cursor.getColumnIndexOrThrow("content"))
                val status = cursor.getInt(cursor.getColumnIndexOrThrow("status"))
                val note = Note(listId, id, title, content, status)
                note.id = id
                note.listId = listId
                note.title = title
                note.content = content
                note.status = status
                notes.add(note)
            }
            cursor.close()
            db.close()
        } catch (e: SQLiteException) {
            Logger.e(className, "Error al obtener las notas", e)
        }
        return notes
    }

    val lists: MutableList<NotesList>
        get() {
            val lists: MutableList<NotesList> = ArrayList()
            try {
                val db: SQLiteDatabase = readableDatabase
                val projection = arrayOf(
                    "id",
                    "name",
                    "privacy",
                    "type"
                )
                val cursor: Cursor = db.query(
                    LISTS,
                    projection,
                    null,
                    null,
                    null,
                    null,
                    null
                )
                while (cursor.moveToNext()) {
                    val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                    val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                    val privacy = cursor.getInt(cursor.getColumnIndexOrThrow("privacy"))
                    val type = cursor.getInt(cursor.getColumnIndexOrThrow("type"))
                    lists.add(NotesList(id, name, privacy, type))
                }
                cursor.close()
                db.close()
            } catch (e: SQLiteException) {
                Logger.e(className, "Error al obtener las listas", e)
            }
            return lists
        }
    val friends: List<Any>
        get() {
            val lists: MutableList<Friend> = ArrayList()
            val db: SQLiteDatabase = readableDatabase
            val projection = arrayOf(
                "id",
                "username",
                "since"
            )
            val cursor: Cursor = db.query(
                LISTS,
                projection,
                null,
                null,
                null,
                null,
                null
            )
            while (cursor.moveToNext()) {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                val username = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                val since = cursor.getLong(cursor.getColumnIndexOrThrow("since"))
                lists.add(Friend(id, username, since))
            }
            cursor.close()
            db.close()
            return lists
        }

    /**
     * Método para saber el tipo de nota que es
     *
     * @param listId Id de la lista
     * @return int : 0 (no es checklist) , 1 (checklist sin terminar), 2 (checklist terminada)
     */
    fun getType(listId: Int): Int {
        var type = 0
        val db: SQLiteDatabase = readableDatabase
        val projection = arrayOf(
            "type"
        )
        val selection = "id = ?"
        val selectionArgs = arrayOf(listId.toString())
        val cursor: Cursor = db.query(
            LISTS,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            null
        )
        if (cursor.moveToNext()) {
            type = cursor.getInt(cursor.getColumnIndexOrThrow("type"))
        }
        cursor.close()
        db.close()
        return type
    }

    /**
     * Método para saber si una nota es privada o no
     *
     * @param listId Id de la lista
     * @return int : 0 (no es checklist) , 1 (checklist sin terminar), 2 (checklist terminada)
     */
    fun getPrivacy(listId: Int): Int {
        var privacy = 0
        val db: SQLiteDatabase = readableDatabase
        val projection = arrayOf(
            "privacy"
        )
        val selection = "id = ?"
        val selectionArgs = arrayOf(listId.toString())
        val cursor: Cursor = db.query(
            LISTS,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            null
        )
        if (cursor.moveToNext()) {
            privacy = cursor.getInt(cursor.getColumnIndexOrThrow("privacy"))
        }
        cursor.close()
        db.close()
        return privacy
    }

    /* INSERTS */
    fun insertTask(mTask: Task) {
        val db: SQLiteDatabase = writableDatabase
        val values = ContentValues()

        // Comprobar que los valores no sean nulos antes de insertarlos
        if (mTask.id != 0) {
            values.put("id", mTask.id)
        }
        values.put("title", mTask.title)

        if (mTask.content != null) {
            values.put("content", mTask.content)
        }
        if (mTask.startTime != null) {
            values.put("start_time", mTask.startTime)
        }
        db.insert(TASKS, null, values)
        db.close()
    }

    fun insertNote(mNote: Note) {
        val db: SQLiteDatabase = writableDatabase
        val values = ContentValues()

        // Comprobar que los valores no sean nulos antes de insertarlos
        if (mNote.id != 0) {
            values.put("id", mNote.id)
        }
        values.put("title", mNote.title)
        if (mNote.listId > 0) {
            values.put("list_id", mNote.listId)
        }
        if (mNote.content != null) {
            values.put("content", mNote.content)
        }
        values.put("status", mNote.status)
        db.insert(NOTES, null, values)
        db.close()
    }

    fun insertList(mNotesList: NotesList): Int {
        val db: SQLiteDatabase = writableDatabase
        val values = ContentValues()

        // Comprobar que los valores no sean nulos antes de insertarlos
        if (mNotesList.id != 0) {
            values.put("id", mNotesList.id)
        }
        values.put("name", mNotesList.name)
        values.put("privacy", mNotesList.privacy)
        values.put("type", mNotesList.type)
        val ret: Int = db.insert(LISTS, null, values).toInt()
        db.close()
        return ret
    }

    fun insertFriend(friend: Friend) {
        val db: SQLiteDatabase = writableDatabase
        val values = ContentValues()

        // Comprobar que los valores no sean nulos antes de insertarlos
        if (friend.id != 0) {
            values.put("id", friend.id)
        }
        values.put("username", friend.username)

        if (friend.since != null) {
            values.put("since", friend.since)
        }
        db.insert(FRIENDS, null, values)
        db.close()
    }

    /* EXISTS */
    fun existsList(listName: String): Boolean {
        val db: SQLiteDatabase = readableDatabase
        var cursor: Cursor? = null
        val query = "SELECT * FROM lists WHERE name = ?"
        return try {
            val selectionArgs = arrayOf(listName)
            cursor = db.rawQuery(query, selectionArgs)
            cursor != null && cursor.count > 0 // La lista existe en la tabla
        } catch (e: Exception) {
            // Manejar la excepción, por ejemplo, imprimir un mensaje de error
            e.printStackTrace()
            false // Indicar que la lista no existe debido a la excepción
        } finally {
            cursor?.close()
        }
    }

    /* UPDATES */
    fun updateNoteStatus(id: Int, isChecked: Boolean) {
        val db: SQLiteDatabase = writableDatabase
        val sql = "UPDATE notes SET status = ? WHERE id = ?"
        val newStatus =
            if (isChecked) 2 else 1 // Si isChecked es true, status = 2; si es false, status = 1
        db.execSQL(sql, arrayOf(newStatus, id))
        db.close()
    }

    /* SETTERS */
    fun setTasks(tasks: ArrayList<Task>) : Boolean{

        var success = false
        val db: SQLiteDatabase = writableDatabase
        try {
            db.delete(TASKS, null, null)
            for (serverTask in tasks) {
                val id: String = serverTask.id.toString()
                val title: String = serverTask.title
                val content: String? = serverTask.content
                val startTime: Long? = serverTask.startTime
                val sql = "INSERT INTO tasks (id, title, content, start_time) VALUES (?,?,?,?)"
                db.execSQL(sql, arrayOf(id, title, content, startTime))
            }
            success = true
        } catch (e: SQLiteException) {
            Logger.e(className, "Error al establecer las tareas", e)
            dropTable(TASKS)
            onCreate(db)
        }
        return success
    }

    fun setLists(lists: ArrayList<NotesList>): Boolean {
        val db: SQLiteDatabase = writableDatabase
        var success = false
        val maxRetries = 2 // Número máximo de intentos
        var retry = 0
        while (retry < maxRetries && !success) {
            try {
                db.delete(LISTS, null, null)
                for (serverList in lists) {
                    val id: Int? = serverList.id
                    val name: String = serverList.name
                    val privacy: Int = serverList.privacy
                    val sql = "INSERT INTO lists (id, name, privacy) VALUES (?,?,?)"
                    db.execSQL(sql, arrayOf(id, name, privacy))
                }
                success = true // Marcamos éxito si llegamos aquí sin excepciones
            } catch (e: SQLiteException) {
                Logger.e(className, "Error al establecer las listas", e)
                dropTable(LISTS)
                onCreate(db)
            }
            retry++
        }
        return success
    }

    fun setNotes(notes: ArrayList<Note>) : Boolean {
        val db: SQLiteDatabase = writableDatabase
        var success = false
        try {
            db.delete(NOTES, null, null)
            for (serverNote in notes) {
                val id: String = serverNote.id.toString()
                val title: String = serverNote.title
                val content: String? = serverNote.content
                val listId: Int = serverNote.listId
                val status: Int = serverNote.status
                val sql =
                    "INSERT INTO notes (id, list_id, title, content, status) VALUES (?,?,?,?,?)"
                db.execSQL(sql, arrayOf(id, listId, title, content, status))
                success = true
            }
        } catch (e: SQLiteException) {
            Logger.e(className, "Error al establecer las notas", e)
            dropTable(NOTES)
            onCreate(db)
        }
        return success
    }

    fun setFriends(friends: ArrayList<Friend?>) {
        val db: SQLiteDatabase = writableDatabase
        try {
            db.delete(FRIENDS, null, null)
            for (serverFriend in friends) {
                val id: Int? = serverFriend?.id
                val username: String? = serverFriend?.username
                val since: Long? = serverFriend?.since
                val sql = "INSERT INTO friends(id, username, since) VALUES (?,?,?)"
                db.execSQL(sql, arrayOf(id, username, since))
            }
        } catch (e: SQLiteException) {
            onCreate(db)
        }
    }

    /* UPDATE */
    fun updateList(mList: NotesList): Int {
        val db: SQLiteDatabase = writableDatabase
        val values = ContentValues()
        values.put("name", mList.name)
        values.put("privacy", mList.privacy)
        values.put("type", mList.type)
        val whereClause = "id = ?"
        val whereArgs = arrayOf<String>(java.lang.String.valueOf(mList.id))
        val updatedLists: Int = db.update(LISTS, values, whereClause, whereArgs)
        db.close()
        return updatedLists
    }

    fun updateNote(mNote: Note): Int {
        val db: SQLiteDatabase = writableDatabase
        val values = ContentValues()
        values.put("title", mNote.title)
        values.put("content", mNote.content)
        values.put("status", mNote.status)
        val whereClause = "id = ?"
        val whereArgs = arrayOf<String>(java.lang.String.valueOf(mNote.id))
        val updatedNotes: Int = db.update(NOTES, values, whereClause, whereArgs)
        db.close()
        return updatedNotes
    }

    fun updateTask(mTask: Task): Int {
        val db: SQLiteDatabase = writableDatabase
        val values = ContentValues()
        values.put("title", mTask.title)
        values.put("content", mTask.content)
        values.put("start_time", mTask.startTime)
        val whereClause = "id = ?"
        val whereArgs = arrayOf<String>(java.lang.String.valueOf(mTask.id))
        val updatedRows: Int = db.update(TASKS, values, whereClause, whereArgs)
        db.close()
        return updatedRows
    }

    /* DELETE */
    fun delete(table: String?, id: Int): Int {
        // Obtén una instancia de la base de datos en modo escritura
        val db: SQLiteDatabase = writableDatabase

        // Define la cláusula WHERE para especificar la nota que se desea eliminar
        val selection = "id = ?"
        val selectionArgs = arrayOf(id.toString())

        // Elimina la nota de la tabla utilizando la cláusula WHERE y los argumentos de selección
        return db.delete(table, selection, selectionArgs)
    }

    private fun dropTable(tableName: String) {

        // Obtén una instancia de la base de datos en modo escritura
        try {
            val db: SQLiteDatabase = writableDatabase
            db.execSQL("DROP TABLE IF EXISTS $tableName")
        } catch (e: Exception) {
            Logger.e("DB", "Error al eliminar la tabla $tableName", e)
        }
    }

    private fun dropTables() {

        // Obtén una instancia de la base de datos en modo escritura
        try {
            val db: SQLiteDatabase = writableDatabase
            db.execSQL("DROP TABLE IF EXISTS $TASKS")
            db.execSQL("DROP TABLE IF EXISTS $NOTES")
            db.execSQL("DROP TABLE IF EXISTS $LISTS")
            db.execSQL("DROP TABLE IF EXISTS $FRIENDS")
        } catch (e: Exception) {
            Logger.e("DB", "Error al eliminar las tablas", e)
        }
    }

    fun deleteAll() {

        // Obtén una instancia de la base de datos en modo escritura
        try {
            val db: SQLiteDatabase = writableDatabase
            db.execSQL("DROP TABLE IF EXISTS $TASKS")
            db.execSQL("DROP TABLE IF EXISTS $NOTES")
            db.execSQL("DROP TABLE IF EXISTS $LISTS")
            db.execSQL("DROP TABLE IF EXISTS $FRIENDS")
        } catch (e: Exception) {
            Logger.e("DB", "Error al eliminar las tablas", e)
        }
        PreferencesHandler.clear(context)
    }



    companion object {
        val className: String = DB::class.java.simpleName
        const val DATABASE_VERSION = 4
        private const val DATABASE_ONLINE = "secretaria_online.sqlite"
        private const val DATABASE_OFFLINE = "secretaria_offline.sqlite"
        const val TASKS = "tasks"
        const val LISTS = "lists"
        const val NOTES = "notes"
        const val FRIENDS = "friends"
        private lateinit var contextRef: WeakReference<Context>
        private var online: Boolean = false

        @Synchronized
        fun getInstance(context: Context): DB {

            contextRef = WeakReference(context.applicationContext)
            online = PreferencesHandler.isOnline(contextRef.get()!!)
            val onlineNow: Boolean = PreferencesHandler.isOnline(context.applicationContext)
            return if (onlineNow) DB(context.applicationContext, DATABASE_ONLINE)
            else DB(context.applicationContext, DATABASE_OFFLINE)
        }
    }
}