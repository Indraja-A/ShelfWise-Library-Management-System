import java.util.ArrayList;
import java.util.Scanner;

/**
 * ShelfWise - Console-based Library Management System.
 * This version uses standard Java console input/output only.
 */
public class Main {
    private static final Scanner sc = new Scanner(System.in);
    private static final Library library = Library.load();
    private static final Admin admin = new Admin();

    public static void main(String[] args) {
        System.out.println("==============================================");
        System.out.println("                 SHELFWISE");
        System.out.println("       Library Management System");
        System.out.println("==============================================");
        System.out.println("Data file: " + Library.getDataFileLocation());

        while (true) {
            System.out.println("\n--------------- MAIN MENU ----------------");
            System.out.println("1. Admin Login");
            System.out.println("2. Member Login");
            System.out.println("3. Register as Member");
            System.out.println("0. Exit");
            int choice = readInt("Enter choice: ");

            switch (choice) {
                case 1: adminLogin(); break;
                case 2: memberLogin(); break;
                case 3: registerMember(); break;
                case 0:
                    library.save();
                    System.out.println("Thank you for using ShelfWise. Goodbye!");
                    return;
                default: System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void adminLogin() {
        System.out.println("\n------------ ADMIN LOGIN ------------");
        String username = readLine("Username: ");
        String password = readLine("Password: ");
        if (admin.login(username, password)) {
            System.out.println("\nLogin successful. Welcome, Admin!");
            adminMenu();
        } else {
            System.out.println("Invalid username or password.");
        }
    }

    private static void adminMenu() {
        while (true) {
            System.out.println("\n=============== ADMIN MENU ===============");
            System.out.println("1. Dashboard summary");
            System.out.println("2. Add a book");
            System.out.println("3. View / search books");
            System.out.println("4. View / search members");
            System.out.println("5. Register a member");
            System.out.println("6. Issue a book");
            System.out.println("7. Return a book");
            System.out.println("8. View all transactions");
            System.out.println("9. View overdue report");
            System.out.println("10. View notifications");
            System.out.println("0. Logout");
            int choice = readInt("Enter choice: ");

            switch (choice) {
                case 1: showDashboard(); break;
                case 2: addBook(); break;
                case 3: viewBooks(); break;
                case 4: searchMembers(); break;
                case 5: registerMember(); break;
                case 6: issueBook(); break;
                case 7: returnBook(); break;
                case 8: showTransactions(library.getTransactions()); break;
                case 9: showOverdue(); break;
                case 10: showNotifications("ADMIN", null); break;
                case 0: System.out.println("Logged out successfully."); return;
                default: System.out.println("Invalid choice.");
            }
        }
    }

    private static void memberLogin() {
        System.out.println("\n------------ MEMBER LOGIN ------------");
        String id = readLine("Member ID: ");
        String password = readLine("Password: ");
        Member member = library.loginMember(id, password);
        if (member == null) {
            System.out.println("Invalid Member ID or password.");
            return;
        }
        System.out.println("\nWelcome, " + member.getName() + "!");
        library.generateMemberDueNotifications(member.getMemberId());
        memberMenu(member);
    }

    private static void memberMenu(Member member) {
        while (true) {
            System.out.println("\n=============== MEMBER MENU ===============");
            System.out.println("Member: " + member.getName() + " (" + member.getMemberId() + ")");
            System.out.println("Books borrowed: " + member.getBorrowedCount()
                    + " | Remaining limit: " + member.getRemainingLimit());
            System.out.println("1. View book catalogue");
            System.out.println("2. Search books");
            System.out.println("3. View my borrowed books");
            System.out.println("4. View my transaction history");
            System.out.println("5. Return a book");
            System.out.println("6. View my notifications");
            System.out.println("7. View outstanding fine");
            System.out.println("0. Logout");
            int choice = readInt("Enter choice: ");

            switch (choice) {
                case 1: printBooks(library.getBooks()); break;
                case 2:
                    String keyword = readLine("Enter title or author: ");
                    printBooks(library.searchBooks(keyword));
                    break;
                case 3: showTransactions(library.getActiveMemberTransactions(member.getMemberId())); break;
                case 4: showTransactions(library.getMemberTransactions(member.getMemberId())); break;
                case 5:
                    String bookId = readLine("Enter Book ID to return: ");
                    System.out.println(library.returnBook(member.getMemberId(), bookId));
                    break;
                case 6: showNotifications("MEMBER", member.getMemberId()); break;
                case 7:
                    System.out.printf("Outstanding fine: Rs. %.2f%n",
                            library.getMemberOutstandingFine(member.getMemberId()));
                    break;
                case 0: System.out.println("Logged out successfully."); return;
                default: System.out.println("Invalid choice.");
            }
        }
    }

    private static void registerMember() {
        System.out.println("\n------------ MEMBER REGISTRATION ------------");
        String name = readLine("Full name: ");
        String phone = readLine("Phone number: ");
        String type = readLine("Member type (Student/Faculty/Other): ");
        String password = readLine("Create password: ");

        if (name.trim().isEmpty() || phone.trim().isEmpty()
                || type.trim().isEmpty() || password.isEmpty()) {
            System.out.println("All fields are required.");
            return;
        }
        Member member = library.registerMember(name, phone, type, password);
        if (member == null) {
            System.out.println("A member with this phone number already exists.");
        } else {
            System.out.println("\nRegistration successful!");
            System.out.println("Member ID: " + member.getMemberId());
            System.out.println("Name: " + member.getName());
            System.out.println("Please remember your Member ID and password.");
        }
    }

    private static void addBook() {
        System.out.println("\n------------ ADD BOOK ------------");
        String title = readLine("Book title: ");
        String author = readLine("Author: ");
        int copies = readInt("Number of copies: ");
        Book book = library.addBook(title, author, copies);
        if (book == null) {
            System.out.println("Invalid details. Title, author and copies are required.");
        } else {
            System.out.println("Book added successfully. Book ID: " + book.getBookId());
        }
    }

    private static void viewBooks() {
        String keyword = readLine("Search by title/author (press Enter to view all): ");
        if (keyword.trim().isEmpty()) printBooks(library.getBooks());
        else printBooks(library.searchBooks(keyword));
    }

    private static void searchMembers() {
        String keyword = readLine("Search by name, Member ID or phone (Enter for all): ");
        printMembers(library.searchMembers(keyword));
    }

    private static void issueBook() {
        System.out.println("\n------------ ISSUE BOOK ------------");
        String memberId = readLine("Member ID: ");
        String bookId = readLine("Book ID: ");
        System.out.println(library.issueBook(memberId, bookId));
    }

    private static void returnBook() {
        System.out.println("\n------------ RETURN BOOK ------------");
        String memberId = readLine("Member ID: ");
        String bookId = readLine("Book ID: ");
        System.out.println(library.returnBook(memberId, bookId));
    }

    private static void showDashboard() {
        System.out.println("\n------------ LIBRARY DASHBOARD ------------");
        System.out.println("Registered members : " + library.getMembers().size());
        System.out.println("Book titles        : " + library.getBooks().size());
        System.out.println("Total book copies  : " + library.getTotalBookCopies());
        System.out.println("Available copies   : " + library.getAvailableBookCopies());
        System.out.println("Issued copies      : " + library.getIssuedBookCopies());
        System.out.println("Overdue books      : " + library.getOverdueTransactions().size());
        System.out.println("Unread notifications: "
                + library.getUnreadNotificationCount("ADMIN", null));
    }

    private static void showOverdue() {
        ArrayList<Transaction> overdue = library.getOverdueTransactions();
        System.out.println("\n------------ OVERDUE REPORT ------------");
        showTransactions(overdue);
        if (!overdue.isEmpty()) {
            System.out.println("\nCurrent overdue fines:");
            for (Transaction t : overdue) {
                Member m = library.findMember(t.getMemberId());
                Book b = library.findBook(t.getBookId());
                System.out.printf("%s | %s | %s | Due: %s | Fine: Rs. %.2f%n",
                        t.getTransactionId(),
                        m == null ? t.getMemberId() : m.getName(),
                        b == null ? t.getBookId() : b.getTitle(),
                        t.getDueDate(), t.getCurrentFine());
            }
        }
    }

    private static void showTransactions(ArrayList<Transaction> transactions) {
        System.out.println("\n---------------- TRANSACTIONS ----------------");
        if (transactions.isEmpty()) {
            System.out.println("No transaction records found.");
            return;
        }
        for (Transaction t : transactions) {
            Member m = library.findMember(t.getMemberId());
            Book b = library.findBook(t.getBookId());
            System.out.println("-----------------------------------------------");
            System.out.println("Transaction ID: " + t.getTransactionId());
            System.out.println("Member         : " + (m == null ? t.getMemberId() : m.getName())
                    + " (" + t.getMemberId() + ")");
            System.out.println("Book           : " + (b == null ? t.getBookId() : b.getTitle())
                    + " (" + t.getBookId() + ")");
            System.out.println("Issue date     : " + t.getIssueDate());
            System.out.println("Due date       : " + t.getDueDate());
            System.out.println("Status         : " + (t.isReturned() ? "Returned" : "Active"));
            if (t.isReturned()) {
                System.out.println("Return date    : " + t.getReturnDate());
                System.out.printf("Fine paid      : Rs. %.2f%n", t.getFine());
            } else if (t.isOverdue()) {
                System.out.printf("Current fine   : Rs. %.2f%n", t.getCurrentFine());
            }
        }
    }

    private static void printBooks(ArrayList<Book> books) {
        System.out.println("\n---------------- BOOK CATALOGUE ----------------");
        if (books.isEmpty()) {
            System.out.println("No books found.");
            return;
        }
        System.out.printf("%-9s %-30s %-24s %-10s %-10s%n",
                "Book ID", "Title", "Author", "Total", "Available");
        for (Book b : books) {
            System.out.printf("%-9s %-30s %-24s %-10d %-10d%n",
                    b.getBookId(), shorten(b.getTitle(), 28),
                    shorten(b.getAuthor(), 22), b.getTotalCopies(),
                    b.getAvailableCopies());
        }
    }

    private static void printMembers(ArrayList<Member> members) {
        System.out.println("\n---------------- MEMBERS ----------------");
        if (members.isEmpty()) {
            System.out.println("No members found.");
            return;
        }
        System.out.printf("%-9s %-24s %-16s %-14s %-10s%n",
                "Member ID", "Name", "Phone", "Type", "Borrowed");
        for (Member m : members) {
            System.out.printf("%-9s %-24s %-16s %-14s %-10d%n",
                    m.getMemberId(), shorten(m.getName(), 22),
                    m.getPhoneNumber(), m.getMemberType(), m.getBorrowedCount());
        }
    }

    private static void showNotifications(String audience, String memberId) {
        ArrayList<Notification> notifications = library.getNotifications(audience, memberId);
        System.out.println("\n---------------- NOTIFICATIONS ----------------");
        if (notifications.isEmpty()) {
            System.out.println("No notifications.");
            return;
        }
        for (Notification n : notifications) {
            System.out.println("-----------------------------------------------");
            System.out.println(n.getId() + " | " + n.getCategory()
                    + " | " + (n.isRead() ? "Read" : "Unread"));
            System.out.println(n.getFormattedDate());
            System.out.println(n.getMessage());
        }
        String id = readLine("\nEnter notification ID to mark as read (or press Enter to go back): ");
        if (!id.trim().isEmpty()) {
            library.markNotificationRead(id.trim());
            System.out.println("Notification marked as read (if the ID was valid).");
        }
    }

    private static String shorten(String value, int max) {
        if (value == null || value.length() <= max) return value;
        return value.substring(0, max - 3) + "...";
    }

    private static String readLine(String prompt) {
        System.out.print(prompt);
        return sc.nextLine();
    }

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = sc.nextLine().trim();
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException ex) {
                System.out.println("Please enter a valid whole number.");
            }
        }
    }
}
