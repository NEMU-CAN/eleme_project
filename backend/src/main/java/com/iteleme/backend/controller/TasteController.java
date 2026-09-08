package com.iteleme.backend.controller;

import com.iteleme.backend.common.Result;
import com.iteleme.backend.service.TasteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tastes")
public class TasteController {
    private final TasteService tasteService;

    @GetMapping
    public Result list() {
        return Result.success(tasteService.list());
    }
}
