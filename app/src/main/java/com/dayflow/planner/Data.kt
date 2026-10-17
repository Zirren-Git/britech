package com.dayflow.planner

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Entity data class Task(@PrimaryKey(autoGenerate=true) val id:Long=0, val title:String, val description:String="", val due:String=LocalDate.now().toString(), val priority:Int=1, val tag:String="", val done:Boolean=false, val recurrence:String="")
@Entity data class Subtask(@PrimaryKey(autoGenerate=true) val id:Long=0, val taskId:Long, val title:String, val done:Boolean=false)
@Entity data class Habit(@PrimaryKey(autoGenerate=true) val id:Long=0,val title:String,val weekdays:String="1234567")
@Entity data class HabitCheckIn(@PrimaryKey(autoGenerate=true) val id:Long=0,val habitId:Long,val date:String=LocalDate.now().toString())
@Entity data class Note(@PrimaryKey(autoGenerate=true) val id:Long=0,val title:String,val body:String="",val tags:String="",val pinned:Boolean=false)
@Entity data class DiaryEntry(@PrimaryKey val date:String=LocalDate.now().toString(),val mood:Int,val text:String)
@Entity data class Goal(@PrimaryKey(autoGenerate=true) val id:Long=0,val title:String,val deadline:String="",val progress:Int=0)
@Entity data class Tag(@PrimaryKey val name:String)

@Dao interface PlannerDao {
 @Query("SELECT * FROM Task ORDER BY due, id DESC") fun tasks():Flow<List<Task>>
 @Query("SELECT * FROM Habit ORDER BY id DESC") fun habits():Flow<List<Habit>>
 @Query("SELECT * FROM Note ORDER BY pinned DESC, id DESC") fun notes():Flow<List<Note>>
 @Query("SELECT * FROM Goal ORDER BY id DESC") fun goals():Flow<List<Goal>>
 @Query("SELECT * FROM DiaryEntry ORDER BY date DESC") fun diary():Flow<List<DiaryEntry>>
 @Query("SELECT * FROM HabitCheckIn") fun checkins():Flow<List<HabitCheckIn>>
 @Insert suspend fun task(value:Task); @Update suspend fun updateTask(value:Task); @Delete suspend fun deleteTask(value:Task)
 @Insert suspend fun habit(value:Habit); @Insert suspend fun checkin(value:HabitCheckIn); @Insert suspend fun note(value:Note); @Insert suspend fun goal(value:Goal); @Update suspend fun updateGoal(value:Goal); @Insert(onConflict=OnConflictStrategy.REPLACE) suspend fun diary(value:DiaryEntry)
 @Query("DELETE FROM Task") suspend fun clearTasks(); @Query("DELETE FROM Habit") suspend fun clearHabits(); @Query("DELETE FROM Note") suspend fun clearNotes(); @Query("DELETE FROM Goal") suspend fun clearGoals(); @Query("DELETE FROM DiaryEntry") suspend fun clearDiary(); @Query("DELETE FROM HabitCheckIn") suspend fun clearChecks()
}
@Database(entities=[Task::class,Subtask::class,Habit::class,HabitCheckIn::class,Note::class,DiaryEntry::class,Goal::class,Tag::class],version=1,exportSchema=false)
abstract class PlannerDatabase:RoomDatabase(){abstract fun dao():PlannerDao; companion object { @Volatile private var instance:PlannerDatabase?=null; fun get(context:Context)=instance?: synchronized(this){instance?:Room.databaseBuilder(context.applicationContext,PlannerDatabase::class.java,"dayflow.db").build().also{instance=it}} }}
