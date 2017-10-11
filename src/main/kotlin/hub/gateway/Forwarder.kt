package hub.gateway

import hub.gateway.mgr.App
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.RestTemplate

@CrossOrigin
@RestController
class Forwarder {
    @RequestMapping("/api/{targetClass}/{dataRealm}/{func}/{appId}/org/{oid}")
    fun forward(@PathVariable targetClass:String, @PathVariable dataRealm:String?, @PathVariable func:String?,
                  @PathVariable appId:String?, @PathVariable oid:Int) : String {

        // 转发往App.base
        var app = App("123456789") // 以后从存贮中根据appId查询得到
        var url = "${app.base}/$targetClass/$dataRealm/$func/org/$oid"

        var request = RestTemplate()
        var response = request.getForObject(url, String::class.java)

        return response
    }
}