package my.ostrea.blog.controllers;

import my.ostrea.blog.exceptions.ArticleNotFoundException;
import my.ostrea.blog.exceptions.CantDeleteNotYoursArticlesException;
import my.ostrea.blog.models.Article;
import my.ostrea.blog.models.ArticleRepository;
import my.ostrea.blog.models.MyUser;
import my.ostrea.blog.models.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/")
public class BaseController {
    private final UserRepository userRepository;
    private final ArticleRepository articleRepository;

    @Autowired
    public BaseController(UserRepository userRepository, ArticleRepository articleRepository) {
        this.userRepository = userRepository;
        this.articleRepository = articleRepository;
    }

    /**
     * Handles '/'
     * @param model model to which add attributes
     * @return view name
     */
    @RequestMapping
    public String index(Model model) {
        if (!SecurityContextHolder.getContext().getAuthentication().getName().equals("anonymousUser")) {
            getUserFromDbAndAddHisInfoToThePage(model,
                    SecurityContextHolder.getContext().getAuthentication().getName());
        }

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
        Optional<Article> articleFromDb = Optional.ofNullable(articleRepository.findOne(articleId));
        articleFromDb.map(article -> {
            if (article.getAuthor().getUsername()
                    .equals(SecurityContextHolder.getContext().getAuthentication().getName())) {
                articleRepository.delete(article);
            } else {
                throw new CantDeleteNotYoursArticlesException();
            }
            return Optional.of(article);
        }).orElseThrow(ArticleNotFoundException::new);
        return "redirect:" + articleFromDb.map(Article::getAuthor).map(MyUser::getUsername);
    }

    @RequestMapping(value = "/create_article", method = RequestMethod.GET)
    public String createArticle(Model model) {
        model.addAttribute("article", new Article());
        return "create_article";
    }

    @RequestMapping(value = "/create_article", method = RequestMethod.POST)
    public String createArticle(@ModelAttribute Article article) {
        MyUser author = userRepository.findByUsername(
                SecurityContextHolder.getContext().getAuthentication().getName()).get();
        article.setAuthor(author);
        articleRepository.save(article);
        return "redirect:/";
    }
}
