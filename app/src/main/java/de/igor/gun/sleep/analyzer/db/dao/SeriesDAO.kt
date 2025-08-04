package de.igor.gun.sleep.analyzer.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import de.igor.gun.sleep.analyzer.db.entities.Series


@Dao
interface SeriesDAO {
    @Insert
    fun insert(series: Series): Long

    @Update
    fun update(series: Series)

    @Delete
    fun delete(series: Series)

    @Query("select * from series order by id desc")
    fun getAllDesc(): List<Series>

    @Query("select * from series order by id asc")
    fun getAllAsc(): List<Series>

    @Query("select * from series where id = :id")
    fun get(id: Long): Series
}