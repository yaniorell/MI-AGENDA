package com.miagenda.app.data.local

import androidx.room.RoomDatabase
import androidx.sqlite.SQLiteConnection
import java.time.LocalDateTime

internal data class DefaultCategory(
    val name: String,
    val colorArgb: Int,
    val iconKey: String
)

internal val defaultCategories: List<DefaultCategory> = listOf(
    DefaultCategory("Universidad", 0xFF7252B8.toInt(), "school"),
    DefaultCategory("Estudio", 0xFF24C5D9.toInt(), "menu_book"),
    DefaultCategory("Proyectos", 0xFFFF829F.toInt(), "folder"),
    DefaultCategory("Programación", 0xFF7AA2FF.toInt(), "code"),
    DefaultCategory("Redes y ciberseguridad", 0xFF3DDC97.toInt(), "shield"),
    DefaultCategory("Ejercicio", 0xFFFFD66B.toInt(), "directions_run"),
    DefaultCategory("Descanso", 0xFFC5C8E8.toInt(), "bedtime"),
    DefaultCategory("Personal", 0xFFF0A3C2.toInt(), "person"),
    DefaultCategory("Otros", 0xFF8E93B8.toInt(), "label")
)

/**
 * Inserta las categorías dentro de la misma creación del archivo.
 * Si la app se cierra justo después, la base ya queda sembrada.
 * Volver a abrirla no vuelve a ejecutar este código.
 */
internal object CategorySeedCallback : RoomDatabase.Callback() {
    override fun onCreate(connection: SQLiteConnection) {
        val now = LocalDateTime.now().toString()
        val sql = """
            INSERT INTO categories (
                name, color_argb, icon_key, sort_order, is_archived, created_at, updated_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()
        defaultCategories.forEachIndexed { index, category ->
            connection.prepare(sql).use { statement ->
                statement.bindText(1, category.name)
                statement.bindLong(2, category.colorArgb.toLong() and 0xFFFFFFFFL)
                statement.bindText(3, category.iconKey)
                statement.bindLong(4, index.toLong())
                statement.bindLong(5, 0L)
                statement.bindText(6, now)
                statement.bindText(7, now)
                statement.step()
            }
        }
    }
}
