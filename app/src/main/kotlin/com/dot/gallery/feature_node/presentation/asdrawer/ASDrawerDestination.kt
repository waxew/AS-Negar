package com.dot.gallery.feature_node.presentation.asdrawer

/**
 * مسیرهای پایه منوی همبرگری AS Team.
 *
 * ترتیب enum نیز با استاندارد مشترک AS Team هماهنگ است:
 * Settings در index 0 و Share در index 1 قرار دارد.
 */
enum class ASDrawerDestination(val route: String) {
    SETTINGS("settings_screen"),
    SHARE("as_share"),
    HOME("timeline_screen"),
    ABOUT("as_about"),
    CONTACT("as_contact")
}
