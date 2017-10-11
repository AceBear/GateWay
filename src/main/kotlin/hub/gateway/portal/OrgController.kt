package hub.gateway.portal

import hub.gateway.mgr.*
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*

@RestController
class OrgController {
    companion object {
        private val s_logger = LoggerFactory.getLogger(OrgController::class.java)
    }

    @RequestMapping(path=arrayOf("/portal/org/all"), method=arrayOf(RequestMethod.POST))
    fun getAllOrgs(@RequestBody arg: ArgBasic):List<OrgInfo>{
        // 从token找到用户
        val sess = Session(arg.token)
        val usr = Mgrs.userMgr.findUserBySession(sess.uid, sess.token)
        check(usr != null){ "token is invalid" }

        val listOrgs = Mgrs.orgMgr.getOrgs(usr!!.id)

        return listOrgs.map({ o -> OrgInfo(o) })
    }

    @RequestMapping(path=arrayOf("/portal/org/single"), method=arrayOf(RequestMethod.POST))
    fun getAllOrgs(@RequestBody arg: ArgSingleOrg): OrgInfo?{
        // 从token找到用户
        val sess = Session(arg.token)
        val usr = Mgrs.userMgr.findUserBySession(sess.uid, sess.token)
        check(usr != null){ "token is invalid" }

        val org = Mgrs.orgMgr.getOrg(usr!!.id, arg.oid)
        return if(org == null) null else OrgInfo(org)
    }

    @RequestMapping(path=arrayOf("/portal/org/create"), method=arrayOf(RequestMethod.POST))
    fun createOrg(@RequestBody arg: ArgCreateOrg): OrgInfo {
        // 从token找到用户
        val sess = Session(arg.token)
        val usr = Mgrs.userMgr.findUserBySession(sess.uid, sess.token)
        check(usr != null){ "token is invalid" }

        // 创建
        val orgNew = Mgrs.orgMgr .createOrg(usr!!.id, arg.name)

        return OrgInfo(orgNew)
    }

    @RequestMapping(path=arrayOf("/portal/org/delete"), method=arrayOf(RequestMethod.POST))
    fun deleteOrg(@RequestBody arg: ArgSingleOrg){
        // 从token找到用户
        val sess = Session(arg.token)
        val usr = Mgrs.userMgr.findUserBySession(sess.uid, sess.token)
        check(usr != null){ "token is invalid" }

        // 删除
        Mgrs.orgMgr.deleteOrg(usr!!.id, arg.oid)
    }
}

class ArgCreateOrg : ArgBasic(){
    lateinit var name:String
}

class ArgSingleOrg : ArgBasic() {
    var oid = 0
}