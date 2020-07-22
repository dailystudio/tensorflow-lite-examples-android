package com.dailystudio.tflite.example.text.bertqa

data class Contents(val titles: Array<String>,
                    val contents: Array<String>,
                    val questions: Array<Array<String>>)

data class Article(val title: String,
                   val content: String,
                   val questions: Array<String>)