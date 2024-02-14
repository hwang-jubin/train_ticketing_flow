package com.example.train_ticketing_flow.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.time.Instant;

import static com.example.train_ticketing_flow.exception.ErrorCode.QUEUE_ALREADY_REGISTERED_USER;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserQueueService {
    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;
    private final String USER_QUEUE_WAIT_KEY = "users:queue:%s:wait";
    private final String USER_QUEUE_WAIT_KEY_FOR_SCAN = "users:queue:*:wait";
    private final String User_Queue_Proceed_key = "users:queue:%s:proceed";

    //applicaion.yaml의 properties를 변수에 넣기
    @Value("${scheduler.enabled}")
    private boolean scheduling = false;



    //대기열 등록 api
    public Mono<Long> RegisterWaitQue(final String queue,final Long userId){
//        redis sortedSet
//        -key : userId
//        -value: unix timestamp
        long unixTimeStamp = Instant.now().getEpochSecond();
        //이미 값이 있으면 False, 없으면 True
        //false면 filter에 걸리지 않아서 아무것도 내려가지 않아 switchIfEmpty로 넘어감
        //순위가 0부터 시작하는것이 이상해서 1증가
        return reactiveRedisTemplate.opsForZSet().add(USER_QUEUE_WAIT_KEY.formatted(queue), userId.toString(), unixTimeStamp)
                .filter(i -> i)
                .switchIfEmpty(Mono.error(QUEUE_ALREADY_REGISTERED_USER.build()))
                .flatMap(i -> reactiveRedisTemplate.opsForZSet().rank(USER_QUEUE_WAIT_KEY.formatted(queue),userId.toString()))
                .map(i->i>=0?i+1:i);
    }

    /**
     * 진입 허용 메서드
     * 1.진입이 가능한 상태인지 조회
     * 2.진입을 허용
     * count : 몇개의 사용자를 허용할 것인지?
     */
    public Mono<Long> allowUser(final String queue, final Long count){
        //진입을 허용하는 단계
        //1.wait queue 사용자를 제거
        //2.proceed queue에 사용자를 추가(추가할 때, 진입 시간을 proceed로 넣는 시간으로 변경)
        return reactiveRedisTemplate.opsForZSet().popMin(USER_QUEUE_WAIT_KEY.formatted(queue), count)
                .flatMap(member -> reactiveRedisTemplate.opsForZSet().add(User_Queue_Proceed_key.formatted(queue),member.getValue(),Instant.now().getEpochSecond()))
                .count();
    }

    /**
     * 특정 회원이 진입이 가능한 상태인지 조회
     * proceed sortedSet에 있어서 ranking 이 1 이상이면 진입 가능하다고 판단
     */
    public Mono<Boolean> isAllowed(final String queue, final Long userId){
        return reactiveRedisTemplate.opsForZSet().rank(User_Queue_Proceed_key.formatted(queue),userId.toString())
                .defaultIfEmpty(-1L)
                .map(rank ->rank>=0);
    }

    public Mono<Long> getRank(final String queue, final Long userId){
        return reactiveRedisTemplate.opsForZSet().rank(USER_QUEUE_WAIT_KEY.formatted(queue), userId.toString())
                .defaultIfEmpty(-1L)
                .map(rank -> rank >= 0 ? rank + 1 : rank);


    }

    @Scheduled(initialDelay = 5000, fixedDelay = 10000)
    public void scheduleAllowUser() {
        //test code일때는 실행하지 않음
        if (!scheduling) {
            log.info("passed scheduling...");
            return;
        }
        log.info("called scheduling...");

        var maxAllowUserCount = 3L;

        reactiveRedisTemplate.scan(ScanOptions.scanOptions()
                        .match(USER_QUEUE_WAIT_KEY_FOR_SCAN)
                        .count(100)
                        .build())
                .map(key -> key.split(":")[2])
                .flatMap(queue -> allowUser(queue, maxAllowUserCount).map(allowed -> Tuples.of(queue, allowed)))
                .doOnNext(tuple -> log.info("Tried %d and allowed %d members of %s queue".formatted(maxAllowUserCount, tuple.getT2(), tuple.getT1())))
                .subscribe();
    }
}
