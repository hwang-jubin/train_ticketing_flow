package com.example.train_ticketing_flow.controller;

import com.example.train_ticketing_flow.service.UserQueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
public class WaitingRoomController {
    private final UserQueueService userQueueService;
    @GetMapping("/waiting-room")
    Mono<Rendering> waitingRoom(@RequestParam(name="queue",  defaultValue = "default") String queue,
                                @RequestParam(name = "user_Id") Long userId,
                                @RequestParam(name="redirect_url") String redirectUrl){
        //대기 등록
        //웹페이지 필요한 데이터를 전달
        //입장이 허용되어 page redirect가 가능한 상태인지 확인하기
        //어디로 이동해야 하는가?
        return userQueueService.isAllowed(queue, userId)
                .filter(allowed -> allowed)
                .flatMap(allowed -> Mono.just(Rendering.redirectTo(redirectUrl).build()))
                .switchIfEmpty(
                        userQueueService.RegisterWaitQue(queue, userId)
                                .onErrorResume(ex -> userQueueService.getRank(queue, userId))
                                .map(rank -> Rendering.view("waiting-room.html")
                                        .modelAttribute("number", rank)
                                        .modelAttribute("userId", userId)
                                        .modelAttribute("queue", queue)
                                        .build()
                                )
                );

    }
}
