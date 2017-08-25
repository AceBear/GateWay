package hub.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class App implements CommandLineRunner{
    private static Logger s_logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args){
        s_logger.info("appver: {}", (new GitVer()).getVersion());
        SpringApplication.run(App.class, args);
    }

    public void run(String... args) throws Exception{
        for(String profile : env.getActiveProfiles()){
            if(profile.equals("dev")) {
                s_logger.warn("Please set OS env variable SPRING_PROFILES_ACTIVE in product mode.");
                break;
            }
        }

        Runtime.getRuntime().addShutdownHook(new Thread(()-> s_logger.info("Shutdown")));
    }

    @Autowired
    private Environment env;
}
