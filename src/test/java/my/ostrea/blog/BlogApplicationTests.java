package my.ostrea.blog;

import my.ostrea.blog.configurations.WebSecurityConfig;
import my.ostrea.blog.models.ArticleRepository;
import my.ostrea.blog.models.MyUser;
import my.ostrea.blog.models.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.Filter;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = BlogApplication.class)
@WebAppConfiguration
public class BlogApplicationTests {
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
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
        mockMvc.perform(get("/test"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser("userForTests")
    public void accessingUsernamePageAsLoggedUserIfUsernameIsNotLoggedUsernameShouldShowIndexViewWithModelWithOneAttr()
            throws Exception {
        mockMvc.perform(get("/admin"))
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
    @WithUserDetails("test")
    public void accessingYourOwnUsernamePageAsLoggedUserShouldRedirectToIndex()
            throws Exception {
        mockMvc.perform(get("/test"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }
}
