package com.liyan.carinfo

import java.text.SimpleDateFormat
import java.util.*

object Utils{
    fun isNight():Boolean{
        val calendar=Calendar.getInstance()
        val hour=calendar.get(Calendar.HOUR_OF_DAY)
        if (hour > 23 || (hour in 0..6)){
            return true
        }
        return false
    }

    fun getFormatTime(time:Long?):String?{
        val dt= SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
        return dt.format(time)
    }
}