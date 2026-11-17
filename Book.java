import java.io.Serializable;

public class Book implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String bookId;
    private final String title;
    private final String author;
    private final int totalCopies;
    private int availableCopies;

    public Book(String bookId, String title, String author, int copies) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.totalCopies = copies;
        this.availableCopies = copies;
    }

    public String getBookId() { return bookId; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public int getTotalCopies() { return totalCopies; }
    public int getAvailableCopies() { return availableCopies; }
    public boolean isAvailable() { return availableCopies > 0; }

    public void issueCopy() {
        if (isAvailable()) availableCopies--;
    }

    public void returnCopy() {
        if (availableCopies < totalCopies) availableCopies++;
    }
}
