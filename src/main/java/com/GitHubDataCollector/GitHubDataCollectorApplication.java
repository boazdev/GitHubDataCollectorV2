package com.GitHubDataCollector;

import com.GitHubDataCollector.service.AutoUpdateService;
import com.GitHubDataCollector.service.GitHubService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootApplication
public class GitHubDataCollectorApplication {
	ExecutorService executorService = Executors.newFixedThreadPool(1);

	@Autowired
	AutoUpdateService autoUpdateService;

	public static void main(String[] args) throws IOException, InterruptedException {
		SpringApplication.run(GitHubDataCollectorApplication.class, args);
	}

//	@PostConstruct
//	public void init() throws IOException, InterruptedException {
//		executorService.execute(() -> {
//			try {
//				autoUpdateService.startAutoUpdate();
//			} catch (IOException | InterruptedException e) {
//				throw new RuntimeException(e);
//			}
//		});
//	}
}
