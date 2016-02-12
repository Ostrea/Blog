package my.ostrea.blog.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class CantEditNotYoursArticlesException extends RuntimeException {
    public CantEditNotYoursArticlesException() {
        super("You can't edit other people's articles!");
    }
}
