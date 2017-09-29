package hub.gateway.mgr

class OrgAgent(val id:Int): Agent() {
    lateinit var name:String
    var ts:Long = 0L
    lateinit var ownerUserId:String

    fun toOrg():Org{
        val org = Org()
        org.id = id
        org.name = name
        org.ownerUserId = ownerUserId
        org.ts = ts

        return org
    }
}

class Org{
    var id = 0
    lateinit var name:String
    lateinit var ownerUserId:String
    var ts = 0L
}