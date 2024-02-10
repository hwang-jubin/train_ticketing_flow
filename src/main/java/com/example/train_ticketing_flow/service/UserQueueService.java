package com.example.train_ticketing_flow.service;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;

import static com.example.train_ticketing_flow.exception.ErrorCode.QUEUE_ALREADY_REGISTERED_USER;

@RequiredArgsConstructor
@Service
public class UserQueueService {
    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;
    private final String User_Queue_Wait_Key = "users:queue:%s:wait";
    private final String User_Queue_Proceed_key = "users:queue:%s:proceed";
    //대기열 등록 api
    public Mono<Long> RegisterWaitQue(final String queue,final Long userId){
//        redis sortedSet
//        -key : userId
//        -value: unix timestamp
        long unixTimeStamp = Instant.now().getEpochSecond();
        //이미 값이 있으면 False, 없으면 True
        //false면 filter에 걸리지 않아서 아무것도 내려가지 않아 switchIfEmpty로 넘어감
        //순위가 0부터 시작하는것이 이상해서 1증가
        return reactiveRedisTemplate.opsForZSet().add(User_Queue_Wait_Key.formatted(queue), userId.toString(), unixTimeStamp)
                .filter(i -> i)
                .switchIfEmpty(Mono.error(QUEUE_ALREADY_REGISTERED_USER.build()))
                .flatMap(i -> reactiveRedisTemplate.opsForZSet().rank(User_Queue_Wait_Key.formatted(queue),userId.toString()))
                .map(i->i>=0?i+1:i);
    }

//    /**
//     * 진입 허용 메서드
//     *
//     */


}
