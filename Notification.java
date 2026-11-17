import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Notification implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String id;
    private final String audience;
    private final String memberId;
    private final String category;
    private final String message;
    private final String deduplicationKey;
    private final LocalDateTime createdAt;
    private boolean read;

    public Notification(String id, String audience, String memberId,
                        String category, String message, String deduplicationKey) {
        this.id = id;
        this.audience = audience;
        this.memberId = memberId;
        this.category = category;
        this.message = message;
        this.deduplicationKey = deduplicationKey;
        this.createdAt = LocalDateTime.now();
        this.read = false;
    }
    public String getId() { return id; }
    public String getAudience() { return audience; }
    public String getMemberId() { return memberId; }
    public String getCategory() { return category; }
    public String getMessage() { return message; }
    public String getDeduplicationKey() { return deduplicationKey; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getFormattedDate() { return createdAt.format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")); }
    public boolean isRead() { return read; }
    public void markRead() { read = true; }
}
