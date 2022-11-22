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

        playList = intent.getParcelableArrayListExtra("playList")
        position = intent.getIntExtra("position",0)
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

        var likeImage = music?.likes
        if(likeImage == 0){
            binding.ivPlayerlike.setImageResource(R.drawable.emptylike)
        }else{
            binding.ivPlayerlike.setImageResource(R.drawable.fulllike)
        }

        mediaPlayer = MediaPlayer.create(this, music?.getMusicUri())

        binding.seekBar.max = mediaPlayer!!.duration
        binding.seekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if(fromUser) {
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

        binding.ivPlayerList.setOnClickListener{
            mediaPlayer?.stop()
            messengerJob?.cancel()
            finish()
        }

        binding.ivPlayBefore.setOnClickListener{
            if(mediaPlayer!!.isPlaying()){
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
                mediaPlayer?.start()
                binding.ivPlayerRepeat.setImageResource(R.drawable.playerrepeat)
                binding.ivPlay.setImageResource(R.drawable.playerpause)
                seekBarCoroutin()
            }
        }

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