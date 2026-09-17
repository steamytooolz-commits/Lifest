package com.example.presentation.navigation

object Routes {
    const val SPLASH = "splash"
    const val AUTH = "auth"
    const val MAIN_MENU = "main_menu"
    const val NEW_LIFE = "new_life"
    const val GAME = "game"
    const val DIALOGUE = "dialogue/{npcId}"
    const val STORE = "store"
    const val REDEEM_COUPON = "redeem_coupon"
    const val SETTINGS = "settings"
    const val LIFE_SUMMARY = "life_summary"
    const val HIDDEN_ADMIN = "hidden_admin"

    fun dialogue(npcId: String): String = "dialogue/$npcId"
}
