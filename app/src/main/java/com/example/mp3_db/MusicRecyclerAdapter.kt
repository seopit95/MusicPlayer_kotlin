package com.example.mp3_db

import android.content.Context
import android.content.Intent
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mp3_db.databinding.ItemRecyclerviewBinding
import java.text.SimpleDateFormat

class MusicRecyclerAdapter(val context: Context, val musicList: MutableList<Music>?):RecyclerView.Adapter<MusicRecyclerAdapter.CustomViewHolder>() {
    //정적멤버상수 (=final)
    companion object{
        val ALBUM_SIZE = 300
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = ItemRecyclerviewBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return CustomViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val binding = (holder as CustomViewHolder).binding
        val music = musicList?.get(position)
        binding.tvArtist.text = music?.artist
        binding.tvTitle.text = music?.title
        binding.tvDuration.text = SimpleDateFormat("mm:ss").format(music?.duration)
        val bitmap = music?.getAlbumImage(context, ALBUM_SIZE)
        if(bitmap != null){
            binding.ivAlbumImage.setImageBitmap(bitmap)
        }else{
            binding.ivAlbumImage.setImageResource(R.drawable.albumpicture)
        }
        when(music?.likes){
            0 -> binding.ivlike.setImageResource(R.drawable.emptylike)
            1 -> binding.ivlike.setImageResource(R.drawable.fulllike)
        }

        //이벤트처리
        binding.root.setOnClickListener{
            val playList: ArrayList<Parcelable>? = musicList as ArrayList<Parcelable>
            val intent = Intent(binding.root.context, MusicPlayerActivity::class.java)
            intent.putExtra("playList", playList)
            intent.putExtra("position", position)
            //intent.putExtra("music", music) 음악을 하나만 주는 것 (그래서 앞,뒤로가기가 안됨)
            binding.root.context.startActivity(intent)
        }
        //이벤트처리 좋아요 눌렀을 때 데이터베이스에 좋아요가 등록되어야됨.
        binding.ivlike.setOnClickListener{
            if(music?.likes == 0){
                binding.ivlike.setImageResource(R.drawable.fulllike)
                music?.likes = 1
            }else {
                binding.ivlike.setImageResource(R.drawable.emptylike)
                music?.likes = 0
            }
            if(music != null){
                val dbHelper = DBHelper(context, MainActivity.DB_NAME, MainActivity.VERSION)
                var flag = dbHelper.updatelike(music)
                if(flag == false){
                    Log.d("mp3_db", "MusicRecyclerAdapter.onBindViewHolder() :업데이트 실패 ${music.toString()}")
                }else{
                    notifyDataSetChanged() // flag가 true면 업데이트한 값으로 데이터를 설정한다.
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return musicList?.size?:0
    }

    class CustomViewHolder(val binding: ItemRecyclerviewBinding):RecyclerView.ViewHolder(binding.root){
    }
}