package com.liyan.carinfo

import com.sobte.cqp.jcq.entity.CoolQ
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class MessageManager{

    private var service:ScheduledExecutorService?= null
    private var CQ:CoolQ?=null

    companion object{
        private const val TAG="MessageManager"
        val instance by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) { MessageManager() }
        private const val FILTER2= "*中共*民主*共产党*中国共产党*胡锦涛*反共*反党*反国家*游行*集会*强奸*走私*强暴*套套*摇头丸*白粉*冰毒*海洛因*假/钞*人民币*假币*操*我日*干你*我操*操你*操你妈*干你妈*傻逼*大逼*高潮*鸡鸡*鸡巴*做爱*打炮*打洞*插入*抽插*贱人*贱逼*骚逼*牛逼*妈的*他妈的*"
        private const val FILTER1="*电话*手机*预约*加群*日赚*红包群*加Q*加q*包补*诚聘*代购*放心购买*QQ群发*有意私聊*接单*任何费用*收徒*淘宝交易*招聘*要的加*低价*业务电话*转让*工资日结*现金交易*代刷*返红包*业务QQ*代价*刷钻*开钻*供货*招收*带价*免费兼职*赶快加入*如有打扰*提供货源*负责售后*火爆出售中*请+q*有需求的可以*的赚钱*寻大量*找代理*招代理*寻合作*正规开票*群里不回*网赚*可购买*欢迎加盟*推荐奖金*刷Q*兼职*点击网址*有需要做*要求能上网*急聘*免费刷*包赚*群号码*要的联系*卡盟*微信公众平台*精品课程"
    }
    private var messageList:ArrayList<Message>?=null
    private fun filterContent(content:String?):String?{
        if(content?.isEmpty()==true)
            return ""
        var data = content?.split(FILTER1, "*")
        data?.forEach {
            content?.replace(it,"")
        }
        data = content?.split(FILTER2, "*")
        data?.forEach {
            content?.replace(it,"")
        }
        return content
    }

    fun sendMessage(groupId:String?,content: String?){
        val content= filterContent(content)
        val message= Message()
        message.group=groupId
        message.content=content
        getMessageList()?.add(message)
        CQ?.logInfo(TAG,"加入发送队列:$content,groupid:$groupId")
    }

    private fun getMessageList():ArrayList<Message>?{
        if(messageList==null)
            messageList=ArrayList()
        return messageList
    }

    private fun poseMessage(message: Message?){
        CQ?.logInfo(TAG,"发送消息:${message?.content},groupid:${message?.group}")
        val groupId=message?.group?.toLong()?:0
        if(groupId!=0L) {
            val content=message?.content?:""
            if(content.isNotEmpty()) {
                CQ?.sendGroupMsg(groupId, content)
            }
        }
    }

    fun start(CQ:CoolQ?){
        this.CQ=CQ
        service = Executors
            .newSingleThreadScheduledExecutor()
        service?.scheduleAtFixedRate({
            if(getMessageList()?.isNotEmpty()==true){
                CQ?.logInfo(TAG,"存在消息，即将发送消息")
                val message= getMessageList()?.get(0)
                getMessageList()?.removeAt(0)
                poseMessage(message)
            }
        }, 0, 1, TimeUnit.SECONDS)
    }

    fun stop(){
        service?.shutdown()
    }

}