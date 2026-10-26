package com.miagenda.app

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ManifestPermissionsTest {
    @Test
    fun elManifiestoNoPidePermisos() {
        val manifest = manifestFile().readText()
        assertFalse(manifest.contains("<uses-permission"))
        listOf(
            "android.permission.INTERNET",
            "android.permission.ACCESS_NETWORK_STATE",
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.MANAGE_EXTERNAL_STORAGE",
            "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.ACCESS_COARSE_LOCATION",
            "android.permission.POST_NOTIFICATIONS",
            "android.permission.CAMERA",
            "android.permission.READ_CONTACTS"
        ).forEach { permission ->
            assertFalse(permission, manifest.contains(permission))
        }
    }

    private fun manifestFile(): File {
        val candidates = listOf(
            File("src/main/AndroidManifest.xml"),
            File("app/src/main/AndroidManifest.xml")
        )
        val manifest = candidates.firstOrNull { file -> file.isFile }
        assertTrue(
            "No encontré AndroidManifest.xml desde ${File(".").absolutePath}",
            manifest != null
        )
        return manifest!!
    }
}
