package my.ostrea.blog.models;

public class ArticleDto {
    private String title;
    private String content;

    public ArticleDto() {
    }

    public ArticleDto(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
