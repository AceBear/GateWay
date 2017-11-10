package hub.gateway.portal

import hub.gateway.mgr.*
import hub.gateway.realm.DataRealm
import hub.gateway.repo.AppAbility
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

    @RequestMapping(path=arrayOf("/portal/org/{oid}/app/mod"), method=arrayOf(RequestMethod.POST))
    fun modifyApp(@PathVariable oid: Int, @RequestBody arg: ArgModApp) {
        val pair = findUsrAndOrg(arg.token, oid)
        val app = Mgrs.appMgr.getApp(arg.app.appId)

        check(app != null && app.uid == pair.usr.id && app.oid == pair.org.id){ "App不存在" }

        // 如果实质上没有修改内容,什么也不做
        if(app!!.name == arg.app.name && app.base == arg.app.baseUrl) return

        // 修改内容
        app.name = arg.app.name
        app.base = arg.app.baseUrl
        Mgrs.appMgr.modifyApp(app)
    }

    @RequestMapping(path=arrayOf("/portal/org/{oid}/app"), method=arrayOf(RequestMethod.POST))
    fun getAllApps(@PathVariable oid: Int, @RequestBody arg: ArgBasic):List<AppNameOnly>{
        val pair = findUsrAndOrg(arg.token, oid)

        return Mgrs.appMgr.getApps(pair.usr.id, pair.org.id)
    }

    @RequestMapping(path=arrayOf("/portal/org/{oid}/app/{appId}"), method=arrayOf(RequestMethod.POST))
    fun getApp(@PathVariable oid: Int, @PathVariable appId:String, @RequestBody arg: ArgBasic):App{
        val pair = findUsrAndOrg(arg.token, oid)
        val app = Mgrs.appMgr.getApp(appId)

        check(app != null){ "App不存在" }
        check(pair.usr.id == app!!.uid && pair.org.id == app.oid){ "App不存在" }

        return app
    }

    @RequestMapping(path=arrayOf("/portal/org/{oid}/app/{appId}/ability"), method=arrayOf(RequestMethod.POST))
    fun addAbility(@PathVariable oid: Int, @PathVariable appId:String, @RequestBody arg: ArgAbility):App{
        val pair = findUsrAndOrg(arg.token, oid)
        val app = Mgrs.appMgr.getApp(appId)

        check(app != null){ "App不存在" }
        check(pair.usr.id == app!!.uid && pair.org.id == app.oid){ "App不存在" }

        Mgrs.appMgr.addAbility(app.id, arg.ability)

        return app
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

class ArgModApp: ArgBasic(){
    class ModField{
        lateinit var appId: String
        lateinit var name: String
        lateinit var baseUrl: String
    }
    lateinit var app: ModField
}

class ArgAbility: ArgBasic(){
    lateinit var ability: AppAbility
}

data class Usr_Org(val usr:User, val org:Org)