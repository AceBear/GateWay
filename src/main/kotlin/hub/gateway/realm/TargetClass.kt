package hub.gateway.realm

import com.alibaba.fastjson.*
import org.slf4j.LoggerFactory

open class TargetClass(val key:String, val chs:String)

class TargetClassWithChildren(tc: TargetClass, val children:List<TargetClass>) :TargetClass(tc.key, tc.chs)

/**
 * 对现实世界中的事务的一个分类
 * 构成一个单根有向不循环图
 * 注意:并不构成树,因为允许存在有多个父节点的节点
 */
object targetClassHolder{

    private val logger = LoggerFactory.getLogger(targetClassHolder.javaClass)
    /**
     * 这是一个JSON形式的分类描述
     */
    private val json = """
        |{
        |   "All": {
        |       "chs": "全部",
        |       "children": [ "SandBox", "Vehicle" ]
        |   },
        |   "SandBox":{
        |       "chs": "沙箱",
        |   },
        |   "Vehicle": {
        |       "chs": "汽车",
        |       "children": [ "Truck", "Car" ]
        |   },
        |   "Truck": {
        |       "chs": "卡车",
        |       "children": [ "ElectricVehicle" ]
        |   },
        |   "Car": {
        |       "chs": "小型汽车",
        |       "children": [ "FuelCar", "ElectricVehicle" ]
        |   },
        |   "FuelCar":{
        |       "chs": "燃油车"
        |   },
        |   "ElectricVehicle": {
        |       "chs": "电动车"
        |   }
        |}
        """.trimMargin()

    val jsonObj = JSON.parseObject(json)

    /**
     * 获得根节点
     */
    fun getRoot():TargetClass{
        val key = "All"
        return getNode(key)!!
    }

    /**
     * 获得一个特定的节点
     */
    fun getNode(key:String):TargetClass?{
        val clsX = jsonObj[key] as JSONObject?
        return clsX?.let{
            TargetClass(key, clsX["chs"] as String)
        }
    }

    /**
     * 获得其所有子节点
     */
    fun getChildren(key:String):List<TargetClass> {
        val clsP = jsonObj[key] as JSONObject?

        return clsP?.let{
            val children = clsP["children"] as JSONArray?
            children?.map({
                logger.info("$it")
                val cls = jsonObj[it] as JSONObject
                TargetClass(it as String, cls["chs"] as String)
            }) ?: ArrayList<TargetClass>()
        } ?: ArrayList<TargetClass>()
    }
}