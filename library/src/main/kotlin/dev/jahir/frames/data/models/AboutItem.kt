package dev.jahir.frames.data.models

data class AboutItem(
    val name: String,
    val description: String? = "",
    val photoUrl: String? = "",
    val links: ArrayList<Pair<String, String>> = ArrayList()
)