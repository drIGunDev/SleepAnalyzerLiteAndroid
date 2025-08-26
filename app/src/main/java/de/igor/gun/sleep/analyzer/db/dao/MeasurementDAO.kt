package de.igor.gun.sleep.analyzer.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import de.igor.gun.sleep.analyzer.db.entities.Measurement
import kotlinx.coroutines.flow.Flow


@Dao
interface MeasurementDAO {
    @Insert
    fun insert(value: Measurement): Long

    @Delete
    fun delete(value: Measurement)

    @Query("select * from measurement where series_id = :seriesId order by id")
    fun get(seriesId: Long): List<Measurement>

    @Query("select * from measurement where series_id = :seriesId order by id limit :limit offset :offset")
    fun get(seriesId: Long, offset: Long, limit: Long): List<Measurement>

    @Query("select count(id) from measurement where series_id = :seriesId")
    fun getCount(seriesId: Long): Long

    @Query("select count(id) from measurement where series_id = :seriesId")
    fun getMeasurementCountAsFlow(seriesId: Long?): Flow<Long>

    @Query("select min(hr) from measurement where series_id = :seriesId and hr > 0")
    fun getMinHR(seriesId: Long): Float?

    @Query("select max(hr) from measurement where series_id = :seriesId and hr > 0")
    fun getMaxHR(seriesId: Long): Float?
}