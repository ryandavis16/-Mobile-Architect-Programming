package com.example.projecttwo

import android.Manifest
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.projecttwo.repo.WeightRepo
import com.example.projecttwo.util.SmsHelper

class GridActivity : AppCompatActivity() {

    companion object { private const val REQ_SEND_SMS = 1001 }

    private var userId: Long = -1
    private lateinit var repo: WeightRepo
    private var adapter: SimpleCursorAdapter? = null

    // pending sms while we ask permission
    private var pendingTo: String? = null
    private var pendingMsg: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_grid)

        userId = intent.getLongExtra("user_id", -1)
        repo = WeightRepo(this)

        val grid = findViewById<GridView>(R.id.grid)
        val btnAdd = findViewById<Button>(R.id.btnAdd)
        val header = findViewById<TextView>(R.id.tvHeader)

        btnAdd.setOnClickListener { showEditDialog(-1, "", "", "") }

        grid.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, id ->
            val c = adapter?.getItem(position) as Cursor
            val day = c.getString(c.getColumnIndexOrThrow("day"))
            val lbs = c.getString(c.getColumnIndexOrThrow("lbs"))
            val note = c.getString(c.getColumnIndexOrThrow("note"))
            showEditDialog(id, day, lbs, note)
        }

        grid.onItemLongClickListener = AdapterView.OnItemLongClickListener { _, _, _, id ->
            repo.delete(id); refresh()
            Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show()
            true
        }

        // long press header to test SMS
        header.setOnLongClickListener {
            val to = SmsHelper.randomPhone443()
            val msg = "Weight Watcha’s reminder: you are close to your goal weight!"
            maybeSendSms(to, msg)
            true
        }
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        val cursor = repo.allForUser(userId)
        if (adapter == null) {
            val from = arrayOf("day", "lbs", "note")
            val to = intArrayOf(R.id.colDay, R.id.colLbs, R.id.colNote)
            adapter = SimpleCursorAdapter(this, R.layout.row_weight, cursor, from, to, 0)
            findViewById<GridView>(R.id.grid).adapter = adapter
        } else {
            adapter!!.changeCursor(cursor)
        }
    }

    private fun showEditDialog(id: Long, day: String, lbs: String, note: String) {
        val v: View = layoutInflater.inflate(R.layout.dialog_edit, null, false)
        val etDay = v.findViewById<EditText>(R.id.etDay)
        val etLbs = v.findViewById<EditText>(R.id.etLbs)
        val etNote = v.findViewById<EditText>(R.id.etNote)

        if (day.isNotEmpty()) {
            etDay.setText(day); etLbs.setText(lbs); etNote.setText(note)
        }

        AlertDialog.Builder(this)
            .setTitle(if (id < 0) "Add Entry" else "Edit Entry")
            .setView(v)
            .setPositiveButton("Save") { _, _ ->
                val dStr = etDay.text.toString()
                val wVal = etLbs.text.toString().toDoubleOrNull() ?: 0.0
                val nStr = etNote.text.toString()
                if (id < 0) repo.add(userId, dStr, wVal, nStr) else repo.update(id, dStr, wVal, nStr)
                refresh()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /** Permission helper */
    private fun maybeSendSms(to: String, msg: String) {
        val granted = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) ==
                PackageManager.PERMISSION_GRANTED
        if (granted) {
            SmsHelper.sendSms(to, msg)
            Toast.makeText(this, "Test SMS sent to $to", Toast.LENGTH_SHORT).show()
        } else {
            pendingTo = to
            pendingMsg = msg
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS), REQ_SEND_SMS)
            Toast.makeText(this, "Requesting SMS permission…", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQ_SEND_SMS && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            val to = pendingTo
            val msg = pendingMsg
            if (!to.isNullOrBlank() && !msg.isNullOrBlank()) {
                SmsHelper.sendSms(to, msg)
                Toast.makeText(this, "Test SMS sent to $to", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == REQ_SEND_SMS) {
            Toast.makeText(this, "SMS denied, app continues without SMS", Toast.LENGTH_SHORT).show()
        }
        pendingTo = null
        pendingMsg = null
    }
}
