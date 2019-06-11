package com.liyan.carinfo

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


object MainClass {
    @JvmStatic
    fun main(args: Array<String>) {
        val list=ArrayList<String>()
        list.add("车找人：今晚5:20 永丰地铁站-北清路-国富-钻石  余2座  明早  6:40原路返回余 3座  有需要联系")
        list.add("@车寻人@今晚17:30@17610300299@软件园北333公交车站出发，后厂村路，永丰路，北清路沿线地铁，六环，涿州北，腾飞大街，华阳路，七号院，国富，广场，汇源大酒店，龙马家园，余一座")
        list.add("车找人\n" +
                "     明天早晨4:30易县去北京，7点北京回易县。13661204827  （微信同号）\n" +
                " 易县⇔北京拼车，高速车队每天往返，每天早4:30---下午4:00，易县去北京。早6:30-晚7:30北京回易县，随时有车。全新七 座商务，可提供包车 ，捎货，拼车服务！各大医院，机场， 议价可接送，请您提前预订，13661204827  （微信同号）")
        list.add("车找人！18600598354\n" +
                "\n" +
                "今天中午1:00~1:30易县去北京速度联系！\n" +
                "\n" +
                "18600598354")
        list.add("车找人今天上午10.30点左右北京回易县的速联15130463884\n" +
                "包车捎货均可")
        list.add("车找人\n" +
                "       8:30左右北京回易县有走联系17344452006\n" +
                "       可包车捎货")
        list.add(" 车找人！（高村——六里桥）\n" +
                "今天上午，9/30-----10/00去北京，走的联系\n" +
                "，\n" +
                "17325563222（微信同号）7座商务车，提前定车")
        list.add("私家车找人，明天（星期一）早上5点县城发车回北京南四环10号线，5号线，亦庄线宋家庄地铁站，经过4号线公益西桥地铁站18519814648")
        list.add("车找人\n" +
                "明天早上4:30分易县回北京。有座车的提前联系电话13651223852高速ETC。终点大红门地铁站。（10号线）大红门服装城。")
        list.add("私家车找人，易县回北京\n" +
                "途径裴山～高村～易县～112国道～北清路～屯佃地铁站～永丰地铁站～永丰南路公交站\n" +
                "有走的老乡提前联系\n" +
                "15811008469\n" +
                "微信同步")
        val service = Executors
            .newSingleThreadScheduledExecutor()
        service.scheduleAtFixedRate({
            print("定时任务启动")
            DbManager.instance.delTimeOut()
        }, 0, 60, TimeUnit.SECONDS)

//        CarInfoParse.instance.start(null)
        CarParse.instance.parse("123456","123","搜 易县")

//        list.forEach{
//            var info=parse.parse("123","123",it)
//            if(info?.result==true){
//                if(info.result==true) {
//                    DbManager.instance.add(info)
//                }
//            }
//        }
    }
}