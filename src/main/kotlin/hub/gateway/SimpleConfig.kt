package hub.gateway

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.*
import org.springframework.web.servlet.config.annotation.*


@Configuration
@EnableWebMvc
open class WebConfig : WebMvcConfigurerAdapter() {

    @Autowired
    lateinit var siteConfig:SiteConfig

    open override fun addCorsMappings(registry: CorsRegistry?) {
        registry!!.addMapping("/portal/**")
                .allowedOrigins(*siteConfig.sites.toTypedArray())
    }
}

@Configuration
@ConfigurationProperties(prefix="gateway")
open class SiteConfig{
    var sites: List<String> = ArrayList<String>()
}