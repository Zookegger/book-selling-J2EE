package com.group.book_selling.config;

import java.util.concurrent.Executor;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Cấu hình xử lý bất đồng bộ (Asynchronous) cho ứng dụng.
 * 
 * Lớp này bật tính năng @Async của Spring và cung cấp cấu hình
 * cho thread pool dùng để thực thi các tác vụ bất đồng bộ,
 * ví dụ như gửi email.
 */
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    /**
     * Khởi tạo ThreadPoolTaskExecutor dành riêng cho các tác vụ
     * 
     * Cấu hình:
     * - corePoolSize: số thread cơ bản luôn duy trì
     * - maxPoolSize: số thread tối đa có thể tạo
     * - queueCapacity: số lượng task có thể chờ trong hàng đợi
     * - threadNamePrefix: tiền tố tên thread để dễ debug/log
     *
     * @return Executor dùng cho các tác vụ bất đồng bộ
     */
    public static Executor createExecutor(
        int corePoolSize,
        int maxPoolSize,
        int queueCapacity,
        String threadNamePrefix
    ) {
        ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
        ex.setCorePoolSize(corePoolSize);
        ex.setMaxPoolSize(maxPoolSize);
        ex.setQueueCapacity(queueCapacity);
        ex.setThreadNamePrefix(threadNamePrefix);
        ex.initialize();
        return ex;
    }

    @Bean(name = "emailTaskExecutor")
    public Executor emailTaskExecutor() {
        return createExecutor(4, 20, 200, "email-");
    }

    /**
     * Cung cấp Executor mặc định cho các phương thức được đánh dấu @Async.
     *
     * @return Executor mặc định (emailTaskExecutor)
     */
    @Override
    public Executor getAsyncExecutor() {
        return emailTaskExecutor();
    }

    /**
     * Xử lý các exception không được bắt trong các phương thức @Async
     * có kiểu trả về void.
     *
     * @return AsyncUncaughtExceptionHandler mặc định của Spring
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new SimpleAsyncUncaughtExceptionHandler();
    }
}