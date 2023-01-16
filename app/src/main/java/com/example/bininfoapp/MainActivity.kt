package com.example.bininfoapp

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.bininfoapp.databinding.ActivityMainBinding
import org.json.JSONException
import org.json.JSONObject
import java.util.*

const val API_KEY = "https://lookup.binlist.net/"
const val PREF_KEY = "search_history"

class MainActivity : AppCompatActivity() {
    private var pref: SharedPreferences? = null
    private lateinit var binding: ActivityMainBinding
    private lateinit var listSearch: MutableList<String>
    lateinit var adapter: ArrayAdapter<String>
    private val black = Color.parseColor("#000000")
    private val gray = Color.parseColor("#9C9B9B")

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        pref = getSharedPreferences("data", MODE_PRIVATE)
        initArrayAdapter()
        inputFormat()
        initListener()
    }

    override fun onPause() {
        super.onPause()
        saveData()
    }

    private fun initListener() = with(binding) {
        searchButton.setOnClickListener {
            getResult(inputCardNumber.text.toString().filter { it != ' ' })
        }
        countryContainer.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("geo:${latitudeInfoTextView.text}+${longitudeInfoTextView.text}?z=19&q=")
            )
            startActivity(intent)
        }
        countryContainer.isClickable = false
    }

    private fun initArrayAdapter() {
        listSearch = pref?.getStringSet(PREF_KEY, emptySet<String>())!!.toMutableList()
        adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_activated_1,
            listSearch
        ).also { adapter ->
            binding.inputCardNumber.setAdapter(adapter)
        }
    }

    private fun getResult(name: String) {
        val url = API_KEY + name
        val queue = Volley.newRequestQueue(this)
        val stringRequest = StringRequest(
            Request.Method.GET,
            url,
            { response ->
                try {
                    val item = parseData(response)
                    bind(item)
                    listSearch.add(name)
                    adapter.add(name)
                } catch (r: JSONException) {
                    bind(CardInfo())
                    Toast.makeText(this, "Invalid Card Number", Toast.LENGTH_SHORT)
                        .show()
                }
            },
            {
                bind(CardInfo())
                Toast.makeText(this, "Invalid Card Number", Toast.LENGTH_SHORT).show()
            })
        queue.add(stringRequest)
    }

    private fun parseData(result: String): CardInfo {
        val mainObject = JSONObject(result)
        val country = mainObject.optJSONObject("country")
        val bank = mainObject.optJSONObject("bank")
        var type: Boolean?
        mainObject.optString("type").let {
            type = when (it) {
                "debit" -> true
                "credit" -> false
                else -> null
            }
        }
        var prepaid: Boolean?
        mainObject.optString("prepaid").let {
            prepaid = when (it) {
                "true" -> true
                "false" -> false
                else -> null
            }
        }
        var luhn: Boolean?
        mainObject.getJSONObject("number").optString("luhn").let {
            luhn = when (it) {
                "true" -> true
                "false" -> false
                else -> null
            }
        }
        return CardInfo(
            mainObject.getJSONObject("number").optInt("length"),
            luhn,
            mainObject.optString("scheme")
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
            type,
            mainObject.optString("brand")
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
            prepaid,
            country?.optString("numeric"),
            country?.optString("alpha2"),
            country?.optString("name"),
            country?.optString("emoji"),
            country?.optString("currency"),
            country?.optInt("latitude"),
            country?.optInt("longitude"),
            bank?.optString("name"),
            bank?.optString("url"),
            bank?.optString("phone"),
            bank?.optString("city")
        )
    }

    private fun bind(item: CardInfo) = with(binding) {
        nullChecker(receivedShame, item.shame)
        nullChecker(receivedBrand, item.brand)
        chooseBoldTextView(typeDebit, typeCredit, item.type)
        chooseBoldTextView(yesTextView, noTextView, item.prepaid)
        nullChecker(lengthInfo, item.cardLength)
        chooseBoldTextView(lunhYesTextView, lunhNoTextView, item.cardLuhn)
        nullCheckerFor2Items(countryName, item.countryName, " ", item.countryEmoji)
        binding.countryContainer.isClickable = false
        nullChecker(latitudeInfoTextView, item.countryLatitude)
        nullChecker(longitudeInfoTextView, item.countryLongitude)
        nullCheckerFor2Items(bankNameTextView, item.bankName, ", ", item.bankCity)
        nullChecker(bankWebSiteTextView, item.bankUrl)
        nullChecker(bankPhoneNumberTextView, item.bankPhone)
    }

    private fun chooseBoldTextView(
        textView1: TextView,
        textView2: TextView,
        bool1: Boolean?
    ) {
        goDefault(textView1)
        goDefault(textView2)
        when (bool1) {
            true -> {
                highlight(textView1)
            }
            false -> {
                highlight(textView2)
            }
            else -> {}
        }
    }

    private fun highlight(textView: TextView) {
        textView.setTextColor(black)
        textView.setTypeface(null, Typeface.BOLD)
    }

    private fun goDefault(vararg textView: TextView) {
        textView.forEach {
            it.setTextColor(gray)
            it.setTypeface(null, Typeface.NORMAL)
            binding.apply {
                if (!listOf(
                        typeDebit, typeCredit, yesTextView, lunhYesTextView, noTextView,
                        lunhNoTextView
                    )
                        .contains(it)
                ) it.setText(R.string.question_mark)
            }
        }
    }

    private fun nullChecker(textView: TextView, info: Any?) {
        goDefault(textView)
        if (info != null && info != "Null" && info != "") {
            when (textView) {

                binding.latitudeInfoTextView -> {
                    textView.text = info.toString()
                    textView.setTextColor(black)
                    binding.countryContainer.isClickable = true
                }
                binding.longitudeInfoTextView -> {
                    textView.text = info.toString()
                    textView.setTextColor(black)
                }
                binding.bankPhoneNumberTextView -> {
                    textView.text = info.toString()
                    textView.setTextColor(black)
                }
                binding.bankWebSiteTextView -> {
                    textView.text = info.toString()
                    textView.linksClickable = true

                }
                else -> {
                    textView.text = info.toString()
                    highlight(textView)
                }
            }
        }
    }

    private fun nullCheckerFor2Items(textView: TextView, info1: Any?, space: String, info2: Any?) {
        goDefault(textView)
        if (info1 != null && info1 != "Null" && info1 != "") {
            var str = info1.toString()
            if (info2 != null && info2 != "Null" && info2 != "") str =
                info1.toString() + space + info2.toString()
            textView.text = str
            highlight(textView)
        }
    }

    private fun saveData() {
        pref?.edit()
            ?.putStringSet(PREF_KEY, listSearch.toSet())
            ?.apply()
    }

    private fun inputFormat() {
        binding.inputCardNumber.addTextChangedListener(CreditCardNumberFormattingTextWatcher())
    }
}