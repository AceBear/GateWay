package hub.gateway.realm

/* TargetClass以后要从数据源加载
   当前暂时先硬编码这2个RootTargetObject, SandBoxTargetObject
*/

open class TargetClass {
    companion object {
        fun GetRoot():TargetClass{
            return RootTargetObject
        }
    }

    var Id: String? = null

    fun GetChildren():Array<TargetClass>{
        if(this.Id == "ROOT"){
            return arrayOf(SandBoxTargetObject)
        }
        else{
           return arrayOf<TargetClass>()
        }
    }
}

object RootTargetObject:TargetClass(){
    init {
        this.Id = "ROOT"
    }
}

object SandBoxTargetObject: TargetClass(){
    init{
        this.Id = "SandBox"
    }
}