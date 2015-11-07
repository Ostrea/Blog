package my.ostrea.blog.controllers;

import my.ostrea.blog.models.MyUser;
import my.ostrea.blog.models.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Optional;

@Controller
@RequestMapping("/")
public class BaseController {

    @Autowired
    UserRepository userRepository;

    /**
     * Handles '/'
     * @param model
     * @return view name
     */
    @RequestMapping
    public String index(Model model) {
        Optional<MyUser> userFromDb = userRepository
                .findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        userFromDb.map(user -> model.addAttribute("articles", user.getArticles()));

        return "index";
    }

    @RequestMapping("admin")
    public String admin() {
        return "admin";
    }

}
