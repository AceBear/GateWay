package hub.gateway.mgr

import hub.gateway.realm.DataRealm

open class AppNameOnly(val id:String):Agent(){
    lateinit var name:String
}

class App(id: String) : AppNameOnly(id) {
    lateinit var base: String
    lateinit var uid: String
    var oid: Int = 0

    /**
     * 支持的功能
     * target.realm - > DataRealm
     */
    val abi = HashMap<String, HashMap<String, DataRealm>>()

    constructor(id:String, src: App):this(id){
        name = src.name
        base = src.base
        uid = src.uid
        oid = src.oid
    }
}