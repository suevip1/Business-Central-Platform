package space.atnibam;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("space.atnibam.**.mapper")
public class TransactionPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(TransactionPlatformApplication.class, args);
    }

}