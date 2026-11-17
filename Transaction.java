import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Transaction implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final double FINE_PER_DAY = 15.0;

    private final String transactionId;
    private final String memberId;
    private final String bookId;
    private final LocalDate issueDate;
    private final LocalDate dueDate;

    private LocalDate returnDate;
    private double fine;
    private boolean returned;

    public Transaction(String transactionId, String memberId,
                       String bookId, LocalDate issueDate) {
        this.transactionId = transactionId;
        this.memberId = memberId;
        this.bookId = bookId;
        this.issueDate = issueDate;
        this.dueDate = issueDate.plusDays(14);
        this.returned = false;
        this.fine = 0;
    }

    public String getTransactionId() { return transactionId; }
    public String getMemberId() { return memberId; }
    public String getBookId() { return bookId; }
    public LocalDate getIssueDate() { return issueDate; }
    public LocalDate getDueDate() { return dueDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public double getFine() { return fine; }
    public boolean isReturned() { return returned; }

    public boolean isOverdue() {
        return !returned && LocalDate.now().isAfter(dueDate);
    }

    public double getCurrentFine() {
        if (returned) return fine;
        if (!isOverdue()) return 0;

        long lateDays = ChronoUnit.DAYS.between(dueDate, LocalDate.now());
        return lateDays * FINE_PER_DAY;
    }

    public void completeReturn() {
        if (!returned) {
            returnDate = LocalDate.now();
            long lateDays = Math.max(
                    0, ChronoUnit.DAYS.between(dueDate, returnDate)
            );
            fine = lateDays * FINE_PER_DAY;
            returned = true;
        }
    }
}
