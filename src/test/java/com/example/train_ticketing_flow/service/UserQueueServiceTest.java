package com.example.train_ticketing_flow.service;

import com.example.train_ticketing_flow.EmbeddedRedis;
import com.example.train_ticketing_flow.exception.ApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.ReactiveRedisConnection;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

@SpringBootTest
@Import(EmbeddedRedis.class)
@ActiveProfiles("test")
class UserQueueServiceTest {

    @Autowired
    private UserQueueService userQueueService;
    @Autowired
    private ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

//    각 test 격리
    @BeforeEach
    public void beforeEach(){
        ReactiveRedisConnection reactiveConnection = reactiveRedisTemplate.getConnectionFactory().getReactiveConnection();
        reactiveConnection.serverCommands().flushAll().subscribe();

    }
    //StepVerifier.create는 Reactor 프로젝트의 테스트 도구 중 하나로, 리액티브 스트림의 동작을 확인하는 데 사용
    //첫번째로 넣으면 대기열 1번 부여
    @Test
    void registerWaitQue() {
        StepVerifier.create(userQueueService.RegisterWaitQue("default", 100L))
                .expectNext(1L)
                .verifyComplete();

        StepVerifier.create(userQueueService.RegisterWaitQue("default", 101L))
                .expectNext(2L)
                .verifyComplete();

        StepVerifier.create(userQueueService.RegisterWaitQue("default", 102L))
                .expectNext(3L)
                .verifyComplete();
    }

    @Test
    void alreadyRegisteredWaitQueue(){
        StepVerifier.create(userQueueService.RegisterWaitQue("default", 100L))
                .expectNext(1L)
                .verifyComplete();

        StepVerifier.create(userQueueService.RegisterWaitQue("default", 100L))
                .expectError(ApplicationException.class)
                .verify();


    }
// 기존에 등록된 사용자가 없기 때문에 0명이 return
    @Test
    void emptyAllowUser() {
        StepVerifier.create(userQueueService.allowUser("default", 3L))
                .expectNext(0L)
                .verifyComplete();
    }
// 사용자 추가 후 3개에 대해서 2개를 요청했고 2개를 processor 에 넣을 수 있으니까 2L이 return
    @Test
    void isAllowed() {
        StepVerifier.create(userQueueService.RegisterWaitQue("default", 100L)
                .then(userQueueService.RegisterWaitQue("default", 101L))
                .then(userQueueService.RegisterWaitQue("default", 102L))
                .then(userQueueService.allowUser("default", 2L)))
                .expectNext(2L)
                .verifyComplete();
    }

    @Test
    void isAllowed2() {
        StepVerifier.create(userQueueService.RegisterWaitQue("default", 100L)
                        .then(userQueueService.RegisterWaitQue("default", 101L))
                        .then(userQueueService.RegisterWaitQue("default", 102L))
                        .then(userQueueService.allowUser("default", 5L)))
                .expectNext(3L)
                .verifyComplete();
    }

    @Test
    void getRank() {
        StepVerifier.create(userQueueService.RegisterWaitQue("default", 100L)
                        .then(userQueueService.getRank("default", 100L)))
                .expectNext(1L)
                .verifyComplete();

        StepVerifier.create(userQueueService.RegisterWaitQue("default", 101L)
                        .then(userQueueService.getRank("default", 101L)))
                .expectNext(2L)
                .verifyComplete();
    }
}