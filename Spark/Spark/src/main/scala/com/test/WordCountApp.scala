package com.test

import org.apache.spark.{SparkConf, SparkContext}

/**
 * Created by Alvinyau on 2016/4/1.
 */
object WordCountApp{

  def main(args: Array[String]) {
    val textFile="/usr/local/spark/spark-1.6.1-bin-hadoop2.6/README.md"
    val hdfsPath="hdfs://localhost:9000/"
    val conf=new SparkConf().setAppName("WordCountApp")
    val sc=new SparkContext(conf)
    val fileData=sc.textFile(textFile,2).cache()
    val hdfsData=sc.textFile(hdfsPath)
    val countNum=fileData.flatMap(_.split(" ")).map((_,1)).reduceByKey(_ + _)
    println("********************************************************************************")
//    println("README WORLD COUNT RESULT: "+countNum)
//    countNum.foreach(e=>{
//      val (k,v)=e
//      println(k+" -> "+v)
//    })
    println(hdfsData)
    countNum.collect()
    println("********************************************************************************")
  }

}
