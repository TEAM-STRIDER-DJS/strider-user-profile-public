package com.strider.user_profile;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {
		"com.strider.user_profile",
		"com.strider.strider_common_lib"
})
@ConfigurationPropertiesScan
@EnableFeignClients
public class UserProfileApplication {
	public static void main(String[] args) {
		SpringApplication.run(UserProfileApplication.class, args);
	}
}
