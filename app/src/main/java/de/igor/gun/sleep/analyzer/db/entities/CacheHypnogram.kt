package de.igor.gun.sleep.analyzer.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import de.igor.gun.sleep.analyzer.repositories.tools.HypnogramHolder
import java.time.LocalDateTime


@Entity(
    tableName = "CacheHypnogram",
    foreignKeys = [
        ForeignKey(
            entity = Series::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("series_id"),
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["series_id"])
    ]
)
class CacheHypnogram(
    @ColumnInfo(name = "sleep_state") var sleepState: HypnogramHolder.SleepState,
    @ColumnInfo(name = "time") var startTime: LocalDateTime,
    @ColumnInfo(name = "series_id") var seriesId: Long,
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
)