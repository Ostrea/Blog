package my.ostrea.blog.controllers;

import my.ostrea.blog.models.Article;
import my.ostrea.blog.models.ArticleRepository;
import my.ostrea.blog.models.MyUser;
import my.ostrea.blog.models.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
@RequestMapping("/")
public class BaseController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    ArticleRepository articleRepository;

    /**
     * Handles '/'
     * @param model
     * @return view name
     */
    @RequestMapping
    public String index(Model model) {
        getUserFromDbAndAddHisInfoToThePage(model,
                SecurityContextHolder.getContext().getAuthentication().getName());

        return "index";
    }

    private void getUserFromDbAndAddHisInfoToThePage(Model model, String username) {
        Optional<MyUser> userFromDb = userRepository
                .findByUsername(username);
        userFromDb.map(user -> model.addAttribute("articles", user.getArticles()));
    }

    @RequestMapping("/administrator_control_panel")
    public String administratorControlPanel() {
        return "administrator_control_panel";
    }

    @RequestMapping("/{username}")
    public String showUsersArticles(Model model, @PathVariable String username) {
        String currentlyLoggedUser = SecurityContextHolder.getContext().getAuthentication().getName();
        if (currentlyLoggedUser.equals(username)) {
            return "redirect:/";
        }

        getUserFromDbAndAddHisInfoToThePage(model, username);

        return "index";
    }

    @RequestMapping("/delete_article")
    public String deleteArticle(@RequestParam("article_id") Long articleId) {
        Article article = articleRepository.findOne(articleId);
        if (article.getAuthor().getUsername()
                .equals(SecurityContextHolder.getContext().getAuthentication().getName())) {
            articleRepository.delete(article);
        }
        return "redirect:" + article.getAuthor().getUsername();
    }
}
