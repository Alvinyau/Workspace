package com.spark.application

import org.apache.spark.{SparkConf, SparkContext}
import scala.collection.mutable.ArrayBuffer
import scala.io.Source

/**
  * Created by yaoyingjie on 2016/4/7.
  */
object AnalysePersonRelationship extends App{

  //创建spark上下文
  val conf =new SparkConf()
  conf.setAppName("AnalysePersonRealtionship")
  val sc=new SparkContext(conf)
  var personRelationshipMap=Map[Int,String]()
  var idList=ArrayBuffer[Int]()                     //存放证件ID
  var resultMap=Map[Int,Map[String,String]]()   //存放结果

  var dataFilePath="/opt/homework/data.txt"
  //读取文件，遍历每行数据添加到personRelationshipMap中
  Source.fromFile(dataFilePath,"GBK").getLines().foreach{s=>
    val ss=s.split(",")
    val key=ss(0).toInt
    //根据证件ID去重
    if(personRelationshipMap.contains(key)) {
      var value=personRelationshipMap.apply(key)
      value+=s
      personRelationshipMap+=(key->value)
    }else{
      idList+=key
      personRelationshipMap+=(key->s)
    }
  }

  //对比是否同旅馆
  for(i <- 1 to idList.size){
    for(j <- i+1 to idList.size if j<idList.size){
      //每次对比的入住相同旅馆总数
      var hotelCount:Int=0
      //存放第一位的入住旅馆名称与次数
      var hotelMap1=Map[String,Int]()
      //存放第二 位的入住旅馆名称与次数
      var hotelMap2=Map[String,Int]()
      val key1=idList.apply(i)
      val key2=idList.apply(j)
      val value1=personRelationshipMap.apply(key1)  //取出第一个人数据
      val value2=personRelationshipMap.apply(key2)  //取出第二个人数据
      var content=Array[String]()
      var content1=Array[String]()
      //如果入住多家旅馆
      //例子：张三,101,2016-04-06 10:12:52,hotelA_1,张三,112,2016-4-23 10:12:52,hotelC
      if(value1.contains("_")){
        content=value1.split("_")
        for(elem <- content){
          //例子：张三,101,2016-04-06 10:12:52,hotelA
          val temp=elem.split(",")
          val hotelNameKey=temp(4)
          //如果旅馆名称重复+1，否则new key,value赋1
          if(hotelMap1.contains(hotelNameKey)){
            var count:Int=hotelMap1(hotelNameKey)
            count=count+1
            hotelMap1+=(hotelNameKey->count)

          } else hotelMap1+=(hotelNameKey->1) //new key,value赋1
        }
      }else{
        val temp=value1.split(",")
        val hotelNameKey=temp(4)
        //如果旅馆名称重复+1，否则new key,value赋1
        if(hotelMap1.contains(hotelNameKey)){
          var count:Int=hotelMap1(hotelNameKey)
          count=count+1
          hotelMap1+=(hotelNameKey->count)
        } else hotelMap1+=(hotelNameKey->1)  //new key,value赋1
      }
      //如果入住多家旅馆
      if(value2.contains("_")){
        content1=value2.split("_")
        for(elem <- content1){
          val temp=elem.split(",")
          val hotelNameKey=temp(4)
          //如果旅馆名称重复+1，否则new key,value赋1
          if(hotelMap2.contains(hotelNameKey)){
            var count:Int=hotelMap2(hotelNameKey)
            count=count+1
            hotelMap2+=(hotelNameKey->count)
          } else hotelMap2+=(hotelNameKey->1)    //new key,value赋1
        }
      }else{
        val temp=value2.split(",")
        val hotelNameKey=temp(4)
        //如果旅馆名称重复+1，否则new key,value赋1
        if(hotelMap2.contains(hotelNameKey)){
          var count:Int=hotelMap2(hotelNameKey)
          count=count+1
          hotelMap2+=(hotelNameKey->count)
        } else hotelMap2+=(hotelNameKey->1)   //new key,value赋1
      }

      for((k,v)<-hotelMap1){
        println("hotelMap1 "+k+" -> "+v)
      }
      for((k,v)<-hotelMap2){
        println("hotelMap2 "+k+" -> "+v)
      }
      //合并第一位与第二位旅客入住相同旅馆的次数
      for((k,v)<-hotelMap2){
        if(hotelMap1.contains(k))
          hotelCount+=v+hotelMap1(k)
      }

      println(hotelCount)
    }
  }



}
