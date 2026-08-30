package com.weplay.othello

import android.app.*
import android.content.*
import android.graphics.Color
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.view.*
import android.widget.*
import android.os.Build

class MainActivity: Activity(){
    private val req=7001
    override fun onCreate(b:Bundle?){super.onCreate(b); show()}
    private fun show(){
        val box=LinearLayout(this); box.orientation=LinearLayout.VERTICAL; box.setPadding(28,28,28,28)
        val title=TextView(this); title.text="OTHELLO\nTự quét màn hình"; title.textSize=24f; title.setTextColor(Color.WHITE)
        box.setBackgroundColor(Color.rgb(35,35,35)); box.addView(title)
        val hint=TextView(this); hint.text="\n1. Cho phép hiển thị trên ứng dụng khác.\n2. Bấm BẮT ĐẦU QUÉT.\n3. Quay lại WePlay."; hint.setTextColor(Color.LTGRAY); hint.textSize=15f; box.addView(hint)
        val overlay=Button(this); overlay.text="CẤP QUYỀN MENU"; overlay.setOnClickListener{startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))}; box.addView(overlay)
        val start=Button(this); start.text="BẮT ĐẦU QUÉT"; start.setOnClickListener{startCapture()}; box.addView(start)
        setContentView(box)
    }
    private fun startCapture(){
        val m=getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        startActivityForResult(m.createScreenCaptureIntent(),req)
    }
    override fun onActivityResult(r:Int,c:Int,d:Intent?){super.onActivityResult(r,c,d); if(r==req && c==RESULT_OK && d!=null){
        ContextCompat.startForegroundServiceCompat(this, Intent(this,CaptureService::class.java).apply{putExtra("resultCode",c);putExtra("data",d)})
        finish()
    }}
}
object ContextCompat { fun startForegroundServiceCompat(c:Context,i:Intent){ if(Build.VERSION.SDK_INT>=26)c.startForegroundService(i) else c.startService(i) } }
