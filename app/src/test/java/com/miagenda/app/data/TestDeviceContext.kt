@file:Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")

package com.miagenda.app.data

import android.content.Context
import java.io.File

/**
 * Contexto minimo para abrir Room en pruebas de este equipo.
 * Solo resuelve la carpeta de la base. No simula un telefono.
 */
@Suppress("DEPRECATION")
internal class TestDeviceContext(
    private val databaseDirectory: File
) : Context() {
    override fun getDatabasePath(name: String): File {
        val requested = File(name)
        return if (requested.isAbsolute) requested else File(databaseDirectory, name)
    }

    override fun getApplicationContext(): Context = this

    override fun getSystemService(name: String): Any? = null

    override fun bindService(p0: android.content.Intent, p1: android.content.ServiceConnection, p2: Int): Boolean = false

    override fun checkCallingOrSelfPermission(p0: String): Int = 0

    override fun checkCallingOrSelfUriPermission(p0: android.net.Uri, p1: Int): Int = 0

    override fun checkCallingPermission(p0: String): Int = 0

    override fun checkCallingUriPermission(p0: android.net.Uri, p1: Int): Int = 0

    override fun checkPermission(p0: String, p1: Int, p2: Int): Int = 0

    override fun checkSelfPermission(p0: String): Int = 0

    override fun checkUriPermission(p0: android.net.Uri, p1: Int, p2: Int, p3: Int): Int = 0

    override fun checkUriPermission(p0: android.net.Uri?, p1: String?, p2: String?, p3: Int, p4: Int, p5: Int): Int = 0

    override fun clearWallpaper(): Unit {}

    override fun createConfigurationContext(p0: android.content.res.Configuration): android.content.Context? = null

    override fun createContextForSplit(p0: String): android.content.Context? = null

    override fun createDeviceProtectedStorageContext(): android.content.Context? = null

    override fun createDisplayContext(p0: android.view.Display): android.content.Context? = null

    override fun createPackageContext(p0: String, p1: Int): android.content.Context? = null

    override fun databaseList(): Array<String> = emptyArray()

    override fun deleteDatabase(p0: String): Boolean = false

    override fun deleteFile(p0: String): Boolean = false

    override fun deleteSharedPreferences(p0: String): Boolean = false

    override fun enforceCallingOrSelfPermission(p0: String, p1: String?): Unit {}

    override fun enforceCallingOrSelfUriPermission(p0: android.net.Uri, p1: Int, p2: String): Unit {}

    override fun enforceCallingPermission(p0: String, p1: String?): Unit {}

    override fun enforceCallingUriPermission(p0: android.net.Uri, p1: Int, p2: String): Unit {}

    override fun enforcePermission(p0: String, p1: Int, p2: Int, p3: String?): Unit {}

    override fun enforceUriPermission(p0: android.net.Uri, p1: Int, p2: Int, p3: Int, p4: String): Unit {}

    override fun enforceUriPermission(p0: android.net.Uri?, p1: String?, p2: String?, p3: Int, p4: Int, p5: Int, p6: String?): Unit {}

    override fun fileList(): Array<String> = emptyArray()

    override fun getApplicationInfo(): android.content.pm.ApplicationInfo? = null

    override fun getAssets(): android.content.res.AssetManager? = null

    override fun getCacheDir(): File? = null

    override fun getClassLoader(): java.lang.ClassLoader? = null

    override fun getCodeCacheDir(): File? = null

    override fun getContentResolver(): android.content.ContentResolver? = null

    override fun getDataDir(): File? = null

    override fun getDir(p0: String, p1: Int): File? = null

    override fun getExternalCacheDir(): File? = null

    override fun getExternalCacheDirs(): Array<File> = emptyArray()

    override fun getExternalFilesDir(p0: String?): File? = null

    override fun getExternalFilesDirs(p0: String): Array<File> = emptyArray()

    override fun getExternalMediaDirs(): Array<File> = emptyArray()

    override fun getFileStreamPath(p0: String): File? = null

    override fun getFilesDir(): File? = null

    override fun getMainLooper(): android.os.Looper? = null

    override fun getNoBackupFilesDir(): File? = null

    override fun getObbDir(): File? = null

    override fun getObbDirs(): Array<File> = emptyArray()

    override fun getPackageCodePath(): String = ""

    override fun getPackageManager(): android.content.pm.PackageManager? = null

    override fun getPackageName(): String = ""

    override fun getPackageResourcePath(): String = ""

    override fun getResources(): android.content.res.Resources? = null

    override fun getSharedPreferences(p0: String, p1: Int): android.content.SharedPreferences? = null

    override fun getSystemServiceName(p0: Class<*>): String = ""

    override fun getTheme(): android.content.res.Resources.Theme? = null

    override fun getWallpaper(): android.graphics.drawable.Drawable? = null

    override fun getWallpaperDesiredMinimumHeight(): Int = 0

    override fun getWallpaperDesiredMinimumWidth(): Int = 0

    override fun grantUriPermission(p0: String, p1: android.net.Uri, p2: Int): Unit {}

    override fun isDeviceProtectedStorage(): Boolean = false

    override fun moveDatabaseFrom(p0: android.content.Context, p1: String): Boolean = false

    override fun moveSharedPreferencesFrom(p0: android.content.Context, p1: String): Boolean = false

    override fun openFileInput(p0: String): java.io.FileInputStream? = null

    override fun openFileOutput(p0: String, p1: Int): java.io.FileOutputStream? = null

    override fun openOrCreateDatabase(p0: String, p1: Int, p2: android.database.sqlite.SQLiteDatabase.CursorFactory): android.database.sqlite.SQLiteDatabase? = null

    override fun openOrCreateDatabase(p0: String, p1: Int, p2: android.database.sqlite.SQLiteDatabase.CursorFactory, p3: android.database.DatabaseErrorHandler?): android.database.sqlite.SQLiteDatabase? = null

    override fun peekWallpaper(): android.graphics.drawable.Drawable? = null

    override fun registerReceiver(p0: android.content.BroadcastReceiver?, p1: android.content.IntentFilter): android.content.Intent? = null

    override fun registerReceiver(p0: android.content.BroadcastReceiver?, p1: android.content.IntentFilter, p2: Int): android.content.Intent? = null

    override fun registerReceiver(p0: android.content.BroadcastReceiver, p1: android.content.IntentFilter, p2: String?, p3: android.os.Handler?): android.content.Intent? = null

    override fun registerReceiver(p0: android.content.BroadcastReceiver, p1: android.content.IntentFilter, p2: String?, p3: android.os.Handler?, p4: Int): android.content.Intent? = null

    override fun removeStickyBroadcast(p0: android.content.Intent): Unit {}

    override fun removeStickyBroadcastAsUser(p0: android.content.Intent, p1: android.os.UserHandle): Unit {}

    override fun revokeUriPermission(p0: android.net.Uri, p1: Int): Unit {}

    override fun revokeUriPermission(p0: String, p1: android.net.Uri, p2: Int): Unit {}

    override fun sendBroadcast(p0: android.content.Intent): Unit {}

    override fun sendBroadcast(p0: android.content.Intent, p1: String?): Unit {}

    override fun sendBroadcastAsUser(p0: android.content.Intent, p1: android.os.UserHandle): Unit {}

    override fun sendBroadcastAsUser(p0: android.content.Intent, p1: android.os.UserHandle, p2: String?): Unit {}

    override fun sendOrderedBroadcast(p0: android.content.Intent, p1: String?): Unit {}

    override fun sendOrderedBroadcast(p0: android.content.Intent, p1: String?, p2: android.content.BroadcastReceiver?, p3: android.os.Handler?, p4: Int, p5: String?, p6: android.os.Bundle?): Unit {}

    override fun sendOrderedBroadcastAsUser(p0: android.content.Intent, p1: android.os.UserHandle, p2: String?, p3: android.content.BroadcastReceiver, p4: android.os.Handler?, p5: Int, p6: String?, p7: android.os.Bundle?): Unit {}

    override fun sendStickyBroadcast(p0: android.content.Intent): Unit {}

    override fun sendStickyBroadcastAsUser(p0: android.content.Intent, p1: android.os.UserHandle): Unit {}

    override fun sendStickyOrderedBroadcast(p0: android.content.Intent, p1: android.content.BroadcastReceiver, p2: android.os.Handler?, p3: Int, p4: String?, p5: android.os.Bundle?): Unit {}

    override fun sendStickyOrderedBroadcastAsUser(p0: android.content.Intent, p1: android.os.UserHandle, p2: android.content.BroadcastReceiver, p3: android.os.Handler?, p4: Int, p5: String?, p6: android.os.Bundle?): Unit {}

    override fun setTheme(p0: Int): Unit {}

    override fun setWallpaper(p0: android.graphics.Bitmap): Unit {}

    override fun setWallpaper(p0: java.io.InputStream): Unit {}

    override fun startActivities(p0: Array<android.content.Intent>): Unit {}

    override fun startActivities(p0: Array<android.content.Intent>, p1: android.os.Bundle): Unit {}

    override fun startActivity(p0: android.content.Intent): Unit {}

    override fun startActivity(p0: android.content.Intent, p1: android.os.Bundle?): Unit {}

    override fun startForegroundService(p0: android.content.Intent): android.content.ComponentName? = null

    override fun startInstrumentation(p0: android.content.ComponentName, p1: String?, p2: android.os.Bundle?): Boolean = false

    override fun startIntentSender(p0: android.content.IntentSender, p1: android.content.Intent?, p2: Int, p3: Int, p4: Int): Unit {}

    override fun startIntentSender(p0: android.content.IntentSender, p1: android.content.Intent?, p2: Int, p3: Int, p4: Int, p5: android.os.Bundle?): Unit {}

    override fun startService(p0: android.content.Intent): android.content.ComponentName? = null

    override fun stopService(p0: android.content.Intent): Boolean = false

    override fun unbindService(p0: android.content.ServiceConnection): Unit {}

    override fun unregisterReceiver(p0: android.content.BroadcastReceiver): Unit {}
}
