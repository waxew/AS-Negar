/*
 * AS Team customization layer for AS-Negar.
 * The upstream ReFra package namespace is intentionally preserved during the
 * staged migration so Room schemas and existing internal imports remain stable.
 */
package com.dot.gallery.core.branding

/**
 * هویت مرکزی نگار.
 *
 * مقادیر برند در یک نقطه نگهداری می‌شوند تا Drawer، About، Share و بخش‌های
 * بعدی برنامه از نام‌ها و اطلاعات تماس یکسان استفاده کنند.
 */
object ASBrand {
    const val APP_NAME = "AS-Negar"
    const val APP_NAME_FA = "نگار"
    const val DEVELOPER_NAME = "AS Team Group"
    const val SUPPORT_EMAIL = "AS.Developers.Support@Gmail.Com"
    const val APPLICATION_ID = "com.asteam.negar"
    const val REPOSITORY_URL = "https://github.com/waxew/AS-Negar"

    const val ABOUT_TEXT_FA =
        "نگار، گالری هوشمند AS Team برای مدیریت، مشاهده، دسته‌بندی و ویرایش عکس‌ها و ویدئوها است."

    const val SHARE_TEXT =
        "AS-Negar | نگار - گالری هوشمند عکس و ویدئو از AS Team Group\n$REPOSITORY_URL"
}
