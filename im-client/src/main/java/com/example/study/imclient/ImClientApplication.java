package com.example.study.imclient;


import com.example.study.imclient.service.ImClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

/**
 * IM客户端启动类（Web版，支持HTTP接口调用）
 * 启动后同时加载：Spring Web容器 + Netty客户端连接
 * @author 编程导师
 */
@Slf4j
@SpringBootApplication
public class ImClientApplication {

    @Autowired
    private ImClientService imClientService;

    @Value("${server.port}")
    private int serverPort;

    public static void main(String[] args) {
        // 启动Spring Boot应用（包含Web容器）
        SpringApplication.run(ImClientApplication.class, args);
    }

    /**
     * 应用启动完成后执行（确保Web容器启动后再连接Netty服务端）
     */
    @EventListener(ApplicationReadyEvent.class)
    public void afterStartup() {
        log.info("=== IM客户端Web服务启动成功 ===");
        log.info("客户端HTTP接口地址：http://127.0.0.1:{}", serverPort);
        log.info("当前客户端用户ID：{}", imClientService.getUserId());
        log.info("==============================");
    }
}