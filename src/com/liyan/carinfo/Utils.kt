package com.liyan.carinfo

import java.text.SimpleDateFormat
import java.util.*
import java.io.IOException
import java.io.FileReader
import java.io.BufferedReader
import java.io.File
import java.lang.StringBuilder
import java.util.regex.Pattern


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
        val dt= SimpleDateFormat("yyyy/MM/dd HH:mm")
        return dt.format(time)
    }

    fun replaceEnter(content:String?):String{
        val p = Pattern.compile("\\s*|\t|\r|\n")
        val m = p.matcher(content)
        val content = m.replaceAll("")
        return content
    }

    fun replace(content: String?,regx:String?):String{
        val p = Pattern.compile(regx)
        val m = p.matcher(content)
        val content = m.replaceAll("")
        return content
    }

    fun readFileByLines(fileName: String):String {
        val file = File(fileName)
        val sb=StringBuilder()
        var reader: BufferedReader? = null
        try {
            reader = BufferedReader(FileReader(file))
            var tempString: String? = null
            tempString = reader.readLine()
            while ((tempString) != null) {
                sb.append(tempString)
                tempString = reader.readLine()
            }
            reader.close()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            if (reader != null) {
                try {
                    reader.close()
                } catch (e1: IOException) {
                }

            }
        }
        return sb.toString()
    }

    fun isSettingGroup(groupId:String):Boolean{
        val groupStr=readFileByLines("db/groupsetting")
        val data=groupStr.split("|")
        val map=HashMap<String,String>()
        data.forEach{
            map.put(it,it)
        }
        return map.contains(groupId)
    }
}