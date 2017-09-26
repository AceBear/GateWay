package hub.gateway.portal

import hub.gateway.mgr.Org
import hub.gateway.ots.Repos
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*

@CrossOrigin
@RestController
class OrgController {
    companion object {
        private val s_logger = LoggerFactory.getLogger(OrgController::class.java)
    }

    @RequestMapping(path=arrayOf("/portal/org/all"), method=arrayOf(RequestMethod.POST))
    fun getAllOrgs(@RequestBody arg: ArgGetAllOrgs):List<Org>{
        // 从token找到用户
        val uid = Repos.sessRepo.findSession(arg.token)
        if(uid === null) throw RuntimeException("token is invalid")

        val listOrgs = Repos.orgRepo.getOrgs(uid)

        return listOrgs
    }

    @RequestMapping(path=arrayOf("/portal/org/create"), method=arrayOf(RequestMethod.POST))
    fun createOrg(@RequestBody arg: ArgCreateOrg):Org{
        // 从token找到用户
        val uid = Repos.sessRepo.findSession(arg.token)
        if(uid === null) throw RuntimeException("token is invalid")

        // 创建
        val orgNew = Repos.orgRepo.createOrg(uid, arg.name)

        return orgNew
    }

    @RequestMapping(path=arrayOf("/portal/org/delete"), method=arrayOf(RequestMethod.POST))
    fun deleteOrg(@RequestBody arg: ArgDeleteOrg){
        // 从token找到用户
        val uid = Repos.sessRepo.findSession(arg.token)
        if(uid === null) throw RuntimeException("token is invalid")

        // 删除
        Repos.orgRepo.deleteOrg(uid, arg.oid)
    }
}

class ArgGetAllOrgs{
    lateinit var token:String
}

class ArgCreateOrg{
    lateinit var token:String
    lateinit var name:String
}

class ArgDeleteOrg{
    lateinit var token:String
    var oid = 0
}