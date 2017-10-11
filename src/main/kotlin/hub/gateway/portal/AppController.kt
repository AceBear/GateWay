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
    fun createApp(@PathVariable oid: Int, @RequestBody arg: ArgCreateApp):App {
        val pair = findUsrAndOrg(arg.token, oid)

        val app = App("")
        app.name = arg.name
        app.base = arg.baseUrl
        app.uid = pair.usr.id
        app.oid = pair.org.id
        return Mgrs.appMgr.createApp(app)
    }

    @RequestMapping(path=arrayOf("/portal/org/{oid}/app/delete"), method=arrayOf(RequestMethod.POST))
    fun deleteApp(@PathVariable oid: Int, @RequestBody arg: ArgDelApp) {
        val pair = findUsrAndOrg(arg.token, oid)

        Mgrs.appMgr.deleteApp(pair.usr.id, pair.org.id, arg.appId)
    }

    @RequestMapping(path=arrayOf("/portal/org/{oid}/app"), method=arrayOf(RequestMethod.POST))
    fun getAllApps(@PathVariable oid: Int, @RequestBody arg: ArgBasic):List<AppNameOnly>{
        val pair = findUsrAndOrg(arg.token, oid)

        return Mgrs.appMgr.getApps(pair.usr.id, pair.org.id)
    }

    private fun findUsrAndOrg(token:String, oid:Int):Usr_Org{
        // 从token找到用户
        val sess = Session(token)
        val usr = Mgrs.userMgr.findUserBySession(sess.uid, sess.token)
        check(usr != null){ "token is invalid" }

        // 从用户找到Org
        val org = Mgrs.orgMgr.getOrg(usr!!.id, oid)
        check(org != null){ "Organization($oid) doesn't exist" }

        return Usr_Org(usr, org!!)
    }
}

class ArgCreateApp : ArgBasic(){
    lateinit var name: String
    lateinit var baseUrl: String
}

class ArgDelApp: ArgBasic(){
    lateinit var appId: String
}

data class Usr_Org(val usr:User, val org:Org)