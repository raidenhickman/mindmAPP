package com.example.mindmapp

import android.annotation.SuppressLint
import android.content.res.Resources
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import org.json.JSONArray
import org.json.JSONObject

private val Int.dp: Float
    get() = this * Resources.getSystem().displayMetrics.density
class MainActivity : AppCompatActivity() {

    private lateinit var rootLayout: FrameLayout
    private lateinit var addButton: Button
    private lateinit var deleteButton: Button
    private lateinit var deleteAllButton: Button
    private var selectedBox: EditText? = null
    private var boxes = mutableListOf<EditText>()
    private val prefs by lazy { getSharedPreferences("boxes_prefs", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rootLayout = findViewById(R.id.rootLayout)
        addButton = findViewById(R.id.addButton)
        deleteButton= findViewById(R.id.deleteButton)
        deleteAllButton = findViewById(R.id.deleteAllButton)

        resetBoxes()
        loadBoxes()

        addButton.setOnClickListener {
            createBox("New Box", 10.dp, 10.dp)
            saveBoxes()
        }

        deleteButton.setOnClickListener {
            selectedBox?.let {
                rootLayout.removeView(it)
                boxes.remove(it)
                selectedBox = null
                saveBoxes()
            }
        }

        deleteAllButton.setOnClickListener {
            resetBoxes()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun createBox(text: String, x: Float, y: Float) {
        val editText = EditText(this).apply {
            setText(text)
            this.x = x
            this.y = y
            val metrics: DisplayMetrics = this.resources.displayMetrics
            layoutParams = FrameLayout.LayoutParams((metrics.widthPixels/2.5).toInt(), (metrics.heightPixels/4.5).toInt())
            setSingleLine(true)
            setBackgroundResource(android.R.drawable.edit_text)
        }

        var dX = 0f
        var dY = 0f
        var isDragging = false

        editText.setOnLongClickListener {
            isDragging = true
            true
        }

        editText.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_MOVE -> if (isDragging) {
                    v.x = event.rawX + dX
                    v.y = event.rawY + dY
                }
                MotionEvent.ACTION_DOWN -> {
                    dX = v.x - event.rawX
                    dY = v.y - event.rawY
                }
                MotionEvent.ACTION_UP -> {
                    if (isDragging) saveBoxes()
                    isDragging = false
                }
            }
            editText.setOnClickListener {
                selectedBox?.setBackgroundResource(android.R.drawable.edit_text)
                selectedBox = editText
                editText.setBackgroundResource(android.R.drawable.dialog_holo_light_frame)
            }
            false
        }

        rootLayout.addView(editText)
        boxes.add(editText)
    }

    private fun saveBoxes() {
        val jsonArray = JSONArray()
        for (box in boxes) {
            val obj = JSONObject()
            obj.put("text", box.text.toString())
            obj.put("x", box.x)
            obj.put("y", box.y)
            jsonArray.put(obj)
        }
        prefs.edit { putString("boxes", jsonArray.toString()) }
    }

    private fun loadBoxes() {
        val jsonString = prefs.getString("boxes", null) ?: return
        val jsonArray = JSONArray(jsonString)
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            createBox(
                obj.getString("text"),
                obj.getDouble("x").toFloat(),
                obj.getDouble("y").toFloat()
            )
        }
    }


    private fun resetBoxes() {
        prefs.edit().remove("boxes").apply()
        rootLayout.removeAllViews()
        boxes.clear()
        rootLayout.addView(addButton)
        rootLayout.addView(deleteButton)
        rootLayout.addView(deleteAllButton)
    }

}
