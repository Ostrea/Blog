package my.ostrea.blog.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/")
public class BaseController {
    /**
     * Handles '/'
     * @param model
     * @return view name
     */
    @RequestMapping
    public String index(Model model) {
        model.addAttribute("password", "");
        model.addAttribute("username", "");
        return "index";
    }

    @RequestMapping(value = "login", method = RequestMethod.GET)
    public String login() {
        return "login";
    }

    @RequestMapping(value = "admin", method = RequestMethod.GET)
    public String admin() {
        return "admin";
    }
}
