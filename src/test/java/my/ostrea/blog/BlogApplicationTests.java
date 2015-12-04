package my.ostrea.blog;

import my.ostrea.blog.controllers.BaseController;
import my.ostrea.blog.models.Article;
import my.ostrea.blog.models.ArticleRepository;
import my.ostrea.blog.models.MyUser;
import my.ostrea.blog.models.UserRepository;
import my.ostrea.blog.utils.TestUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Iterator;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.*;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = BlogApplication.class)
@WebAppConfiguration
public class BlogApplicationTests {
    private static final String USERNAME_FOR_TESTS = "test";

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    public void accessingStartPageAsAnonymousShouldReturnViewNamedIndexAndModelWithZeroAttributes()
            throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeDoesNotExist("articles"));
    }

	@Test
    @WithUserDetails("test")
	public void accessingStartPageAsLoggedUserShouldReturnViewNamedIndexAndModelWithOneAttribute() throws Exception {
        mockMvc.perform(
                get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("articles"));
    }

    @Test
    public void accessingUsernamePageAsAnonymousShouldRedirectToLoginPage()
            throws Exception {
        mockMvc.perform(get("/" + USERNAME_FOR_TESTS))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser("userForTests")
    public void accessingUsernamePageAsLoggedUserIfUsernameIsNotLoggedUsernameShouldShowIndexViewWithModelWithOneAttr()
            throws Exception {
        mockMvc.perform(get("/" + USERNAME_FOR_TESTS))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("articles"));
    }

    @Test
    @WithMockUser("userForTests")
    public void accessingNotExistingUsernamePageAsLoggedUserShouldShowIndexViewWithModelWithZeroAttr()
            throws Exception {
        mockMvc.perform(get("/not_existing_user"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeDoesNotExist("articles"));
    }

    @Test
    @WithUserDetails(USERNAME_FOR_TESTS)
    public void accessingYourOwnUsernamePageAsLoggedUserShouldRedirectToIndex()
            throws Exception {
        mockMvc.perform(get("/" + USERNAME_FOR_TESTS))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    @WithMockUser("userForTests")
    public void deletingNotExistingArticleShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/delete_article?article_id=-1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser("userForTests")
    public void deletingOtherPersonArticleShouldReturnForbidden() throws Exception {
        MyUser author = userRepository.findByUsername(USERNAME_FOR_TESTS).get();
        Article article = new Article("test", "test", author);
        Article savedArticle = articleRepository.save(article);

        mockMvc.perform(get("/delete_article?article_id=" + savedArticle.getId()))
                .andExpect(status().isForbidden());

        articleRepository.delete(savedArticle);
    }

    @Test
    @WithUserDetails(USERNAME_FOR_TESTS)
    public void deletingExistingArticleShouldRedirectToUsersPage() throws Exception {
        MyUser author = userRepository.findByUsername(USERNAME_FOR_TESTS).get();
        Article article = new Article("test", "test", author);
        Article savedArticle = articleRepository.save(article);

        mockMvc.perform(get("/delete_article?article_id=" + savedArticle.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(USERNAME_FOR_TESTS));

        articleRepository.delete(savedArticle);
    }

    @Test
    public void accessingCreateArticlePageAsAnonymousShouldRedirectToLoginPage()
            throws Exception {
        mockMvc.perform(get("/create_article"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithUserDetails(USERNAME_FOR_TESTS)
    public void createArticleWithTitleGreaterThan255ShouldReturnViewWithTitleError() throws Exception {
        mockMvc.perform(post("/create_article")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("title", TestUtil.createStringWithLength(256))
                .param("content", "")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("create_article"))
                .andExpect(model().attributeExists("titleError"))
                .andExpect(model().attributeDoesNotExist("contentError"));
    }

    @Test
    @WithUserDetails(USERNAME_FOR_TESTS)
    public void createArticleWithContentGreaterThan255ShouldReturnViewWithContentError() throws Exception {
        mockMvc.perform(post("/create_article")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("title", "")
                .param("content", TestUtil.createStringWithLength(256))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("create_article"))
                .andExpect(model().attributeExists("contentError"))
                .andExpect(model().attributeDoesNotExist("titleError"));
    }

    @Test
    @WithUserDetails(USERNAME_FOR_TESTS)
    public void createArticleWithContentAndTitleGreaterThan255ShouldReturnViewWithContentAndTitleErrors()
            throws Exception {
        mockMvc.perform(post("/create_article")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("title", TestUtil.createStringWithLength(256))
                .param("content", TestUtil.createStringWithLength(256))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("create_article"))
                .andExpect(model().attributeExists("titleError"))
                .andExpect(model().attributeExists("contentError"));
    }
}
