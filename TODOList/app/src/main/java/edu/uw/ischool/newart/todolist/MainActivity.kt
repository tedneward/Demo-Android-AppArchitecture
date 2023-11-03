package edu.uw.ischool.newart.todolist

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale


// ====================================
// Domain objects
//
data class Todo(val item : String, val dueDate : Date = Date.from(Instant.now()), val id : Int = -1) {
    override fun toString(): String {
        return "$id: $item (Due $dueDate)"
    }
}

// ====================================
// Repository
//
interface ITodoRepository {
    fun getAll() : List<Todo>
    //fun get(id : Int) : Todo
    //fun update(todo : Todo)
    fun delete(todo : Todo)
    fun insert(todo : Todo)
}
class MockTodoRepository : ITodoRepository {
    val items : MutableList<Todo> = mutableListOf(
        Todo("Eat lunch"),
        Todo("Eat Halloween candy")
    )

    override fun getAll(): List<Todo> {
        return items
    }

    override fun delete(todo: Todo) {
        items.remove(todo)
    }

    override fun insert(todo: Todo) {
        items.add(todo)
    }
}
class FileTodoRepository(val context : Context) : ITodoRepository {
    val TAG = "FileTodoRepository"
    lateinit var items : MutableList<Todo>

    init {
        readItems()
    }

    override fun getAll(): List<Todo> {
        return items
    }

    override fun delete(todo: Todo) {
        items.remove(todo)
        writeItems()
    }

    override fun insert(todo: Todo) {
        items.add(todo)
        writeItems()
    }

    private fun readItems() {
        Log.v(TAG, "Calling readItems()")
        val todoFile = File(context.filesDir, "todo.txt")
        items = mutableListOf()
        try {
            val reader = BufferedReader(FileReader(todoFile))
            for (line in reader.lines()) {
                // Split along the "|" characters
                val lineParts = line.split("|")
                Log.v(TAG, "Found $lineParts")
                items.add(Todo(lineParts[1],
                    Date(lineParts[2]),
                    Integer.getInteger(lineParts[0]) ?: -1))
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error while reading items", e)
        }
    }

    private fun writeItems() {
        val todoFile = File(context.filesDir, "todo.txt")
        try {
            val writer = FileWriter(todoFile)
            for (item in items) {
                writer.write("${item.id}|${item.item}|${item.dueDate}\n")
            }
            writer.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        Log.v(TAG, "Wrote file ${todoFile.absolutePath}")
    }
}

// ====================================
// Application object: Convenient singleton on which to hang our Repository
// for easier access from all activities and services
//
class TodoApp : Application() {
    lateinit var todoRepository : ITodoRepository

    // If we try to create the File repository in the
    // initializer, the Context isn't fully set up yet.
    // That's what it throws an exception.
    // All initialization should really be deferred
    // until onCreate() for anything that needs to
    // talk to the Android platform (e.g., needs a
    // Context object).

    override fun onCreate() {
        super.onCreate()

        todoRepository = FileTodoRepository(this)
    }
}

// ====================================
// View (UI) layer code
//
class MainActivity : Activity() {
    var items: List<Todo> = mutableListOf()
    lateinit var lvItems: ListView
    lateinit var itemsAdapter: ArrayAdapter<Todo>
    lateinit var btnAddItem: Button
    lateinit var edtNewItem: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val todoApp = (application as TodoApp)
        val repository = todoApp.todoRepository
        items = repository.getAll()

        lvItems = findViewById(R.id.lstItems)
        edtNewItem = findViewById(R.id.edtNewItem)
        btnAddItem = findViewById(R.id.btnAddItem)

        itemsAdapter = ArrayAdapter<Todo>(this, android.R.layout.simple_list_item_1, items)
        lvItems.adapter = itemsAdapter

        btnAddItem.setOnClickListener {
            // Insert a new one
            val itemText = edtNewItem.text.toString()
            (application as TodoApp).todoRepository.insert(Todo(itemText))
            itemsAdapter.notifyDataSetChanged()
            edtNewItem.setText("")
        }

        lvItems.setOnItemLongClickListener { adapter, item, pos, id ->
            // Remove one
            val todo = items[pos]
            (application as TodoApp).todoRepository.delete(todo)
            itemsAdapter.notifyDataSetChanged()
            true
        }
    }
}





