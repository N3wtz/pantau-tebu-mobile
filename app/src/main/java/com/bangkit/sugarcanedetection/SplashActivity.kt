package com.bangkit.sugarcanedetection

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import android.os.Handler
import android.content.Intent

class SplashActivity : AppCompatActivity() {
    private lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Inisialisasi imageView
        imageView = findViewById(R.id.imageView)

        // Memulai animasi saat aktivitas dibuat
        playAnimation()

        // Menunggu beberapa detik dan kemudian memindahkan ke MainActivity
        Handler().postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()  // Menutup SplashActivity agar tidak kembali ke aktivitas ini
        }, 3000)  // Waktu tunggu dalam milidetik (3000 ms = 3 detik)
    }

    private fun playAnimation() {
        // Animasi untuk ImageView dengan pergerakan horizontal
        ObjectAnimator.ofFloat(imageView, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 2500
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()
    }
}
