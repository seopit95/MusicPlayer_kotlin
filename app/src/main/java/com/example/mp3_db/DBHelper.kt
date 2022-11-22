package com.example.mp3_db

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DBHelper(context: Context, dbName: String, version: Int):SQLiteOpenHelper(context, dbName, null, version) {
    override fun onCreate(db: SQLiteDatabase?) {
        val query = """
            create table musicTBL(
            id text primary key,
            title text not null,
            artist text not null,
            albumId text not null,
            duration integer not null,
            likes integer,
            repeat integer)
        """.trimIndent()
        db?.execSQL(query)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        val query = """
            drop table musicTBL
        """.trimIndent()
        db?.execSQL(query)
        this.onCreate(db)
    }

    fun selectMusicAll(): MutableList<Music>? {
        var musicList: MutableList<Music>? = mutableListOf<Music>()
        var cursor: Cursor? = null
        val query = """
            select * from musicTBL
        """.trimIndent()
        val db = this.readableDatabase

        try {
            cursor = db.rawQuery(query, null)
            if(cursor.count > 0){
                while(cursor.moveToNext()){
                    val id = cursor.getString(0)
                    val title = cursor.getString(1)
                    val artist = cursor.getString(2)
                    val albumID = cursor.getString(3)
                    val duration = cursor.getInt(4)
                    val likes = cursor.getInt(5)
                    val music = Music(id, title, artist, albumID,duration,likes)
                    musicList?.add(music)
                }
            }else{
                musicList = null
            }
        }catch (e : Exception){
            Log.d("mp3_db", "DBHelper.selectMusicAll() ${e.printStackTrace()}")
            musicList = null
        }finally {
            cursor?.close()
            db.close()
        }
        return musicList

    }

    fun insertMusic(music: Music): Boolean {
        var flag = false
        val query = """
            insert into musicTBL(id, title, artist, albumId, duration, likes)
            values ('${music.id}', '${music.title}', '${music.artist}', '${music.albumId}', ${music.duration}, ${music.likes}) 
        """.trimIndent() 
        val db = this.writableDatabase

        try {
            db.execSQL(query)
            flag = true
        }catch (e : Exception){
            Log.d("mp3_db", "DBHelper.inserMusic() ${e.printStackTrace()}")
            flag = false
        }finally {
            db.close()
        }
        return flag
    }

    fun updatelike(music: Music): Boolean {
        var flag = false
        val query = """
            update musicTBL set likes = ${music.likes} where id = '${music.id}'
        """.trimIndent()
        val db = this.writableDatabase

        try {
            db.execSQL(query)
            flag = true
        }catch (e : Exception){
            Log.d("mp3_db", "DBHelper.updatelike() ${e.printStackTrace()}")
            flag = false
        }finally {
            db.close()
        }
        return flag
    }

    fun searchMusic(query: String?): MutableList<Music>? { // MutableList<Music>? = 리턴값
        var musicList: MutableList<Music>? = mutableListOf<Music>()
        var cursor: Cursor? = null
        val query = """
            select * from musicTBL where title like '${query}%' or artist like '${query}%'  
                    """.trimIndent() // like = 유사한, % = 포함
        val db = this.readableDatabase

        try {
            cursor = db.rawQuery(query, null)
            if(cursor.count > 0){
                while(cursor.moveToNext()){
                    val id = cursor.getString(0)
                    val title = cursor.getString(1)
                    val artist = cursor.getString(2)
                    val albumID = cursor.getString(3)
                    val duration = cursor.getInt(4)
                    val likes = cursor.getInt(5)
                    val music = Music(id, title, artist, albumID,duration,likes)
                    musicList?.add(music)
                }
            }else{
                musicList = null
            }
        }catch (e : Exception){
            Log.d("mp3_db", "DBHelper.searchMusic() ${e.printStackTrace()}")
            musicList = null
        }finally {
            cursor?.close()
            db.close()
        }
        return musicList
    }

    fun selectMusiclike(): MutableList<Music>? {
        var musicList: MutableList<Music>? = mutableListOf<Music>()
        var cursor: Cursor? = null
        val query = """
            select * from musicTBL where likes = 1
        """.trimIndent()
        val db = this.readableDatabase

        try {
            cursor = db.rawQuery(query, null)
            if(cursor.count > 0){
                while(cursor.moveToNext()){
                    val id = cursor.getString(0)
                    val title = cursor.getString(1)
                    val artist = cursor.getString(2)
                    val albumID = cursor.getString(3)
                    val duration = cursor.getInt(4)
                    val likes = cursor.getInt(5)
                    val music = Music(id, title, artist, albumID,duration,likes)
                    musicList?.add(music)
                }
            }else{
                musicList = null
            }
        }catch (e : Exception){
            Log.d("mp3_db", "DBHelper.selectMusiclike() ${e.printStackTrace()}")
            musicList = null
        }finally {
            cursor?.close()
            db.close()
        }
        return musicList
    }
}