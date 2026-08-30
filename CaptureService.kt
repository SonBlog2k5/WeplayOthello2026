package com.weplay.othello

import android.app.*
import android.content.*
import android.graphics.*
import android.hardware.display.DisplayManager
import android.media.*
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.*
import android.view.*

class CaptureService: Service(){
    private var projection:MediaProjection?=null; private var reader:ImageReader?=null; private var overlay:HintOverlay?=null
    override fun onStartCommand(i:Intent?,flags:Int,id:Int):Int{
        createChannel(); startForeground(11,Notification.Builder(this,"scan").setContentTitle("Othello đang quét").setSmallIcon(android.R.drawable.ic_menu_search).build())
        val code=i?.getIntExtra("resultCode",0)?:return START_NOT_STICKY
        val data=i.getParcelableExtra<Intent>("data")?:return START_NOT_STICKY
        val pm=getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        projection=pm.getMediaProjection(code,data)
        val dm=resources.displayMetrics; reader=ImageReader.newInstance(dm.widthPixels,dm.heightPixels,PixelFormat.RGBA_8888,2)
        projection!!.createVirtualDisplay("Othello",dm.widthPixels,dm.heightPixels,dm.densityDpi,DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,reader!!.surface,null,null)
        overlay=HintOverlay(this); overlay!!.show()
        reader!!.setOnImageAvailableListener({ r-> r.acquireLatestImage()?.use{ BoardDetector.process(it,overlay!!) } },Handler(Looper.getMainLooper()))
        return START_STICKY
    }
    private fun createChannel(){ if(Build.VERSION.SDK_INT>=26)getSystemService(NotificationManager::class.java).createNotificationChannel(NotificationChannel("scan","Screen scan",NotificationManager.IMPORTANCE_LOW)) }
    override fun onBind(i:Intent?)=null
    override fun onDestroy(){reader?.close();projection?.stop();overlay?.hide();super.onDestroy()}
}
