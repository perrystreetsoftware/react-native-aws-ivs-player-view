package com.perrystreetsoftware

import android.os.Handler
import android.os.Message
import com.amazonaws.ivs.player.Player
import java.lang.ref.WeakReference

interface AwsIvsTransferListener {
    fun onBitrateRecalculated(bitrate: Long)
}

class AwsIvsBitrateCalculator(player: Player, listener: AwsIvsTransferListener) {
    companion object {
        val MSG_CHECK_BITRATE = 0
        val RECALC_RATE_IN_MS = 5000
    }
    private val mHandler = IncomingHandler(this)
    private val mListener: AwsIvsTransferListener
    private val mPlayer: WeakReference<Player>
    private var mDisposed = false

    internal class IncomingHandler(target: AwsIvsBitrateCalculator) : Handler() {
        private val mTarget: WeakReference<AwsIvsBitrateCalculator>
        override fun handleMessage(msg: Message) {
            mTarget.get()?.let { target ->
                when (msg.what) {
                    MSG_CHECK_BITRATE -> target.measureBitrate()
                    else -> super.handleMessage(msg)
                }
            } ?: run {
                super.handleMessage(msg)
            }
        }

        init {
            mTarget = WeakReference(target)
        }
    }

    init {
        this.mListener = listener
        this.mPlayer = WeakReference(player)

        start()
    }

    fun dispose() {
        mHandler.removeMessages(MSG_CHECK_BITRATE)
        this.mDisposed = true
    }

    fun start() {
        if (!this.mDisposed) {
            mHandler.sendMessageDelayed(Message.obtain(mHandler, MSG_CHECK_BITRATE), RECALC_RATE_IN_MS.toLong())
        }
    }

    fun measureBitrate() {
        mPlayer.get()?.let { player ->
            val bitrate = if (player.state == Player.State.PLAYING) player.averageBitrate else 0

            mListener.onBitrateRecalculated(bitrate)
        }

        if (!this.mDisposed) {
            mHandler.sendMessageDelayed(Message.obtain(mHandler, MSG_CHECK_BITRATE), RECALC_RATE_IN_MS.toLong())
        }
    }
}