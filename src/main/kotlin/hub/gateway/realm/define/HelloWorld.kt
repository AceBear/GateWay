package hub.gateway.realm.define

import hub.gateway.realm.DataRealm
import hub.gateway.realm.DataRealmVersion
import hub.gateway.realm.Func

abstract class HelloWorld : DataRealm() {
    override val id = "HelloWorld"
    override val chs = "供试验目的使用"
}

class HelloWorld_Slim_100: HelloWorld(){
    override val level = 1
    override val ver = DataRealmVersion(1, 0, 0)

    init {
        val fnHelloWorld = Func()
        fnHelloWorld.url = "hello-world"
        fnHelloWorld.chs = "返回一个字符串,可以用来确认通信是否正常"
        fnHelloWorld.httpMethod = "GET"

        funcs = arrayListOf(fnHelloWorld)
    }
}