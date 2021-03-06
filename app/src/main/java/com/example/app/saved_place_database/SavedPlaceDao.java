package com.example.app.saved_place_database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

/**
 * Dao layer to interact with the database.
 * As Room requires
 */
@Dao
public interface SavedPlaceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertPlace(SavedPlace place);

    @Query("SELECT * from saved_place_table")
    LiveData<List<SavedPlace>> getAll();

    @Delete
    void delete(SavedPlace place);


}
