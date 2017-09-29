package hub.gateway.portal

import hub.gateway.GitVer
import org.springframework.web.bind.annotation.*

@RestController
class HomeController {

    @RequestMapping(path= arrayOf("/portal/version"))
    fun version(): GitVer {
        return GitVer()
    }
}