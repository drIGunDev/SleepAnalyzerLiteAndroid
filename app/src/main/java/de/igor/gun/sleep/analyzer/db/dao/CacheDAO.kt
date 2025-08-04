package de.igor.gun.sleep.analyzer.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import de.igor.gun.sleep.analyzer.db.entities.Cache


@Dao
interface CacheDAO {

    @Insert
    fun insert(cache: Cache): Long

    @Update
    fun update(cache: Cache)

    @Delete
    fun delete(cache: Cache)

    @Query("select * from cache where series_id = :seriesId")
    fun get(seriesId: Long): Cache?

    @Query("select * from cache order by series_id desc")
    fun getAllDesc(): List<Cache>

    @Query("select * from cache order by series_id asc")
    fun getAllAsc(): List<Cache>
}