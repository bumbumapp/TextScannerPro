package com.bumbumapps.studytextscan.service.textFilter


interface TextFilterService {

    fun filterTextForPhoneNumbers(text: String): List<Pair<String, String>>

    fun filterTextForEmails(text: String): List<Pair<String, String>>

    fun filterTextForLinks(text: String): List<Pair<String, String>>

}