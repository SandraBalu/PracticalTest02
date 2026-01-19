package ro.pub.cs.systems.eim.practicaltest02v1

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlin.concurrent.thread

class PracticalTest02v1MainActivity : AppCompatActivity() {

    /* =========================
       VARIABILE UI (Subiectul 2)
       ========================= */
    private lateinit var editTextPrefix: EditText
    private lateinit var buttonSearch: Button
    private lateinit var textViewResults: TextView

    /* =========================
       CONSTANTE PENTRU BROADCAST (Subiectul 3.c)
       ========================= */
    private val ACTION_AUTOCOMPLETE =
        "ro.pub.cs.systems.eim.practicaltest02v1.AUTOCOMPLETE"

    private val EXTRA_RESULT = "result"

    /* =========================
       BROADCAST RECEIVER (Subiectul 3.c)
       Primește sugestia #3 și actualizează UI
       ========================= */
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

            // Luăm sugestia trimisă prin broadcast
            val result = intent?.getStringExtra(EXTRA_RESULT) ?: return

            // Actualizăm UI-ul (DOAR AICI pentru 3.c)
            textViewResults.text = result

            Log.d("BROADCAST_TEST", "Receiver received: $result")
        }
    }

    /* =========================
       onCreate – inițializare UI + logica principală
       ========================= */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_practical_test02v1_main)

        // 1️⃣ Legăm componentele din XML (Subiectul 2)
        editTextPrefix = findViewById(R.id.editTextPrefix)
        buttonSearch = findViewById(R.id.buttonSearch)
        textViewResults = findViewById(R.id.textViewResults)

        /* =========================
           Subiectul 4
           Deschiderea hărții Google la click pe rezultat
           ========================= */
        textViewResults.setOnClickListener {
            startActivity(
                Intent(this, PracticalTest02v1MapActivity::class.java)
            )
        }

        /* =========================
           Subiectul 3 – Click pe Search
           ========================= */
        buttonSearch.setOnClickListener {

            // 3.a – citim prefixul
            val prefix = editTextPrefix.text.toString().trim()

            // feedback minim UI
            textViewResults.text =
                if (prefix.isEmpty()) "Introduce un prefix!" else "Prefix: $prefix"

            Log.d("GOOGLE_AUTO", "Button clicked, prefix=$prefix")

            // 3.a – request HTTP (thread separat)
            thread {
                try {
                    val encoded = URLEncoder.encode(prefix, "UTF-8")

                    val url = URL(
                        "https://www.google.com/complete/search?client=chrome&q=$encoded"
                    )

                    val conn = (url.openConnection() as HttpURLConnection).apply {
                        requestMethod = "GET"
                        connectTimeout = 10_000
                        readTimeout = 10_000
                        setRequestProperty("User-Agent", "Mozilla/5.0")
                        setRequestProperty("Accept", "*/*")
                    }

                    val stream =
                        if (conn.responseCode in 200..299) conn.inputStream
                        else conn.errorStream

                    val response =
                        stream.bufferedReader().use { it.readText() }

                    // 3.a – logăm răspunsul complet
                    Log.d("GOOGLE_AUTO", response)

                    /* =========================
                       3.b – Parsare sugestia #3
                       ========================= */
                    val arr = JSONArray(response)
                    val suggestions = arr.getJSONArray(1)

                    val thirdSuggestion =
                        if (suggestions.length() >= 3)
                            suggestions.getString(2)
                        else
                            "Nu există 3 sugestii"

                    /* =========================
                       3.c – Trimitem sugestia prin BROADCAST
                       ========================= */
                    val intent = Intent(ACTION_AUTOCOMPLETE).apply {
                        setPackage(packageName) // doar în aplicația noastră
                        putExtra(EXTRA_RESULT, thirdSuggestion)
                    }

                    Log.d("BROADCAST_TEST", "Sending broadcast with: $thirdSuggestion")
                    sendBroadcast(intent)

                    conn.disconnect()

                } catch (e: Exception) {
                    Log.e("GOOGLE_AUTO", "Error: ${e.message}", e)
                }
            }
        }
    }

    /* =========================
       3.c – Înregistrare receiver
       ========================= */
    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onResume() {
        super.onResume()

        val filter = IntentFilter(ACTION_AUTOCOMPLETE)

        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(receiver, filter)
        }
    }

    /* =========================
       3.c – Dezînregistrare receiver
       ========================= */
    override fun onPause() {
        unregisterReceiver(receiver)
        super.onPause()
    }
}
