package my.ostrea.blog.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class CantDeleteNotYoursArticlesException extends RuntimeException {
    public CantDeleteNotYoursArticlesException() {
        super("You can't delete other people's articles!");
    }
}
