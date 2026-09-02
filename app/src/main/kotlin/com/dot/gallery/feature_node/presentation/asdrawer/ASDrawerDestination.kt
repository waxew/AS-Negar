package com.dot.gallery.feature_node.presentation.asdrawer

/**
 * مسیرهای پایه منوی همبرگری AS Team.
 *
 * این قرارداد مستقل است تا همه پروژه‌های AS از ساختار یکسان Drawer استفاده کنند.
 */
enum class ASDrawerDestination(val route: String) {
    HOME("timeline_screen"),
    SETTINGS("settings_screen"),
    SHARE("as_share"),
    ABOUT("as_about"),
    CONTACT("as_contact")
}
