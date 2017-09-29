package hub.gateway.portal

import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*

@RestController
class AppController {
    companion object {
        private val s_logger = LoggerFactory.getLogger(AppController::class.java)
    }
}

open class ArgAppBasic{
    lateinit var token:String
}

class ArgCreateApp : ArgAppBasic(){
    lateinit var name:String
}