package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AnimationExperienceEntity
import com.example.data.model.AppPreferenceEntity
import com.example.data.model.DesignTemplateEntity
import com.example.data.model.EventEntity
import com.example.data.model.GuestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DesignDao {
  @Query("SELECT * FROM design_templates ORDER BY createdAt DESC")
  fun getAllDesigns(): Flow<List<DesignTemplateEntity>>

  @Query("SELECT * FROM design_templates WHERE id = :id LIMIT 1")
  suspend fun getDesignById(id: String): DesignTemplateEntity?

  @Query("SELECT * FROM design_templates WHERE id = :id LIMIT 1")
  fun getDesignFlow(id: String): Flow<DesignTemplateEntity?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertDesign(design: DesignTemplateEntity)

  @Update
  suspend fun updateDesign(design: DesignTemplateEntity)

  @Query("DELETE FROM design_templates WHERE id = :id")
  suspend fun deleteDesignById(id: String)

  @Query("UPDATE design_templates SET name = :newName WHERE id = :id")
  suspend fun renameDesign(id: String, newName: String)

  @Query("UPDATE design_templates SET isFavorite = :isFav WHERE id = :id")
  suspend fun toggleFavorite(id: String, isFav: Boolean)
}

@Dao
interface AnimationDao {
  @Query("SELECT * FROM animation_experiences ORDER BY createdAt DESC")
  fun getAllAnimations(): Flow<List<AnimationExperienceEntity>>

  @Query("SELECT * FROM animation_experiences WHERE id = :id LIMIT 1")
  suspend fun getAnimationById(id: String): AnimationExperienceEntity?

  @Query("SELECT * FROM animation_experiences WHERE id = :id LIMIT 1")
  fun getAnimationFlow(id: String): Flow<AnimationExperienceEntity?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAnimation(animation: AnimationExperienceEntity)

  @Query("SELECT COUNT(*) FROM animation_experiences")
  suspend fun getAnimationCount(): Int

  @Query("SELECT * FROM animation_experiences")
  suspend fun getAllAnimationsList(): List<AnimationExperienceEntity>

  @Query("UPDATE animation_experiences SET isFavorite = :isFav WHERE id = :id")
  suspend fun toggleFavorite(id: String, isFav: Boolean)

  @Query("DELETE FROM animation_experiences WHERE id = :id")
  suspend fun deleteAnimationById(id: String)
}

@Dao
interface EventDao {
  @Query("SELECT * FROM events ORDER BY createdAt DESC")
  fun getAllEvents(): Flow<List<EventEntity>>

  @Query("SELECT * FROM events WHERE id = :id LIMIT 1")
  suspend fun getEventById(id: String): EventEntity?

  @Query("SELECT * FROM events WHERE id = :id LIMIT 1")
  fun getEventFlow(id: String): Flow<EventEntity?>

  @Query("SELECT * FROM events WHERE designId = :designId")
  suspend fun getEventsUsingDesign(designId: String): List<EventEntity>

  @Query("SELECT * FROM events WHERE animationId = :animationId")
  suspend fun getEventsUsingAnimation(animationId: String): List<EventEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertEvent(event: EventEntity)

  @Update
  suspend fun updateEvent(event: EventEntity)

  @Query("DELETE FROM events WHERE id = :id")
  suspend fun deleteEventById(id: String)
}

@Dao
interface GuestDao {
  @Query("SELECT * FROM guests ORDER BY createdAt DESC")
  fun getAllGuests(): Flow<List<GuestEntity>>

  @Query("SELECT * FROM guests WHERE eventId = :eventId ORDER BY createdAt DESC")
  fun getGuestsForEvent(eventId: String): Flow<List<GuestEntity>>

  @Query("SELECT * FROM guests WHERE eventId = :eventId ORDER BY createdAt DESC")
  suspend fun getGuestsListForEvent(eventId: String): List<GuestEntity>

  @Query("SELECT * FROM guests WHERE uniqueToken = :token LIMIT 1")
  suspend fun getGuestByToken(token: String): GuestEntity?

  @Query("SELECT COUNT(*) FROM guests WHERE eventId = :eventId")
  fun getGuestCountForEvent(eventId: String): Flow<Int>

  @Query("SELECT COUNT(*) FROM guests")
  fun getTotalGuestCount(): Flow<Int>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertGuest(guest: GuestEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertGuests(guests: List<GuestEntity>)

  @Query("UPDATE guests SET uniqueToken = :newToken WHERE id = :id")
  suspend fun regenerateToken(id: String, newToken: String)

  @Query("UPDATE guests SET active = :isActive WHERE id = :id")
  suspend fun setGuestActive(id: String, isActive: Boolean)

  @Query("DELETE FROM guests WHERE id = :id")
  suspend fun deleteGuestById(id: String)

  @Query("DELETE FROM guests WHERE eventId = :eventId")
  suspend fun deleteGuestsForEvent(eventId: String)
}

@Dao
interface PreferenceDao {
  @Query("SELECT value FROM app_preferences WHERE `key` = :key LIMIT 1")
  fun getPreferenceFlow(key: String): Flow<String?>

  @Query("SELECT value FROM app_preferences WHERE `key` = :key LIMIT 1")
  suspend fun getPreference(key: String): String?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun setPreference(pref: AppPreferenceEntity)
}
