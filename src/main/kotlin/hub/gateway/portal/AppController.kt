package hub.gateway.portal

import hub.gateway.mgr.*
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*

@RestController
class AppController {
    companion object {
        private val s_logger = LoggerFactory.getLogger(AppController::class.java)
    }

    @RequestMapping(path=arrayOf("/portal/org/{oid}/app/create"), method=arrayOf(RequestMethod.POST))
    fun createOrg(@RequestBody arg: ArgCreateApp) {
        // 从token找到用户
        val sess = Session(arg.token)
        val usr = Mgrs.userMgr.findUserBySession(sess.uid, sess.token)
        check(usr != null){ "token is invalid" }
    }
}

class ArgCreateApp : ArgBasic(){
    lateinit var name: String
    lateinit var baseUrl: String
}