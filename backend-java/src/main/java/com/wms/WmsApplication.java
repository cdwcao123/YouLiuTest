package com.wms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * WMS 仓储管理系统后端启动类。
 *
 * <p>技术栈：Java 17 + Spring Boot 3 + Spring Data JPA。
 * 启动时读取 src/main/resources/application.yml：
 * <ul>
 *   <li>数据源：MySQL（localhost:3306/wms），JPA ddl-auto=update 会自动建表；</li>
 *   <li>服务端口：8080，Swagger 文档地址：/swagger-ui.html。</li>
 * </ul>
 *
 * <p>启动完成后 DataInitializer 会插入商品/仓库/库位/库存示例数据（仅首次启动）。
 */
@SpringBootApplication
public class WmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(WmsApplication.class, args);
    }
}
