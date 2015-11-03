package my.ostrea.blog.configurations;

import java.util.*;
import java.util.stream.Collectors;

import my.ostrea.blog.models.MyUser;
import my.ostrea.blog.models.UserRepository;
import my.ostrea.blog.models.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("userDetailsService")
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Transactional(readOnly=true)
    @Override
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
        Optional<MyUser> userFromDatabase = userRepository.findByUsername(username);

        return userFromDatabase.map(user -> {
            List<GrantedAuthority> authorities = buildUserAuthority(user.getUserRoles());
            return buildUserForAuthentication(user, authorities);
        }).orElseThrow(() -> new UsernameNotFoundException("User " + username + " was not found in the database"));
    }

    /**
     * Converts from application user to Spring Security user
     * @param user application user
     * @param authorities user's authorities
     * @return Spring Security user
     */
    private User buildUserForAuthentication(MyUser user,
                                            List<GrantedAuthority> authorities) {
        return new User(user.getUsername(), user.getPassword(),
                user.isEnabled(), true, true, true, authorities);
    }

    private List<GrantedAuthority> buildUserAuthority(Set<UserRole> userRoles) {
        Set<GrantedAuthority> authorities = new HashSet<>();

        // Build user's authorities
        authorities.addAll(userRoles.stream()
                .map(userRole -> new SimpleGrantedAuthority(userRole.getRole())).collect(Collectors.toList()));

        return new ArrayList<>(authorities);
    }

}