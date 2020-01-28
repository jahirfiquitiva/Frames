package dev.jahir.frames.data.models

data class DonationItem(val id: String, val name: String, private val price: String) {
    override fun toString(): String = "$name - $price"
}