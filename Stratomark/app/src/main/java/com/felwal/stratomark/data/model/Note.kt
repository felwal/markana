package com.felwal.stratomark.data.model

data class Note(var title: String = "", var body: String = "", var extension: String = "txt") {

    init {
        if (extension == "") extension = "txt"
    }

    override fun toString(): String = "$title.$extension: $body"

    fun isEmpty(): Boolean = title == "" && body == ""
}