import java.util.ArrayList;
import java.io.Serializable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;

public class Library implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Path DATA_FILE = Paths.get(System.getProperty("user.home"), ".shelfwise", "library-data.ser");

    private final ArrayList<Member> members = new ArrayList<>();
    private final ArrayList<Book> books = new ArrayList<>();
    private final ArrayList<Transaction> transactions = new ArrayList<>();
    // Non-final for backward-compatible loading of older saved Library files.
    private ArrayList<Notification> notifications = new ArrayList<>();

    private int memberCounter = 0;
    private int bookCounter = 0;
    private int transactionCounter = 0;


    public static Library load() {
        try {
            if (Files.exists(DATA_FILE)) {
                try (ObjectInputStream input = new ObjectInputStream(
                        Files.newInputStream(DATA_FILE))) {
                    Object saved = input.readObject();
                    if (saved instanceof Library) {
                        Library loaded = (Library) saved;
                        if (loaded.notifications == null) loaded.notifications = new ArrayList<>();
                        return loaded;
                    }
                }
            }
        } catch (Exception ex) {
            System.err.println("Could not load ShelfWise data: " + ex.getMessage());
        }
        return new Library();
    }

    public void save() {
        try {
            Files.createDirectories(DATA_FILE.getParent());
            try (ObjectOutputStream output = new ObjectOutputStream(
                    Files.newOutputStream(DATA_FILE))) {
                output.writeObject(this);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Could not save library data: " + ex.getMessage(), ex);
        }
    }

    public static String getDataFileLocation() {
        return DATA_FILE.toAbsolutePath().toString();
    }

    public Member registerMember(String name, String phone,
                                 String type, String password) {
        for (Member member : members) {
            if (member.getPhoneNumber().equals(phone)) return null;
        }

        memberCounter++;
        String id = String.format("M%03d", memberCounter);
        Member member = new Member(id, name, phone, type, password);
        members.add(member);
        save();
        return member;
    }

    public Member loginMember(String id, String password) {
        Member member = findMember(id);
        if (member != null && member.checkPassword(password)) return member;
        return null;
    }

    public Member findMember(String id) {
        if (id == null) return null;
        for (Member member : members) {
            if (member.getMemberId().equalsIgnoreCase(id.trim())) return member;
        }
        return null;
    }

    public Book addBook(String title, String author, int copies) {
        if (title == null || author == null || title.trim().isEmpty()
                || author.trim().isEmpty() || copies <= 0) return null;

        bookCounter++;
        String id = String.format("B%03d", bookCounter);
        Book book = new Book(id, title.trim(), author.trim(), copies);
        books.add(book);
        addNotification("ADMIN", null, "NEW BOOK",
                "New book added to the library: “" + book.getTitle()
                + "” by " + book.getAuthor() + " (" + book.getTotalCopies() + " copies).",
                "BOOK_ADDED_" + book.getBookId());
        save();
        return book;
    }

    public ArrayList<Member> searchMembers(String keyword) {
        ArrayList<Member> result = new ArrayList<>();
        if (keyword == null) return result;
        String search = keyword.trim().toLowerCase();
        if (search.isEmpty()) {
            result.addAll(members);
            return result;
        }
        for (Member member : members) {
            if (member.getName().toLowerCase().contains(search)
                    || member.getMemberId().toLowerCase().contains(search)
                    || member.getPhoneNumber().contains(search)) {
                result.add(member);
            }
        }
        return result;
    }

    public Book findBook(String id) {
        if (id == null) return null;
        for (Book book : books) {
            if (book.getBookId().equalsIgnoreCase(id.trim())) return book;
        }
        return null;
    }

    public String issueBook(String memberId, String bookId) {
        Member member = findMember(memberId);
        Book book = findBook(bookId);

        if (member == null) return "Member ID was not found.";
        if (book == null) return "Book ID was not found.";
        if (!member.canBorrow()) return "Borrowing limit reached. Maximum is 5 books.";
        if (!book.isAvailable()) return "This book is currently unavailable.";

        book.issueCopy();
        member.increaseBorrowedCount();
        transactionCounter++;

        String transactionId = String.format("T%03d", transactionCounter);
        Transaction transaction = new Transaction(
                transactionId, memberId, bookId, LocalDate.now()
        );
        transactions.add(transaction);
        Member issuedTo = findMember(memberId);
        String memberName = issuedTo == null ? memberId : issuedTo.getName();
        addNotification("ADMIN", null, "BOOK ISSUED",
                "“" + book.getTitle() + "” issued to " + memberName
                + ". Due date: " + transaction.getDueDate() + ".",
                "ISSUED_ADMIN_" + transactionId);
        addNotification("MEMBER", memberId, "BOOK ISSUED",
                "“" + book.getTitle() + "” has been issued to you. Please return it by "
                + transaction.getDueDate() + ".",
                "ISSUED_MEMBER_" + transactionId);
        save();

        return "Book issued successfully!\n\n"
                + "Book: " + book.getTitle()
                + "\nTransaction ID: " + transactionId
                + "\nIssue date: " + transaction.getIssueDate()
                + "\nDue date: " + transaction.getDueDate();
    }

    public String returnBook(String memberId, String bookId) {
        Member member = findMember(memberId);
        Book book = findBook(bookId);
        if (member == null || book == null) return "Invalid Member ID or Book ID.";

        for (Transaction transaction : transactions) {
            if (transaction.getMemberId().equalsIgnoreCase(memberId)
                    && transaction.getBookId().equalsIgnoreCase(bookId)
                    && !transaction.isReturned()) {
                transaction.completeReturn();
                book.returnCopy();
                member.decreaseBorrowedCount();
                addNotification("ADMIN", null, "BOOK RETURNED",
                        "“" + book.getTitle() + "” returned by " + member.getName()
                        + ". Fine: Rs. " + String.format("%.2f", transaction.getFine()) + ".",
                        "RETURN_ADMIN_" + transaction.getTransactionId());
                addNotification("MEMBER", memberId, "BOOK RETURNED",
                        "Your return of “" + book.getTitle() + "” was recorded. Fine: Rs. "
                        + String.format("%.2f", transaction.getFine()) + ".",
                        "RETURN_MEMBER_" + transaction.getTransactionId());
                save();

                return "Book returned successfully!\n\n"
                        + "Book: " + book.getTitle()
                        + "\nReturn date: " + transaction.getReturnDate()
                        + "\nFine: Rs. "
                        + String.format("%.2f", transaction.getFine());
            }
        }
        return "No active borrowing record found for this member and book.";
    }


    public void addNotification(String audience, String memberId, String category,
                                String message, String deduplicationKey) {
        if (notifications == null) notifications = new ArrayList<>();
        for (Notification existing : notifications) {
            if (existing.getDeduplicationKey().equals(deduplicationKey)) return;
        }
        String id = "N" + String.format("%05d", notifications.size() + 1);
        notifications.add(new Notification(id, audience, memberId, category,
                message, deduplicationKey));
    }

    public ArrayList<Notification> getNotifications(String audience, String memberId) {
        ArrayList<Notification> result = new ArrayList<>();
        if (notifications == null) return result;
        for (int i = notifications.size() - 1; i >= 0; i--) {
            Notification n = notifications.get(i);
            if (audience.equalsIgnoreCase(n.getAudience())
                    && ("ADMIN".equalsIgnoreCase(audience)
                    || (memberId != null && memberId.equalsIgnoreCase(n.getMemberId())))) {
                result.add(n);
            }
        }
        return result;
    }

    public int getUnreadNotificationCount(String audience, String memberId) {
        int count = 0;
        for (Notification n : getNotifications(audience, memberId)) {
            if (!n.isRead()) count++;
        }
        return count;
    }

    public void markNotificationRead(String notificationId) {
        if (notifications == null) return;
        for (Notification n : notifications) {
            if (n.getId().equals(notificationId)) {
                n.markRead();
                save();
                return;
            }
        }
    }

    public void generateMemberDueNotifications(String memberId) {
        LocalDate today = LocalDate.now();
        for (Transaction transaction : getActiveMemberTransactions(memberId)) {
            Book book = findBook(transaction.getBookId());
            if (book == null) continue;
            long days = java.time.temporal.ChronoUnit.DAYS.between(today, transaction.getDueDate());
            if (days == 1) {
                addNotification("MEMBER", memberId, "DUE TOMORROW",
                        "Reminder: “" + book.getTitle() + "” is due tomorrow ("
                        + transaction.getDueDate() + ").",
                        "DUE_TOMORROW_" + transaction.getTransactionId() + "_" + today);
            } else if (days == 0) {
                addNotification("MEMBER", memberId, "DUE TODAY",
                        "“" + book.getTitle() + "” is due today. Please return it today.",
                        "DUE_TODAY_" + transaction.getTransactionId() + "_" + today);
            } else if (days < 0) {
                addNotification("MEMBER", memberId, "OVERDUE",
                        "“" + book.getTitle() + "” is overdue by " + Math.abs(days)
                        + " day(s). Current fine: Rs. "
                        + String.format("%.2f", transaction.getCurrentFine()) + ".",
                        "OVERDUE_" + transaction.getTransactionId() + "_" + today);
            }
        }
        save();
    }

    public ArrayList<Member> getMembers() { return members; }
    public ArrayList<Book> getBooks() { return books; }
    public ArrayList<Transaction> getTransactions() { return transactions; }

    public ArrayList<Transaction> getMemberTransactions(String memberId) {
        ArrayList<Transaction> result = new ArrayList<>();
        for (Transaction transaction : transactions) {
            if (transaction.getMemberId().equalsIgnoreCase(memberId)) {
                result.add(transaction);
            }
        }
        return result;
    }

    public ArrayList<Transaction> getActiveMemberTransactions(String memberId) {
        ArrayList<Transaction> result = new ArrayList<>();
        for (Transaction transaction : transactions) {
            if (transaction.getMemberId().equalsIgnoreCase(memberId)
                    && !transaction.isReturned()) result.add(transaction);
        }
        return result;
    }

    public ArrayList<Book> searchBooks(String keyword) {
        ArrayList<Book> result = new ArrayList<>();
        if (keyword == null) return result;
        String search = keyword.trim().toLowerCase();

        for (Book book : books) {
            if (book.getTitle().toLowerCase().contains(search)
                    || book.getAuthor().toLowerCase().contains(search)) {
                result.add(book);
            }
        }
        return result;
    }

    public ArrayList<Transaction> getOverdueTransactions() {
        ArrayList<Transaction> result = new ArrayList<>();
        for (Transaction transaction : transactions) {
            if (transaction.isOverdue()) result.add(transaction);
        }
        return result;
    }

    public double getMemberOutstandingFine(String memberId) {
        double total = 0;
        for (Transaction transaction : getMemberTransactions(memberId)) {
            if (!transaction.isReturned()) total += transaction.getCurrentFine();
        }
        return total;
    }

    public int getTotalBookCopies() {
        int total = 0;
        for (Book book : books) total += book.getTotalCopies();
        return total;
    }

    public int getAvailableBookCopies() {
        int total = 0;
        for (Book book : books) total += book.getAvailableCopies();
        return total;
    }

    public int getIssuedBookCopies() {
        return getTotalBookCopies() - getAvailableBookCopies();
    }
}
