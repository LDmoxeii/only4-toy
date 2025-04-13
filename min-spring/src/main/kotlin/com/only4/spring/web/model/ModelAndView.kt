package com.only4.spring.web.model

class ModelAndView(var view: String) {
    val context = mutableMapOf<String, String>()
}
