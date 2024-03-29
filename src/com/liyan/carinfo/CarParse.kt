package com.liyan.carinfo

import com.sobte.cqp.jcq.entity.CoolQ
import com.time.nlp.TimeNormalizer
import java.lang.Exception
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.HashMap


class CarParse {

    private val key=HashMap<String,String>()
    private val carKey=HashMap<String,String>()
    private val personKey=HashMap<String,String>()
    private val viewCommandKey=HashMap<String,String>()
    private val delCommandKey=HashMap<String,String>()

    private var thread:Thread?=null
    private var exit=false

    companion object{
        private const val TAG="CarParse"
        private const val SEARCH_COMMAND="搜"
        private const val TYPE_CAR=1
        private const val TYPE_PERSON=2
        val instance by lazy(mode =LazyThreadSafetyMode.SYNCHRONIZED ) { CarParse() }
    }

    private var CQ:CoolQ?=null

    init {
        carKey["車找人"] = ""
        carKey["車尋人"] = ""
        carKey["车找人"] = ""
        carKey["順路車"] = ""
        carKey["私家車"] = ""
        carKey["车寻人"] = ""
        carKey["顺路车"] = ""
        carKey["私家车"] = ""
        personKey["人找车"]=""
        personKey["人寻车"]=""
        personKey["人尋車"]=""
        personKey["人找車"]=""
        key.putAll(carKey)
        key.putAll(personKey)

        viewCommandKey["拼车信息"] = ""
        viewCommandKey["拼车"] = ""

        delCommandKey["车满"] = ""
        delCommandKey["删除"] = ""
        delCommandKey["#删除"] = ""
        delCommandKey["#车满"] = ""
    }

    class Info{
        var type:Int?=1
        var time:Long?=0
        var content:String?=null
        var phone:String?=null
        var QQ:String?=null
        var group:String?=null
    }

    private fun getNoDataMsg(qq:String?):String?{
        return "[CQ:at,qq=$qq]\n非常抱歉,暂时没有相关信息"
    }

    fun parse(qq:String,groupId:String?,content: String?){
        try {
            parseExe(qq,groupId,content)
        }catch (e:Exception){
            println(e.message)
        }
    }
    private fun parseExe(qq:String,groupId:String?,content: String?) {
        if(content.isNullOrEmpty()) {
            println("解析内容为空，直接返回")
            return
        }

        if(Utils.isBlackList(qq)){
            println("黑名单QQ：$qq")
            return
        }
        println("$content:群组:$groupId:qq:$qq")
//        var content=TransactSQLInjection(content)

        if(content.substring(0,1)== SEARCH_COMMAND){
            println("搜索拼车信息")
            if(content.length>1) {
                val key = content.substring(1).trim()
                println("搜索关键字：$key")
                val content= DbManager.instance.search(groupId,key)
                if(content.isNotEmpty()){
                    MessageManager.instance.sendMessage(groupId,content)
                }else{
                    MessageManager.instance.sendMessage(groupId, getNoDataMsg(qq))
                }
            }
            return
        }

        viewCommandKey.forEach {
            if(it.key==content){
                println("查看拼车信息：$groupId")
                val content= DbManager.instance.queryInfo(groupId)

                if(content?.isNotEmpty()==true) {
                    MessageManager.instance.sendMessage(groupId, content)
                }else{
                    MessageManager.instance.sendMessage(groupId, getNoDataMsg(qq))
                }
                return
            }
        }

        delCommandKey.forEach {
            if(it.key==content){
                val result= DbManager.instance.queryExist(groupId,qq)
                var content=""
                if(result){
                    DbManager.instance.delMsgData(qq,groupId)
                    content= "[CQ:at,qq=$qq]\n删除成功，您的信息将不再转发"
                }else{
                    content="[CQ:at,qq=$qq]\n虽然我是机器人，但也不能这么调戏我"
                }
                MessageManager.instance.sendMessage(groupId,content)
                return
            }
        }

        var result=false
        key.forEach {
            if(content.contains(it.key)){
                result=true
                return@forEach
            }
        }

        if(!result) {
            println("没有解析的关键字，直接返回：$content")
            return
        }
        val info= Info()
        val timerParse=getTimeParse()
        try {
            timerParse?.parse(content)// 抽取时间
        }catch (e:Exception){
            println("解析时间出错"+e.cause.toString())
            return
        }
        val unit = timerParse?.timeUnit
        if(unit?.isEmpty()==true) {
            val message="[CQ:at,qq=$qq]\n如果您想让机器人自动帮你发信息，需要具体的出发时间和手机号哦！"
            MessageManager.instance.sendMessage(groupId,message)
            println("没有解析到时间，直接返回：$content")
            return
        }

        val phone=getPhone(content)
        if(phone.isNullOrEmpty()){
            val message="[CQ:at,qq=$qq]\n如果您想让机器人自动帮你发信息，需要具体的出发时间和手机号哦！"
            MessageManager.instance.sendMessage(groupId,message)
            println("没有解析到时间，直接返回：$content")
            return
        }
        val descTime = unit?.get(0)?.time
        val timeStr= Utils.getFormatTime(descTime?.time)
        if((descTime?.time?:0)<Calendar.getInstance().timeInMillis){
            val message="[CQ:at,qq=$qq]\n登记失败 您的时间$timeStr"+"已经过期"
            MessageManager.instance.sendMessage(groupId,message)
            return
        }
        if((descTime?.time?:0)>Calendar.getInstance().timeInMillis+7*24*3600*1000){
            val message="[CQ:at,qq=$qq]\n目前只支持登记7天以内的信息！"
            MessageManager.instance.sendMessage(groupId,message)
            return
        }

        info.QQ=qq
        info.group=groupId
        info.time=descTime?.time
        info.phone=phone
        info.content=content.replace("\n","")

        var successInfo=""

        val endDesc="机器人每隔${DbManager.TIME_INTERVAL}分钟会自动转发您的信息\n如果您想取消登记请发送 删除"
        carKey.forEach{
            if(content.contains(it.key)){
                info.type= TYPE_CAR
                successInfo= "[CQ:at,qq=$qq][CQ:face,id=76][CQ:face,id=76][CQ:face,id=76]\n登记成功 发车时间:$timeStr\n$endDesc"
                return@forEach
            }
        }
        personKey.forEach{
            if(content.contains(it.key)){
                info.type= TYPE_PERSON
                successInfo= "[CQ:at,qq=$qq][CQ:face,id=76][CQ:face,id=76][CQ:face,id=76]\n登记成功 坐车时间:$timeStr\n$endDesc"
                return@forEach
            }
        }
        DbManager.instance.add(info)
        println("保存到数据库成功：$content")
        successInfo= "$successInfo\n如有任何疑问，请联系QQ：279135138"
        println(successInfo)
        MessageManager.instance.sendMessage(groupId,successInfo)

        return
    }

    private fun getPhone(content: String?):String?{
        val pattern = Pattern.compile("(?<!\\d)(?:(?:1[35789]\\d{9})|(?:861[358]\\d{9}))(?!\\d)");
        val matcher=pattern.matcher(content)
        val bf =StringBuffer(64);
        while (matcher.find()){
            if(bf.isNotEmpty()){
                break
            }
            bf.append(matcher.group())
        }
        println(bf.toString())
        return bf.toString()
    }

    private fun println(message: String){
        System.out.println(message)
        CQ?.logInfo(TAG,message)
    }

    private fun TransactSQLInjection(str: String): String {
        return str.replace(".*([';]+|(--)+).*".toRegex(), " ")
    }

    private fun getTimeParse():TimeNormalizer?{
        return TimeNormalizer("db/TimeExp.m")
    }

    fun start(CQ:CoolQ?){
        this.CQ=CQ

        DbManager.instance.start(CQ)
        MessageManager.instance.start(CQ)
        thread= Thread(Runnable {
            while (!exit){
                DbManager.instance.delTimeOut()
                DbManager.instance.getAutoSendMessage()?.forEach {
                    MessageManager.instance.sendMessage(it.group,it.content)
                }
                println("定时发送任务执行中")
                Thread.sleep(60*1000)
            }
        })
        thread?.start()
    }

    fun stop(){
        MessageManager.instance.stop()
        exit=true
        thread=null
        DbManager.instance.quit()
    }
}