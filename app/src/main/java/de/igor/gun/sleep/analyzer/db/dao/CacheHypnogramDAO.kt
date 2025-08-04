package de.igor.gun.sleep.analyzer.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import de.igor.gun.sleep.analyzer.db.entities.CacheHypnogram


@Dao
interface CacheHypnogramDAO {
    @Insert
    fun insert(cache: CacheHypnogram): Long

    @Update
    fun update(cache: CacheHypnogram)

    @Delete
    fun delete(cache: CacheHypnogram)

    @Query("delete from CacheHypnogram where series_id = :seriesId")
    fun delete(seriesId: Long)

    @Query("select * from CacheHypnogram where series_id = :seriesId order by id")
    fun get(seriesId: Long): List<CacheHypnogram>

    @Query("select * from CacheHypnogram order by id")
    fun getAll(): List<CacheHypnogram>
}