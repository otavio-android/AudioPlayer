package com.talentica.androidkotlin.audioplayer

import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.PowerManager
import android.view.View
import android.view.View.OnClickListener
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.audioplayer.SeekBarHandler


class MainActivity : AppCompatActivity(), SeekBar.OnSeekBarChangeListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener,
    MediaPlayer.OnPreparedListener, MediaPlayer.OnSeekCompleteListener, OnClickListener {


    var mMediaPlayer: MediaPlayer? = null
    var mPlayPauseButton:ImageButton? = null
    var mSeekbar:SeekBar? = null
    var mTimer:TextView? = null
    var musica_mostrada:TextView? = null
    var seekBarHandler: SeekBarHandler? = null
    var playlist: Button? =  null
    var reproduçao_btn:Switch? = null
    var reproduçaoOn = 0
    var index_next_music = 0
    var escolha_manual = 0
    var musicas = arrayOf(R.raw.jm1.toString(),R.raw.simplerock.toString(),R.raw.jm2.toString(),R.raw.jm3.toString(),R.raw.jm4.toString())
    var musicas_mostradas = arrayOf("jm1","simplerock","jm2","jm3","jm4")
    // A primeira musica a ser tocada se o usuario der o play e a primeira de musicas_mostradas
    var select_music = R.raw.jm1
    var music_title:String? = null
    var codigo_request = 99

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tela1)
        musica_mostrada = findViewById<TextView>(R.id.song_artist)
        mSeekbar = findViewById<SeekBar>(R.id.progressbar)
        mSeekbar?.setOnSeekBarChangeListener(this)
        mPlayPauseButton = findViewById<ImageButton>(R.id.play_pause_btn)
        mPlayPauseButton?.setOnClickListener(this)
        mTimer = findViewById<TextView>(R.id.tv_progress)
        playlist = findViewById<Button>(R.id.button1)
        reproduçao_btn = findViewById<Switch>(R.id.switch2)
        if(ActivityCompat.checkSelfPermission
                (this,android.Manifest.permission.WAKE_LOCK)!= PackageManager.PERMISSION_GRANTED)
        {  ActivityCompat.requestPermissions(this@MainActivity,
            arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE ),codigo_request)}
        else{

        }

        alerta()
        reproduçao_automatica()
    }

    override fun onStart() {
        super.onStart()
                            }

        fun reproduçao_automatica(){
            reproduçao_btn?.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked)
                 {Toast.makeText(this@MainActivity,"Reproduçao automatica ativada",
                    Toast.LENGTH_SHORT).show()
                reproduçaoOn = 1}

                else reproduçaoOn = 0
            }
        }

        fun alerta(){

            playlist?.setOnClickListener(){
                makealerta()
            }
        }


  fun makealerta(){

      var dialogo = AlertDialog.Builder(this@MainActivity)
      dialogo.setTitle("Playlist de musica")
      dialogo.setItems(musicas_mostradas) { dialog, which ->

          select_music = musicas[which].toInt()
          index_next_music = which
          escolha_manual = 1
          onCompletion(mMediaPlayer)
      }
      var mostrar = dialogo.create()
      mostrar.show()
  }

    override fun onClick(v: View?) {
        if (v?.id == R.id.play_pause_btn) {
            musica_mostrada?.setText("teste")
            togglePlayback()
        }

    }

    fun togglePlayback() {
        if (mMediaPlayer?.isPlaying == true) {

           pauseAudio()
        } else {
            createMediaPlayerIfNeeded()
            playAudio()
        }
    }


    private fun createMediaPlayerIfNeeded() {
        // As linhas abaixo vao definir qual musica vai ser tocada
          index_next_music = if(escolha_manual==1 || reproduçaoOn==0) index_next_music else index_next_music+1
        // A linha abaixo verifica se a musica que esta tocando e a ultima da playlist se for
        //a ultima  a proxima musica tocada sera a primeira da playlist
          index_next_music = if(index_next_music==musicas.size) 0 else index_next_music
          select_music = if(escolha_manual==1) select_music else musicas[index_next_music].toInt()

          var contador = 0
          escolha_manual = 0
          var musica_escolhida = musicas.iterator()
          for (m in musica_escolhida){

              if(m==select_music.toString()){
                  music_title = musicas_mostradas[contador]
                  musica_mostrada?.setText(music_title)

              }
              else {
                  contador+=1
              }
          }

        if (mMediaPlayer == null) {
            mMediaPlayer = MediaPlayer.create(this,select_music)
            mMediaPlayer?.setWakeMode(this.getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK)
            mMediaPlayer?.setOnPreparedListener(this)
            mMediaPlayer?.setOnCompletionListener(this)
            mMediaPlayer?.setOnErrorListener(this)
            mMediaPlayer?.setOnSeekCompleteListener(this)

        }

    }

    fun playAudio() {
        mMediaPlayer?.start()

        seekBarHandler = SeekBarHandler(mSeekbar, mMediaPlayer, isViewOn = true, timer = mTimer!!)
        seekBarHandler?.execute()
        val pauseDrawabale = ContextCompat.getDrawable(this, android.R.drawable.ic_media_pause)
        mPlayPauseButton?.setImageDrawable(pauseDrawabale)
    }

    fun pauseAudio() {
        seekBarHandler?.cancel(true)
        mMediaPlayer?.pause()
        val playDrawabale = ContextCompat.getDrawable(this, android.R.drawable.ic_media_play)
        mPlayPauseButton?.setImageDrawable(playDrawabale)
    }


    private fun relaxResources(releaseMediaPlayer: Boolean) {

        seekBarHandler?.cancel(true)
        seekBarHandler = null
        if (releaseMediaPlayer && mMediaPlayer != null) {
            mMediaPlayer?.reset()
            mMediaPlayer?.release()
            mMediaPlayer = null
        }
    }

    override fun onPause() {
        super.onPause()
        if (mMediaPlayer?.isPlaying == true) {
            pauseAudio()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        select_music = R.raw.jm1
        relaxResources(true)
    }

    override fun onCompletion(mp: MediaPlayer?) {
        relaxResources(true)
        val playDrawabale = ContextCompat.getDrawable(this, android.R.drawable.ic_media_play)
        mPlayPauseButton?.setImageDrawable(playDrawabale)
        mSeekbar?.progress = 0
        mMediaPlayer = null
        var stop_reproduçao = if(reproduçaoOn==1||escolha_manual==1) togglePlayback() else null

    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        return false
    }

    override fun onPrepared(mp: MediaPlayer?) {
    }

    override fun onSeekComplete(mp: MediaPlayer?) {

    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        if (fromUser) {
            val progress = seekBar?.progress
            mMediaPlayer?.seekTo(progress!!)
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
    }
}

