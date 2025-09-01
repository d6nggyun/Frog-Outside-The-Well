package com._oormthon.seasonthon.domain.todo.controller;

import com._oormthon.seasonthon.domain.todo.service.TodoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/todos")
public class TodoController {

    private final TodoService todoService;


}
