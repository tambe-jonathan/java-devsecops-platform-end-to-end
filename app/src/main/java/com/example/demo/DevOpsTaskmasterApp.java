package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
@Controller
public class DevOpsTaskmasterApp {

    private final List<String> tasks = new ArrayList<>();

    public static void main(String[] args) {
        SpringApplication.run(DevOpsTaskmasterApp.class, args);
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("tasks", tasks);
        model.addAttribute("hostname", getHostname());
        return "index";
    }

    @PostMapping("/add")
    public String addTask(@RequestParam String task) {
        if (task != null && !task.trim().isEmpty()) {
            tasks.add(task);
        }
        return "redirect:/";
    }

    @PostMapping("/delete")
    public String deleteTask(@RequestParam String task) {
        tasks.remove(task);
        return "redirect:/";
    }

    private String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "Taskmaster-Node-Unknown";
        }
    }
}
