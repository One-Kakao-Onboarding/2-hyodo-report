package com.example.spring.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA Auditing 설정
 * @CreatedDate, @LastModifiedDate 어노테이션 활성화
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
