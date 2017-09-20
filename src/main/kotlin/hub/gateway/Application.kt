package hub.gateway

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.ApplicationContext


@SpringBootApplication
open class Application : CommandLineRunner {

    companion object {
        var s_logger = LoggerFactory.getLogger(Application::class.java)
        private lateinit var _appCtx: ApplicationContext

        @JvmStatic fun main(args: Array<String>){
            s_logger.info("appver: ${GitVer().version()}")
            SpringApplication.run(Application::class.java, *args)
        }

        @JvmStatic fun getCtx():ApplicationContext{
            return _appCtx
        }
    }

    @Autowired
    private lateinit var _ctx:ApplicationContext

    override fun run(vararg args: String){
        _appCtx = _ctx
        Runtime.getRuntime().addShutdownHook(Thread({ s_logger.info("Shutting down....") }))
    }
}