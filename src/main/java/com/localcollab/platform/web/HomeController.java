package com.localcollab.platform.web;

import com.localcollab.platform.service.InMemoryRoomService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final InMemoryRoomService roomService;

    public HomeController(InMemoryRoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("rooms", roomService.findAll());
        return "index";
    }
}
