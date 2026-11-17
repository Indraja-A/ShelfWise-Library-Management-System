import java.io.Serializable;

public class Member implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String memberId;
    private final String name;
    private final String phoneNumber;
    private final String memberType;
    private final String password;

    private final int borrowingLimit = 5;
    private int borrowedCount = 0;

    public Member(String memberId, String name, String phoneNumber,
                  String memberType, String password) {
        this.memberId = memberId;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.memberType = memberType;
        this.password = password;
    }

    public String getMemberId() { return memberId; }
    public String getName() { return name; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getMemberType() { return memberType; }

    public boolean checkPassword(String enteredPassword) {
        return password.equals(enteredPassword);
    }

    public int getBorrowingLimit() { return borrowingLimit; }
    public int getBorrowedCount() { return borrowedCount; }
    public int getRemainingLimit() { return borrowingLimit - borrowedCount; }
    public boolean canBorrow() { return borrowedCount < borrowingLimit; }

    public void increaseBorrowedCount() {
        if (canBorrow()) borrowedCount++;
    }

    public void decreaseBorrowedCount() {
        if (borrowedCount > 0) borrowedCount--;
    }
}
