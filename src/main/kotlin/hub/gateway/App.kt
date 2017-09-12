package hub.gateway

import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication


@SpringBootApplication
open class App : CommandLineRunner {
    companion object {
        var s_logger = LoggerFactory.getLogger(App::class.java)

        @JvmStatic fun main(args: Array<String>){
            s_logger.info("appver: ${GitVer().version()}")
            SpringApplication.run(App::class.java, *args)
        }
    }

    override fun run(vararg args: String){

        Runtime.getRuntime().addShutdownHook(Thread({ s_logger.info("Shutting down....") }))
    }
}