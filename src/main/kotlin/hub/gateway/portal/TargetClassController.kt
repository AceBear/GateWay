package hub.gateway.portal

import hub.gateway.realm.*
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*

@RestController
class TargetClassController {
    companion object {
        private val s_logger = LoggerFactory.getLogger(TargetClassController::class.java)
    }

    /**
     * 根节点
     */
    @RequestMapping(path=arrayOf("/portal/class/single"))
    fun getRootCls(): TargetClassWithChildren {
        val root = targetClassHolder.getRoot()
        val list = targetClassHolder.getChildren(root.key)
        return TargetClassWithChildren(root, list)
    }

    /**
     * 单个CLASS查询
     */
    @RequestMapping(path=arrayOf("/portal/class/single/{key}"))
    fun getCls(@PathVariable key: String): TargetClassWithChildren? {
        val clsNode = targetClassHolder.getNode(key)
        val list = targetClassHolder.getChildren(key)
        return clsNode?.let{TargetClassWithChildren(clsNode, list)}
    }
}