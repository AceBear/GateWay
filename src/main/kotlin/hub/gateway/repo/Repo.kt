package hub.gateway.repo

import hub.gateway.Application
import hub.gateway.repo.aliyunots.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

/**
 * 数据持久基类
  */
abstract class Repo{

    init {
        ensureInfrastructure()
    }

    /**
     * 确保基础存储设施就绪
     * 数据持久通常都需要预先准备对应的基础存储单元
     * 例如,对于数据库需要表,存储过程,视图等等
     * 对于阿里云表格存储或hadoop的hbase,需要数据存储表
     */
    protected abstract fun ensureInfrastructure()
}

/**
 * 集中管理所有的repos
 */
object Repos{
    val userRepo:IUserRepo
    val sessRepo:ISessionRepo
    val orgRepo:IOrgRepo

    init {
        val config = Application.getCtx().getBean(RepoConfig::class.java)
        if(config.repo.equals("AliYunOTS", true)){
            userRepo = UserRepoOTS()
            sessRepo = SessionRepoOTS()
            orgRepo = OrgRepoOTS()
        }
        else{
            TODO("当前只实现了AliYunOTS作为存储引擎的Repos")
        }
    }
}

/**
 * spring-boot的yml配置项目
 */
@Component
class RepoConfig{
    @Value("\${gateway.repo}")
    lateinit var repo:String
}