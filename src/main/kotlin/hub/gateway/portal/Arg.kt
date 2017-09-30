package hub.gateway.portal

open class ArgBasic{
    /**
     * 客户端传入的token实际上由2部分组合而成:用户uid32字符+会话token16字符
     */
    lateinit var token:String
}