package moe.ganen.konversi.data.database.adapter

import app.cash.sqldelight.ColumnAdapter
import kotlinx.datetime.Instant
import kotlinx.datetime.toInstant

internal object InstantStringColumnAdapter : ColumnAdapter<Instant, String> {
    override fun decode(databaseValue: String): Instant = databaseValue.toInstant()

    override fun encode(value: Instant): String = value.toString()
}
