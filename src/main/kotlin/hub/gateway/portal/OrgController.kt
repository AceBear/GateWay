package hub.gateway.portal

import hub.gateway.agent.*
import hub.gateway.repo.Repos
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
        val sessAgent = Repos.sessRepo.findSession(arg.token.substring(0, 32), arg.token.substring(32))
        check(sessAgent != null){ "token is invalid" }

        val listOrgs = Repos.orgRepo.getOrgs(sessAgent!!.uid)

        return listOrgs.map({ o -> o.toOrg() })
    }

    @RequestMapping(path=arrayOf("/portal/org/single"), method=arrayOf(RequestMethod.POST))
    fun getAllOrgs(@RequestBody arg: ArgSingleOrg):Org?{
        // 从token找到用户
        val sessAgent = Repos.sessRepo.findSession(arg.token.substring(0, 32), arg.token.substring(32))
        check(sessAgent != null){ "token is invalid" }

        val org = Repos.orgRepo.getOrg(sessAgent!!.uid, arg.oid)
        return org?.toOrg()
    }

    @RequestMapping(path=arrayOf("/portal/org/create"), method=arrayOf(RequestMethod.POST))
    fun createOrg(@RequestBody arg: ArgCreateOrg):Org{
        // 从token找到用户
        val sessAgent = Repos.sessRepo.findSession(arg.token.substring(0, 32), arg.token.substring(32))
        check(sessAgent != null){ "token is invalid" }

        // 创建
        val orgNew = Repos.orgRepo.createOrg(sessAgent!!.uid, arg.name)

        return orgNew.toOrg()
    }

    @RequestMapping(path=arrayOf("/portal/org/delete"), method=arrayOf(RequestMethod.POST))
    fun deleteOrg(@RequestBody arg: ArgSingleOrg){
        // 从token找到用户
        val sessAgent = Repos.sessRepo.findSession(arg.token.substring(0, 32), arg.token.substring(32))
        check(sessAgent != null){ "token is invalid" }

        // 删除
        Repos.orgRepo.deleteOrg(sessAgent!!.uid, arg.oid)
    }
}

class ArgGetAllOrgs{
    lateinit var token:String
}

class ArgCreateOrg{
    lateinit var token:String
    lateinit var name:String
}

class ArgSingleOrg {
    lateinit var token:String
    var oid = 0
}