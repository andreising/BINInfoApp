package com.example.bininfoapp

import android.app.DownloadManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.bininfoapp.databinding.ActivityMainBinding

const val API_KEY = "https://lookup.binlist.net/"

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        getResult("48934704")
    }

    private fun getResult(name: String){
        val url = API_KEY+name
        val queue = Volley.newRequestQueue(this)
        val stringRequest = StringRequest(
            Request.Method.GET,
            url,
            {
                response->
                binding.textView.text = response
            },
            {
                Toast.makeText(this, "Error: $it", Toast.LENGTH_SHORT).show()
            })
        queue.add(stringRequest)
    }
}