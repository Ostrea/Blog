package my.ostrea.blog.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ArticleNotFoundException  extends RuntimeException {
    public ArticleNotFoundException() {
        super("Article wasn't found.");
    }
}
