package com.example.mp3_db

import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.widget.SeekBar
import com.example.mp3_db.databinding.ActivityMusicPlayerBinding
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import kotlin.concurrent.timer

class MusicPlayerActivity : AppCompatActivity() {
    companion object{
        val ALBUM_SIZE = 300
    }
    private lateinit var binding: ActivityMusicPlayerBinding
    private var playList: MutableList<Parcelable>? = null
    private var position: Int = 0
    private var music: Music? = null
    private var mediaPlayer: MediaPlayer? = null
    private var messengerJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMusicPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //인텐트 데이터 가져오기
        playList = intent.getParcelableArrayListExtra("playList")
        position = intent.getIntExtra("position",0)
        music = playList?.get(position) as Music

        //화면에 바인딩
        binding.tvPlayerTitle.text = music?.title
        binding.tvPlayerArtist.text = music?.artist
        //해당 음원의 총 노래시간
        binding.tvTotalDuration.text = SimpleDateFormat("mm:ss").format(music?.duration)
        binding.tvPlayerDuration.text = "00:00"
        val bitmap = music?.getAlbumImage(this, ALBUM_SIZE)
        if(bitmap != null){
            binding.ivPlayerAlbum.setImageBitmap(bitmap)
        }else{
            binding.ivPlayerAlbum.setImageResource(R.drawable.albumpicture)
        }

        // 리사이클러뷰에서 '좋아요'가 선택된 음원의 정보를 불러오기
        var likeImage = music?.likes
        if(likeImage == 0){
            binding.ivPlayerlike.setImageResource(R.drawable.emptylike)
        }else{
            binding.ivPlayerlike.setImageResource(R.drawable.fulllike)
        }

        //음악등록
        mediaPlayer = MediaPlayer.create(this, music?.getMusicUri())

        //시크바 음악재생위치 변경
        binding.seekBar.max = mediaPlayer!!.duration
        binding.seekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if(fromUser) {//사용자로 부터 컨트롤 된다면
                    mediaPlayer?.seekTo(progress)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                Log.d("mp3_db", "seekBar 작동할 때 호출")
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                Log.d("mp3_db", "seekBar 정지할 때 호출")
            }
        })
        //이벤트 설정(재생목록)
        binding.ivPlayerList.setOnClickListener{
            mediaPlayer?.stop()
            messengerJob?.cancel()
            finish()
        }
        //이벤트 설정(이전곡)
        binding.ivPlayBefore.setOnClickListener{
            if(mediaPlayer!!.isPlaying()){ // 음원이 재생중이라면 정지,리셋효과
                mediaPlayer?.stop()
                messengerJob?.cancel()
                mediaPlayer = MediaPlayer.create(this, music?.getMusicUri())
                binding.seekBar.progress = 0
                binding.tvPlayerDuration.text = "00:00"
            }else if(position > 0){
                --position
                music = playList?.get(position) as Music
                binding.tvPlayerTitle.text = music?.title
                binding.tvPlayerArtist.text = music?.artist
                binding.tvTotalDuration.text = SimpleDateFormat("mm:ss").format(music?.duration)
                binding.tvPlayerDuration.text = "00:00"

                val bitmap = music?.getAlbumImage(this, ALBUM_SIZE)
                if(bitmap != null){
                    binding.ivPlayerAlbum.setImageBitmap(bitmap)
                }else{
                    binding.ivPlayerAlbum.setImageResource(R.drawable.albumpicture)
                }
                mediaPlayer = MediaPlayer.create(this, music?.getMusicUri())
                //자동실행
                mediaPlayer?.start()
                binding.ivPlayerRepeat.setImageResource(R.drawable.playerrepeat)
                binding.ivPlay.setImageResource(R.drawable.playerpause)
                //시크바 코루틴 적용
                seekBarCoroutin()
            }
        }
        //이벤트 설정(다음곡)
        binding.ivPlayNext.setOnClickListener{
            if(mediaPlayer!!.isPlaying || position >= 0){
                mediaPlayer?.stop()
                messengerJob?.cancel()
                position++
                music = playList?.get(position) as Music
                binding.tvPlayerTitle.text = music?.title
                binding.tvPlayerArtist.text = music?.artist
                binding.tvTotalDuration.text = SimpleDateFormat("mm:ss").format(music?.duration)
                binding.tvPlayerDuration.text = "00:00"

                val bitmap = music?.getAlbumImage(this, ALBUM_SIZE)
                if(bitmap != null){
                    binding.ivPlayerAlbum.setImageBitmap(bitmap)
                }else{
                    binding.ivPlayerAlbum.setImageResource(R.drawable.albumpicture)
                }
                mediaPlayer = MediaPlayer.create(this, music?.getMusicUri())
                mediaPlayer?.start()
                binding.ivPlayerRepeat.setImageResource(R.drawable.playerrepeat)
                binding.ivPlay.setImageResource(R.drawable.playerpause)
                seekBarCoroutin()
            }
        }
        //이벤트 설정(플레이 버튼)
        binding.ivPlay.setOnClickListener{
            if(mediaPlayer?.isPlaying == true){
                mediaPlayer?.pause()
                binding.ivPlay.setImageResource(R.drawable.playerplay)
            }else{
                mediaPlayer?.start()
                binding.ivPlay.setImageResource(R.drawable.playerpause)
                seekBarCoroutin()
            }
        }
        //한곡 반복
        binding.ivPlayerRepeat.setOnClickListener{
            when(music?.repeat){
                0 -> binding.ivPlayerRepeat.setImageResource(R.drawable.playerrepeat)
                1 -> binding.ivPlayerRepeat.setImageResource(R.drawable.playeronerepeat)
            }

            if(music?.repeat == 0){
                binding.ivPlayerRepeat.setImageResource(R.drawable.playerrepeat)
                mediaPlayer?.isLooping = false
                music?.repeat = 1
            }else{
                binding.ivPlayerRepeat.setImageResource(R.drawable.playeronerepeat)
                mediaPlayer?.isLooping = true
                music?.repeat = 0
            }
            seekBarCoroutin()
        }
    }
    fun seekBarCoroutin(){
        val backgroudScope = CoroutineScope(Dispatchers.Default + Job())
        messengerJob = backgroudScope.launch {
            while (mediaPlayer?.isPlaying == true){
                runOnUiThread{
                    val currentPosition = mediaPlayer?.currentPosition!!
                    binding.seekBar.progress = currentPosition
                    val currentDuration = SimpleDateFormat("mm:ss").format(mediaPlayer?.currentPosition)
                    binding.tvPlayerDuration.text = currentDuration
                }
                try{
                    delay(100)
                }catch(e:Exception){
                    Log.d("mp3_db", "Thread 오류 발생")
                }
            }
            runOnUiThread{
                if(mediaPlayer!!.currentPosition >= (binding.seekBar.max -1000)){
                    binding.seekBar.progress = 0
                    binding.tvPlayerDuration.text = "00:00"
                }
                binding.ivPlay.setImageResource(R.drawable.playerplay)
            }
        }
    }
    override fun onBackPressed() {
        super.onBackPressed()
        mediaPlayer?.stop()
        messengerJob?.cancel()
        finish()
    }
}