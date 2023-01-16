package com.example.bininfoapp

import java.util.*

data class CardInfo(
    val cardLength: Int?=null,
    val cardLuhn: Boolean?=null,
    val shame: String?=null,
    val type: Boolean?=null,
    val brand: String?=null,
    val prepaid: Boolean?=null,
    val countryNumeric: String?=null,
    val countryAlpha2: String?=null,
    val countryName: String?=null,
    val countryEmoji: String?=null,
    val countryCurrency: String?=null,
    val countryLatitude: Int?=null,
    val countryLongitude: Int?=null,
    val bankName: String?=null,
    val bankUrl: String?=null,
    val bankPhone: String?=null,
    val bankCity: String?=null
)
