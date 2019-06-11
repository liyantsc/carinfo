package com.liyan.carinfo

import com.sobte.cqp.jcq.entity.CoolQ
import java.lang.StringBuilder
import java.sql.Connection
import java.sql.DriverManager
import java.sql.Statement
import java.sql.ResultSet
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList




class DbManager{

    private var conn:Connection?=null
    private var CQ:CoolQ?=null
    companion object{
        private const val TIME_INTERVAL=30
        const val TAG="DbManager"
        val instance: DbManager by lazy(mode = LazyThreadSafetyMode.NONE) { DbManager() }
    }


    private fun getNextSendTime():Long{
        val calendar=Calendar.getInstance()
        var timeCount= TIME_INTERVAL
        if(Utils.isNight()) {
            timeCount = TIME_INTERVAL * 2
        }
        calendar.add(Calendar.MINUTE,timeCount)
        return calendar.timeInMillis
    }

    fun delMsgData(qq:String?,groupId: String?){
        val sql= "delete from msgdata where [qq]='$qq' and [group]='$groupId'"
        getStmt()?.execute(sql)
    }
    
    private fun getStmt():Statement?{
        if(conn==null || conn?.isClosed==true){
            Class.forName("org.sqlite.JDBC")
            conn = DriverManager.getConnection("jdbc:sqlite:db/data.db")
        }
        return conn?.createStatement()
    }

    fun add(info: CarParse.Info){
        delMsgData(info.QQ,info.group)
        val sql="insert into msgdata([qq],[mobile],[group],[time],[type],[content]) values('" + info.QQ + "','" + info.phone + "','" + info.group + "','" + info.time + "','"+info.type+"','" + info.content + "')"
        getStmt()?.execute(sql)


        val result=queryGroupExist(info.group)
        if(result==false){
            val sql="insert into groupinfo ([group],[time]) values('" + info.group + "','" + getNextSendTime() + "')"
            getStmt()?.execute(sql)
        }
    }

    private fun queryGroupExist(groupId: String?):Boolean?{
        val rs = getStmt()?.executeQuery("select * from groupinfo where [group]='$groupId'")
        var result=rs?.next()
        rs?.close()
        return result
    }

    fun queryExist(groupId: String?,qq: String?):Boolean{
        val rs = getStmt()?.executeQuery("select * from msgdata where [group]='$groupId' and [qq]='$qq'")
        val result=rs?.row?:0>0
        rs?.close()
        return result
    }

    fun search(groupId: String?,key: String?):String{
        val sql= "select * from msgdata where "+ splitKeyWord(key) +" order by type+time"
        val rs = getStmt()?.executeQuery(sql)
        var index=0
        val sb=StringBuilder()
        val contentSb = StringBuffer()
        if(rs?.row?:0>0) {
            while (rs?.next() == true) {
                index += 1
                if (index <= 5) {
                    if (sb.isNotEmpty()) {
                        sb.append("\n\n")
                    }
                    sb.append(getContent(rs, index))
                }
            }
            val beginStr = "[$key]搜索结果:\n\n===拼车消息(" + index + "条,只显示前5条)按发车先后==\n\n"
            contentSb.append(beginStr)
            contentSb.append(sb.toString())
        }
        rs?.close()
        return contentSb.toString()
    }

    fun updateNextTime(groupId: String?){
        val sql= "update groupinfo set [time]='${getNextSendTime()}' where [group]='$groupId'"
        getStmt()?.execute(sql)
    }

    private fun splitKeyWord(keyWord:String?):String{
        val sb=StringBuilder()
        val data=keyWord?.split(" ")
        data?.forEach {
            if(it.isNotEmpty()){
                if(sb.isNotEmpty()){
                    sb.append(" and ")
                }
                sb.append(" content like '%$it%' ")
            }
        }
        return sb.toString()
    }

    fun queryInfo(groupId:String?):String? {
        val rs = getStmt()?.executeQuery("select * from msgdata where [group]='$groupId' order by type+time")
        var index = 0
        val sb = StringBuilder()
        while (rs?.next() == true) {
            index += 1
            val content = getContent(rs, index)
            if (content?.isNotEmpty() == true) {
                if (sb.isNotEmpty()) {
                    sb.append("\n\n")
                }
                sb.append(content)
            }
        }
        val infoStr=StringBuilder()
        if (sb.isNotEmpty()) {
            val beginStr = "===拼车消息(" + index + "条)按发车先后==\n\n"
            infoStr.append(beginStr)
            val endTime = getNextSendTime()
            val dt = SimpleDateFormat("HH:mm:ss")
            infoStr.append(sb.toString())
            val endStr = "\n\n下次转发时间:" + dt.format(endTime) + ",取消登记请发送 删除"
            infoStr.append(endStr)
        }
        println(infoStr.toString())
        rs?.close()
        return infoStr.toString()
    }

    private fun getContent(resultSet: ResultSet,index:Int):String?{
        val time=resultSet.getString(resultSet.findColumn("time"))
        val dt=SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val strTime=dt.format(time.toLong())
        val type=resultSet.getString(resultSet.findColumn("type"))
        val mobile=resultSet.getString(resultSet.findColumn("mobile"))
        val content=resultSet.getString(resultSet.findColumn("content"))
        val emoji=if(type=="1"){
            "[CQ:emoji,id=128661]"
        }else {
            "[CQ:emoji,id=128694]"
        }
        content.replace("\n","")
        content.replace("\t","")
        val sb=StringBuilder()
        sb.append(index)
            .append(emoji)
            .append(strTime)
            .append("·℡·")
            .append(mobile)
            .append("·")
            .append(content)
        println(sb.toString())
        return sb.toString()
    }

    fun delTimeOut(){
        val time=Calendar.getInstance().timeInMillis
        val sql= "delete from msgdata where time<'$time'"
        getStmt()?.execute(sql)
    }

    fun getAutoSendMessage():List<Message>?{
        val list=ArrayList<Message>()
        val time=Calendar.getInstance().timeInMillis
        val sql= "select * from groupinfo where time<='$time'"
        val rs = getStmt()?.executeQuery(sql)
        while (rs?.next()==true){
            val group = rs.getString(rs.findColumn("group")) + ""
            val content = queryInfo(group)
            val message= Message()
            message.content=content
            message.group=group
            list.add(message)
        }
        rs?.close()
        return list
    }

    fun start(CQ:CoolQ?){
        this.CQ=CQ
        println("开始连接数据库")
        Class.forName("org.sqlite.JDBC")
        conn = DriverManager.getConnection("jdbc:sqlite:db/data.db")
        println("数据库状态:"+conn?.isClosed)
    }


    private fun println(message: String){
        System.out.println(message)
        CQ?.logInfo(TAG,message)
    }
    fun quit(){
        conn?.close()
        getStmt()?.close()
    }
}