package com.nedejje.vibe.ui.util

/**
 * Returns the drawable resource name for a given event title.
 * Place all JPEG files in res/drawable/ using the names below.
 */
fun eventImageName(title: String): String? = when {
    title.contains("Nyege Nyege", true)           -> "nyege_nyege"
    title.contains("Jazz on the Nile", true)       -> "jazz_on_the_nile"
    title.contains("Afrobeat", true)               -> "afroeat_night"
    title.contains("Reggae Waves", true)           -> "reggae_waves"
    title.contains("Classical Gala", true)         -> "classical_gala"
    title.contains("Gospel Groove", true)          -> "goepel_groove"
    title.contains("Hip Hop Summit", true)         -> "hip_hop_summit"
    title.contains("Indie Soul", true)             -> "indie_soul_session"
    title.contains("Rock in Kampala", true)        -> "rock_in_kampala"
    title.contains("Piano", true)                  -> "piano_and_wine"
    title.contains("Ugandan Startup", true)        -> "ugandan_startup_expo"
    title.contains("Blockchain", true)             -> "blockchain_conference"
    title.contains("AI", true)                     -> "ai_and_robotics_summit"
    title.contains("Fintech", true)                -> "fintech_forum"
    title.contains("Cyber Security", true)
            || title.contains("Cybersecurity", true)   -> "cybersecurity_lab"
    title.contains("EdTech", true)                 -> "edtech_connect"
    title.contains("Cloud Computing", true)        -> "cloud_computing_day"
    title.contains("Developer Fest", true)         -> "developer_fest"
    title.contains("Data Science", true)           -> "data_science_meetup"
    title.contains("IoT", true)                    -> "iot_workshop"
    title.contains("Kampala City Run", true)       -> "kampala_city_run"
    title.contains("Golf", true)                   -> "golf_pro_am"
    title.contains("Rugby", true)                  -> "rugby_7s_series"
    title.contains("Boxing", true)                 -> "boxing_fight_night"
    title.contains("Swimming", true)               -> "swimming_championships"
    title.contains("Cyclists", true)               -> "cyclists_challenge"
    title.contains("Yoga", true)                   -> "yoga_in_the_park"
    title.contains("Inter-University", true)       -> "inter_university_games"
    title.contains("Cricket", true)                -> "cricket_carnival"
    title.contains("E-Sports", true)
            || title.contains("eSports", true)         -> "esports_tournament"
    title.contains("Rolex Festival", true)         -> "rolexfestival"
    title.contains("Restaurant Week", true)        -> "kampala_restuarant_week"
    title.contains("Wine & Cheese", true)
            || title.contains("Wine and Cheese", true) -> "wine_and_cheese_tasting"
    title.contains("Cultural Expo", true)          -> "cultural_expo"
    title.contains("Street Food", true)            -> "street_food_gala"
    title.contains("Oktoberfest", true)            -> "oktoberfest_kampala"
    title.contains("Organic Farmers", true)        -> "organic_farmers__market"
    title.contains("Traditional Dance", true)      -> "traditional_dance_fest"
    title.contains("Coffee Brewers", true)         -> "coffee_brewers_summit"
    title.contains("Seafood Sunday", true)         -> "seafood_sunday"
    else                                           -> null
}
