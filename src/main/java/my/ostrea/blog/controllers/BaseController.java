package my.ostrea.blog.controllers;

import my.ostrea.blog.exceptions.ArticleNotFoundException;
import my.ostrea.blog.exceptions.CantDeleteNotYoursArticlesException;
import my.ostrea.blog.exceptions.CantEditNotYoursArticlesException;
import my.ostrea.blog.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Optional;

@Controller
@RequestMapping("/")
public class BaseController {
    private final UserRepository userRepository;
    private final ArticleRepository articleRepository;

    private Long editedArticleId;
    private String  editedArticleAuthor;

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

        Optional<String> username = articleFromDb.map(Article::getAuthor).map(MyUser::getUsername);
        if (username.isPresent()) {
            return "redirect:" + username.get();
        } else {
            throw new RuntimeException("Something went completely wrong!");
        }
    }

    // TODO fix:anonymous user can create articles
    @RequestMapping(value = "/create_article", method = RequestMethod.GET)
    public String createArticle(Model model) {
        model.addAttribute("articleDto", new ArticleDto());
        return "create_article";
    }

    @RequestMapping(value = "/create_article", method = RequestMethod.POST)
    public String  createArticle(Model model, @ModelAttribute ArticleDto article) {
        boolean errors = false;
        if (article.getTitle().length() > 255) {
            errors = true;
            model.addAttribute("titleError", true);
        }
        if (article.getContent().length() > 255) {
            errors = true;
            model.addAttribute("contentError", true);
        }
        if (errors) {
            return "create_article";
        }

        MyUser author = userRepository.findByUsername(
                SecurityContextHolder.getContext().getAuthentication().getName()).get();
        Article articleForDb = new Article(article.getTitle(), article.getContent(), author);
        articleRepository.save(articleForDb);
        return "redirect:/";
    }

    @RequestMapping(value = "/edit_article", method = RequestMethod.GET)
    public String editArticle(Model model, @RequestParam("article_id") Long articleId) {
        Optional<Article> articleFromDb = Optional.ofNullable(articleRepository.findOne(articleId));
        articleFromDb.map(article -> {
            if (!article.getAuthor().getUsername()
                    .equals(SecurityContextHolder.getContext().getAuthentication().getName())) {
                throw new CantEditNotYoursArticlesException();
            }
            model.addAttribute("articleDto", new ArticleDto(article.getTitle(), article.getContent()));
            editedArticleAuthor = article.getAuthor().getUsername();
            editedArticleId = articleId;

            return Optional.of(article);
        }).orElseThrow(ArticleNotFoundException::new);
        return "edit_article";
    }

    @RequestMapping(value = "/edit_article", method = RequestMethod.POST)
    public String editArticle(Model model, @ModelAttribute ArticleDto article) {
        if (editedArticleId == null || editedArticleAuthor == null || !editedArticleAuthor.equals(
                SecurityContextHolder.getContext().getAuthentication().getName())) {
            throw new CantEditNotYoursArticlesException();
        }

        boolean errors = false;
        if (article.getTitle().length() > 5) {
            errors = true;
            model.addAttribute("titleError", true);
        }
        if (article.getContent().length() > 5) {
            errors = true;
            model.addAttribute("contentError", true);
        }
        if (errors) {
            return "edit_article?article_id=" + editedArticleId;
        }

        MyUser author = userRepository.findByUsername(
                SecurityContextHolder.getContext().getAuthentication().getName()).get();
        Article articleForDb = new Article(article.getTitle(), article.getContent(), author);
        articleForDb.setId(editedArticleId);
        articleRepository.save(articleForDb);
        editedArticleId = null;
        return "redirect:/";
    }
}
