package com.example.mp3_db

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mp3_db.databinding.ActivityMainBinding
import kotlin.time.Duration

class MainActivity : AppCompatActivity() {
    companion object{
        val REQ_READ = 99
        val DB_NAME = "musicDB"
        var VERSION = 1
    }
    lateinit var binding: ActivityMainBinding
    lateinit var adapter: MusicRecyclerAdapter
    private var musicList: MutableList<Music>? = mutableListOf<Music>()
    //승인받을 permission 항목 요청
    val permissions = arrayOf((android.Manifest.permission.READ_EXTERNAL_STORAGE))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //승인되었는지 점검
        if(isPermitted()){
            startProcess()
        }else{
            ActivityCompat.requestPermissions(this, permissions, REQ_READ)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == REQ_READ && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            startProcess()
        }else{
            Toast.makeText(this, "권한을 허용하셔야만 앱을 실행하실 수 있습니다", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun startProcess() {
//        var musicList: MutableList<Music>? = mutableListOf<Music>()
        //먼저 데이터베이스에서 음원정보를 가져온다. 없다면 그 때 공유메모리에서 음원정보를 가져온다
        val dbHelper = DBHelper(this, MainActivity.DB_NAME, MainActivity.VERSION)
        musicList = dbHelper.selectMusicAll()

        //만약 데이터베이스에 음원정보가 없다면 contentResolver를 통해 직접 공유메모리에서 가져온다
        if(musicList == null){ //데이터베이스의 음원정보가 null이면
            val playMusicList = getMusicList() // 외장메모리를 받을 새로운 뮤직리스트를 생성함
            if(playMusicList != null){ //외장메모리에 음원정보가 있다면(null이 아니면)
                for(i in 0..playMusicList.size -1){ //음원리스트가 0번째부터 전체 -1까지
                    val music = playMusicList.get(i)
                    dbHelper.insertMusic(music)
                }
                musicList = playMusicList //비어있는 뮤직리시트에 외장메모리가 들어있는 playMusicList를 넣어준다
            }else{//외장메모리에 음원정보가 없다면 (null이면)
                Log.d("mp3_db", "MainActivity.startProcess() 외장메모리에 음원파일이 없음")
            }
        }
        //리사이클러 뷰에 제공한다.
        adapter = MusicRecyclerAdapter(this, musicList)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
    }

    //공유메모리에서 직접 음원정보를 가져오는 함수 모듈화
    private fun getMusicList(): MutableList<Music>? {
        var imsimusicList: MutableList<Music>? = mutableListOf<Music>()
        //공유메모리에서 음원정보 가져오기
        val musicURL = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        //음원정보를 가져올 배열
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION
        )
        // 공유메모리를 지정한곳(musicURL)에 projection을 가져와서 contentResolver를 통해 커서에 가져온다
        val cursor = contentResolver.query(musicURL, projection, null, null, null)
        if(cursor?.count!! > 0){
            while (cursor!!.moveToNext()){
                val id = cursor.getString(0)
                val title = cursor.getString(1).replace("'","")
                val artist = cursor.getString(2).replace("'","")
                val albumId = cursor.getString(3)
                val duration = cursor.getInt(4)
                val music = Music(id, title, artist, albumId, duration, 0, 0)
                imsimusicList?.add(music)// musicList에 music에 대한 정보들을 집어넣는다
            }
        }else{
            imsimusicList = null
        }
        return imsimusicList
    }

    //현재 사용하는 앱이 외부저장소를 읽을 권한이 있는지 체크
    fun isPermitted(): Boolean{
        if(ContextCompat.checkSelfPermission(this, permissions[0]) == PackageManager.PERMISSION_GRANTED){
            return true
        }else{
            return false
        }
    }

    //액션바 메뉴
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)

        //메뉴에서 서치항목 찾음
        val searchMenu = menu?.findItem(R.id.menuSearch)
        val searchView = searchMenu?.actionView as SearchView

        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }
            //한글자 칠 때마다 검색기능이 발생함
            override fun onQueryTextChange(query: String?): Boolean {
                val dbHelper = DBHelper(applicationContext, MainActivity.DB_NAME, MainActivity.VERSION)
                if(query.isNullOrBlank()){ //검색창에 아무런 값이 없거나 빈칸일 경우
                    musicList?.clear() // 뮤직리스트를 싹 지운다.
                    dbHelper.selectMusicAll()?.let{musicList?.addAll(it)} //모든 뮤직리스트를 가져온다. null상태면 모든 리스트를 추가한다.
                    adapter.notifyDataSetChanged() // 어댑터에 위 데이터를 다시 새로 넣는다
                }else{
                    musicList?.clear() // 뮤직리스트를 싹 지운다.
                    dbHelper.searchMusic(query)?.let{musicList?.addAll(it)}
                    adapter.notifyDataSetChanged()
                }
                return true
            }
        })
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val dbHelper = DBHelper(applicationContext, MainActivity.DB_NAME, MainActivity.VERSION)

        when(item.itemId){
            R.id.menuLikes ->{
                musicList?.clear() // 뮤직리스트를 싹 지운다.
                dbHelper.selectMusiclike()?.let{musicList?.addAll(it)}
                adapter.notifyDataSetChanged()
            }
            R.id.menuAllList ->{
                musicList?.clear() // 뮤직리스트를 싹 지운다.
                dbHelper.selectMusicAll()?.let{musicList?.addAll(it)}
                adapter.notifyDataSetChanged()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}