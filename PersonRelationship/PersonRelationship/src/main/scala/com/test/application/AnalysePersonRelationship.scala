package com.spark.application

import org.apache.spark.{SparkContext, SparkConf}

import scala.collection.mutable
import scala.collection.mutable.{HashMap, ArrayBuffer}

/**
 * Created by yaoyingjie on 2016/4/7.
 */
class AnalysePersonRelationship{

  /**
   * 判断是否入住相同旅馆
   * @param idList 证件IDlist
   * @param personRelationshipMap  人员关系分析Map
   */
  def analyse(idList:ArrayBuffer[Int],personRelationshipMap:mutable.HashMap[Int,String]):HashMap[String,HashMap[String,String]]={
    //证件ID key，例如甲方key_乙方key
    var comparisonKey=""
    //人员分析临时结果
    val analyseReportMap=new HashMap[String,HashMap[String,String]]
    for(i <- 1 to idList.size){
      for(j <- i+1 to idList.size if j<idList.size){

        //每次对比的入住相同旅馆总数
        val key1=idList(i)
        val key2=idList(j)
        val value1 = personRelationshipMap(key1) //取出甲方数据
        val value2 = personRelationshipMap(key2) //取出乙方数据
        //存放第一位的入住旅馆名称与次数
        val hotelMap1 = this.packageHotelCountMap(value1)
        //存放第二位的入住旅馆名称与次数
        val hotelMap2 = this.packageHotelCountMap(value2)

        //分析旅馆和房间次数,该函数返回一个Map结果集，value为相同旅馆次数的key为hotelCount，value为相同房间次数的key为roomCount，
        val hotelAndRommCountMap = this.analyseSameHotelAndRoomCount(hotelMap1, hotelMap2, value1, value2)
        val hotelCount = hotelAndRommCountMap("hotelCount")
        val roomCount = hotelAndRommCountMap("roomCount")
        var isValentine = "不为情侣"
        //相同房间3次以上为情侣
        if (roomCount.toInt > 3) {
          isValentine = "为情侣"
        }
        val name1 = value1.split(",")(1)
        val name2 = value2.split(",")(1)
        comparisonKey = key1 + "_" + key2
        val comparisonPersonInfo = "【 证件号：" + key1 + "，姓名：" + name1 + " 】 与【 证件号 " + key2 + ",姓名：" + name2 + " 】 对比"
        //存储对比人证件信息
        val personRelationshipTempMap = new HashMap[String, String]
        personRelationshipTempMap += ("comparisonPersonInfo" -> comparisonPersonInfo)
        personRelationshipTempMap += ("APersonInfo" -> value1)
        personRelationshipTempMap += ("BPersonInfo" -> value2)
        personRelationshipTempMap += ("sameHotelCount" -> hotelCount)
        personRelationshipTempMap += ("sameRoomCount" -> roomCount)
        personRelationshipTempMap += ("isValentine" -> isValentine)
        analyseReportMap += (comparisonKey -> personRelationshipTempMap)
      }
    }
    analyseReportMap
  }

  /**
   * 将数据转成成Map key=hotelName value=count
   * @param value 例子：张三,101,2016-04-06 10:12:52,hotelA_1,张三,112,2016-4-23 10:12:52,hotelC
   * @return
   */
  def packageHotelCountMap(value:String):HashMap[String,Int]={
    val hotelMap=new HashMap[String,Int]
    //如果入住多家旅馆
    //例子：张三,101,2016-04-06 10:12:52,hotelA_1,张三,112,2016-4-23 10:12:52,hotelC
    if(value.contains("_")){
      val content=value.split("_")
      for(elem <- content){
        //例子：张三,101,2016-04-06 10:12:52,hotelA
        val temp=elem.split(",")
        val hotelNameKey=temp(4)
        //如果旅馆名称重复+1，否则new key,value赋1
        if(hotelMap.contains(hotelNameKey)){
          var count:Int=hotelMap(hotelNameKey)
          count=count+1
          hotelMap+=(hotelNameKey->count)

        } else hotelMap+=(hotelNameKey->1) //new key,value赋1
      }
    }else{
      val temp=value.split(",")
      val hotelNameKey=temp(4)
      //如果旅馆名称重复+1，否则new key,value赋1
      if(hotelMap.contains(hotelNameKey)){
        var count:Int=hotelMap(hotelNameKey)
        count=count+1
        hotelMap+=(hotelNameKey->count)
      } else hotelMap+=(hotelNameKey->1)  //new key,value赋1
    }
    hotelMap
  }

  /**
   * 封装房间信息Map key:房间号 value:入住日期
   * @param value
   * @param key
   * @return
   */
  def packageRoomMap(value:String,key:String ):HashMap[String,HashMap[String,Int]]={
    val roomMap=new HashMap[String,HashMap[String,Int]]
    var roomCount=0
    //如果入住多家旅馆
    //例子：张三,101,2016-04-06 10:12:52,hotelA_1,张三,112,2016-4-23 10:12:52,hotelC
    if(value.contains("_")){
      val content=value.split("_")
      for(elem <- content){
        val temp=elem.split(",")
        //例子：张三,101,2016-04-06 10:12:52,hotelA
        val hotelName=temp(4)
        if(key.equals(hotelName)){
          val roomNumKey=temp(2)
          val checkInTime=temp(3)
          if(roomMap.contains(roomNumKey)){
            roomCount=roomCount+1
            val roomCountMap=new HashMap[String,Int]
            roomCountMap+=(checkInTime->roomCount)
            roomMap+=(roomNumKey->roomCountMap)
          }else{
            val roomCountMap=new HashMap[String,Int]
            roomCountMap+=(checkInTime->1)
            roomMap+=(roomNumKey->roomCountMap)
          }
        }
      }
    }else{
      val temp=value.split(",")
      //例子：张三,101,2016-04-06 10:12:52,hotelA
      val hotelName=temp(4)
      if(key.equals(hotelName)){
        val roomNumKey=temp(2)
        val checkInTime=temp(3)
        if(roomMap.contains(roomNumKey)){
          roomCount=roomCount+1
          val roomCountMap=new HashMap[String,Int]
          roomCountMap+=(checkInTime->roomCount)
          roomMap+=(roomNumKey->roomCountMap)
        }else{
          val roomCountMap=new HashMap[String,Int]
          roomCountMap+=(checkInTime->1)
          roomMap+=(roomNumKey->roomCountMap)
        }
      }
    }
    roomMap
  }

  /**
   * 分析相同旅馆与相同房间号的次数
   * @param A
   * @param B
   * @param valueA
   * @param valueB
   * @return  sameHotelAndRoomCountMap key1:hotelCount key2:roomCount
   */
  def analyseSameHotelAndRoomCount(A:HashMap[String,Int],B:mutable.HashMap[String,Int],valueA:String,valueB:String):HashMap[String,String]={
    val sameHotelAndRoomCountMap=new HashMap[String,String]
    var hotelCount=0
    var roomCount=0
    //合并甲方与乙方旅客入住相同旅馆的次数
    for((k,v)<-B){
      if(A.contains(k)) {  //为同一旅馆,可往后判断是否有入住相同房间
        hotelCount += v + A(k)
        val roomMapA=this.packageRoomMap(valueA,k)
        val roomMapB=this.packageRoomMap(valueB,k)
        //对比是否入住相同日期的相同房间
        roomCount=this.comparisonSameRoom(roomMapA,roomMapB)
      }
    }
    sameHotelAndRoomCountMap+=("hotelCount"->hotelCount.toString)
    sameHotelAndRoomCountMap+=("roomCount"->roomCount.toString)
  }

  /**
   * 对比入住房间号与入住日期是否相同
   * @param roomMapA
   * @param roomMapB
   * @return
   */
  def comparisonSameRoom(roomMapA:HashMap[String,HashMap[String,Int]],roomMapB:HashMap[String,HashMap[String,Int]]):Int={
    var roomCount=0
    if(roomMapA.size>0 && roomMapB.size>0){
      for((k,v) <- roomMapB){
        if(roomMapA.contains(k)){
          val roomCountMapB=v
          val roomCountMapA=roomMapA(k)
          for((k1,v1) <- roomCountMapB){
            if(roomCountMapA.contains(k1)){
              roomCount=v1+roomCountMapA(k1)
            }
          }
        }
      }
    }
    roomCount
  }

}

object AnalysePersonRelationship{

  def main(args: Array[String]) {
    //创建spark上下文
    val conf = new SparkConf()
    conf.setAppName("AnalysePersonRealtionship")
    val sc = new SparkContext(conf)
    val dataFilePath = "/usr/local/spark/spark-1.6.1-bin-hadoop2.6/test/data.txt"

    val personRelationshipMap =new HashMap[Int, String]
    val idList =ArrayBuffer[Int]() //存放证件ID

    sc.textFile(dataFilePath).flatMap(_.split(" ")).collect().foreach(elem=>{
      val ss=elem.split(",")
      val key=ss(0).toInt
      //根据证件ID去重
      if(personRelationshipMap.contains(key)) {
        var value=personRelationshipMap.apply(key)
        value+="_"+elem
        personRelationshipMap+=(key->value)
      }else{
        idList+=key
        personRelationshipMap+=(key->elem)
      }
    })

    val analyseClass=new AnalysePersonRelationship
    val analyseReportMap=analyseClass.analyse(idList,personRelationshipMap)
    for((k,v) <- analyseReportMap){
      println("*******************************************************************************")
      println("     "+v("comparisonPersonInfo"))
      println("     "+"住房记录 【")
      println("            "+v("APersonInfo"))
      println("            "+"】")
      println("            "+"住房记录 【")
      println("            "+v("BPersonInfo"))
      println("            "+"】")
      println("     同旅馆次数："+v("sameHotelCount"))
      println("     同房间次数："+v("sameRoomCount"))
      println("     "+v("isValentine"))
      println("*******************************************************************************")
    }

    //释放spark上下文
    sc.stop()
  }
}
