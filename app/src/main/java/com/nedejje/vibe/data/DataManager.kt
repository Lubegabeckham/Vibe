package com.nedejje.vibe.data

import androidx.compose.runtime.mutableStateListOf

object DataManager {
    val events = mutableStateListOf<Event>(
        Event(
            title = "Nyege Nyege Festival",
            date = "Sept 4-7, 2025",
            location = "Jinja, Uganda",
            description = "The biggest music festival in East Africa.",
            priceOrdinary = 150000,
            priceVIP = 350000,
            priceVVIP = 500000
        ),
        Event(
            title = "Blankets and Wine",
            date = "Oct 15, 2025",
            location = "Lugogo Cricket Oval",
            description = "A picnic-style music festival.",
            priceOrdinary = 100000,
            priceVIP = 250000,
            isFree = false
        ),
        Event(
            title = "Community Yoga",
            date = "Every Sunday",
            location = "City Park",
            isFree = true
        )
    )

    fun addEvent(event: Event) {
        events.add(event)
    }

    fun removeEvent(event: Event) {
        events.remove(event)
    }

    fun updateEvent(updatedEvent: Event) {
        val index = events.indexOfFirst { it.id == updatedEvent.id }
        if (index != -1) {
            events[index] = updatedEvent
        }
    }
    
    fun getEventById(id: String): Event? {
        return events.find { it.id == id }
    }
}
