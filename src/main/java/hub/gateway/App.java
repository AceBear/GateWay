package hub.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class App implements CommandLineRunner{
    private static Logger s_logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args){
        s_logger.info("appver: {}", (new GitVer()).getVersion());
        SpringApplication.run(App.class, args);
    }

    public void run(String... args) throws Exception{
        Runtime.getRuntime().addShutdownHook(new Thread(()-> s_logger.info("Shutdown")));
    }
}
