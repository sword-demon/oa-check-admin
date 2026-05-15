package com.oa.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author wxvirus
 */
@SpringBootApplication
@EnableTransactionManagement
@MapperScan({"com.oa.admin.system.mapper", "com.oa.admin.approval.mapper"})
public class OaAdminApplication {
    public static void main(String[] args) {
        SpringApplication.run(OaAdminApplication.class, args);
    }
}
