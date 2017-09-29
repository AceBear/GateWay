package hub.gateway.portal

import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*

@RestController
class AppController {
    companion object {
        private val s_logger = LoggerFactory.getLogger(AppController::class.java)
    }
}

class ArgCreateApp{
    lateinit var token:String
    lateinit var name:String
}