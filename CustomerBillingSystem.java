import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

// ---------------------------------------------------------------
// Customer Billing System (PS-49)
// Concepts: Queue (Linked List), Array, Linear Search,
//           Bubble Sort, Methods, File Handling
// ---------------------------------------------------------------

// Stores the details of one waiting customer.
class Customer {
    String billId;
    String customerName;
    double amount;

    Customer(String billId, String customerName, double amount) {
        this.billId = billId;
        this.customerName = customerName;
        this.amount = amount;
    }
}

// One box of the Linked List. It holds a customer and points to the next box.
class Node {
    Customer customer;
    Node next;

    Node(Customer customer) {
        this.customer = customer;
        this.next = null;
    }
}

// Queue (First-In-First-Out) built with our own Linked List.
class CustomerQueue {
    Node front; // customer who will be served first
    Node rear;  // customer who joined last
    int size;

    CustomerQueue() {
        front = null;
        rear = null;
        size = 0;
    }

    boolean isEmpty() {
        return front == null;
    }

    // Add a customer at the REAR (end) of the queue.
    void enqueue(Customer customer) {
        Node newNode = new Node(customer);
        if (isEmpty()) {
            front = newNode;
            rear = newNode;
        } else {
            rear.next = newNode;
            rear = newNode;
        }
        size++;
    }

    // Remove and return the customer at the FRONT of the queue.
    Customer dequeue() {
        if (isEmpty()) {
            return null;
        }
        Customer served = front.customer;
        front = front.next;
        if (front == null) {
            rear = null; // queue became empty
        }
        size--;
        return served;
    }

    // Show all waiting customers from front to rear.
    void displayQueue() {
        if (isEmpty()) {
            System.out.println("Queue is empty.");
            return;
        }
        System.out.println("Waiting Queue (" + size + " customer(s)):");
        Node current = front;
        int position = 1;
        while (current != null) {
            Customer c = current.customer;
            System.out.println(position + ". " + c.billId + " | " + c.customerName
                    + " | Amount: " + CustomerBillingSystem.format(c.amount));
            current = current.next;
            position++;
        }
    }
}

// Stores one generated (processed) bill.
class Bill {
    String billId;
    String customerName;
    double amount;
    double gst;
    double totalAmount;
    String status;

    Bill(String billId, String customerName, double amount,
         double gst, double totalAmount, String status) {
        this.billId = billId;
        this.customerName = customerName;
        this.amount = amount;
        this.gst = gst;
        this.totalAmount = totalAmount;
        this.status = status;
    }
}

// Main class: menu, billing operations and file handling.
public class CustomerBillingSystem {

    static final String FILE_NAME = "bills.txt";
    static final double GST_RATE = 0.05; // 5% GST

    static CustomerQueue queue = new CustomerQueue();
    static Bill[] billRecords = new Bill[10]; // array of processed bills
    static int billCount = 0;                 // how many bills are stored
    static int nextBillNumber = 101;          // next ID will be BL101, BL102...

    static Scanner input = new Scanner(System.in);

    // ----------------------------- MAIN -----------------------------
    public static void main(String[] args) {
        loadData();

        boolean running = true;
        while (running) {
            showMenu();
            String line = readLine();
            if (line == null) { // input ended (e.g. file input) -> exit safely
                System.out.println();
                running = false;
                break;
            }

            int choice;
            try {
                choice = Integer.parseInt(line.trim());
            } catch (NumberFormatException e) {
                choice = -1;
            }

            if (choice == 1) {
                addCustomer();
            } else if (choice == 2) {
                processNextBill();
            } else if (choice == 3) {
                viewWaitingQueue();
            } else if (choice == 4) {
                searchBill();
            } else if (choice == 5) {
                running = false;
            } else {
                System.out.println("Invalid choice. Please enter a number from 1 to 5.");
            }
        }

        saveData();
        System.out.println("Data saved to " + FILE_NAME + ". Goodbye!");
    }

    // ----------------------------- MENU -----------------------------
    static void showMenu() {
        System.out.println();
        System.out.println("===== CUSTOMER BILLING SYSTEM =====");
        System.out.println("1. Add Customer to Queue");
        System.out.println("2. Process Next Bill");
        System.out.println("3. View Waiting Queue");
        System.out.println("4. Search Bill by ID");
        System.out.println("5. Exit");
        System.out.print("Enter your choice: ");
    }

    // Reads one line from the keyboard. Returns null if there is no more input.
    static String readLine() {
        if (input.hasNextLine()) {
            return input.nextLine();
        }
        return null;
    }

    // Prints 1000.0 as "1000" and 52.5 as "52.50".
    static String format(double value) {
        if (value == Math.floor(value)) {
            return String.valueOf((long) value);
        }
        return String.format("%.2f", value);
    }

    // Makes the next Bill ID like BL101 and moves the counter forward.
    static String generateBillId() {
        String id = "BL" + nextBillNumber;
        nextBillNumber++;
        return id;
    }

    // ------------------------ 1. ADD CUSTOMER -----------------------
    static void addCustomer() {
        System.out.print("Enter customer name: ");
        String name = readLine();
        if (name == null) {
            return;
        }
        name = name.trim().replace("|", " "); // '|' is used as separator in the file
        if (name.isEmpty()) {
            System.out.println("Invalid name. Name cannot be empty.");
            return;
        }

        System.out.print("Enter amount: ");
        String amountText = readLine();
        if (amountText == null) {
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText.trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount. Amount must be greater than 0.");
            return;
        }

        if (Double.isNaN(amount) || Double.isInfinite(amount) || amount <= 0) {
            System.out.println("Invalid amount. Amount must be greater than 0.");
            return;
        }

        // Bill ID is created only after the input is valid.
        String billId = generateBillId();
        Customer customer = new Customer(billId, name, amount);
        queue.enqueue(customer);

        System.out.println("Customer added. ID: " + billId + " | " + name
                + " | Amount: " + format(amount) + " | Position: " + queue.size);
    }

    // ---------------------- 2. PROCESS NEXT BILL --------------------
    static void processNextBill() {
        if (queue.isEmpty()) {
            System.out.println("Queue is empty. No bill to process.");
            return;
        }

        Customer c = queue.dequeue(); // FIFO: first customer in is served first
        double gst = calculateGST(c.amount);
        double total = Math.round((c.amount + gst) * 100.0) / 100.0;

        Bill bill = new Bill(c.billId, c.customerName, c.amount, gst, total, "Paid");
        addBillToArray(bill);

        System.out.println("Bill generated. " + bill.billId + " | " + bill.customerName
                + " | Amount: " + format(bill.amount)
                + " | GST 5%: " + format(bill.gst)
                + " | Total: " + format(bill.totalAmount));
        if (queue.isEmpty()) {
            System.out.println("Queue is now empty.");
        }
    }

    // 5% GST rounded to 2 decimal places.
    static double calculateGST(double amount) {
        return Math.round(amount * GST_RATE * 100.0) / 100.0;
    }

    // Stores a bill in the array. If the array is full, make it bigger by hand.
    static void addBillToArray(Bill bill) {
        if (billCount == billRecords.length) {
            Bill[] bigger = new Bill[billRecords.length * 2];
            for (int i = 0; i < billCount; i++) {
                bigger[i] = billRecords[i];
            }
            billRecords = bigger;
        }
        billRecords[billCount] = bill;
        billCount++;
    }

    // ---------------------- 3. VIEW WAITING QUEUE -------------------
    static void viewWaitingQueue() {
        queue.displayQueue();
    }

    // ----------------------- 4. SEARCH BILL BY ID -------------------
    static void searchBill() {
        System.out.print("Enter Bill ID: ");
        String id = readLine();
        if (id == null) {
            return;
        }
        id = id.trim();

        int index = linearSearch(id);
        if (index == -1) {
            System.out.println("Bill not found.");
        } else {
            Bill b = billRecords[index];
            System.out.println("Found: " + b.billId + " | " + b.customerName
                    + " | Total: " + format(b.totalAmount) + " | Status: " + b.status);
        }
    }

    // Manual Linear Search: check every bill one by one from the start.
    static int linearSearch(String id) {
        for (int i = 0; i < billCount; i++) {
            if (billRecords[i].billId.equalsIgnoreCase(id)) {
                return i;
            }
        }
        return -1; // not found
    }

    // ------------------------- BUBBLE SORT --------------------------
    // Manual Bubble Sort on the bill records array (smallest total first).
    // Used when saving, so processed bills in bills.txt are stored in order of amount.
    static void bubbleSortBillsByAmount() {
        for (int pass = 0; pass < billCount - 1; pass++) {
            for (int j = 0; j < billCount - 1 - pass; j++) {
                if (billRecords[j].totalAmount > billRecords[j + 1].totalAmount) {
                    Bill temp = billRecords[j];
                    billRecords[j] = billRecords[j + 1];
                    billRecords[j + 1] = temp;
                }
            }
        }
    }

    // ------------------------- FILE HANDLING ------------------------
    // File format (one record per line, parts separated by '|'):
    //   WAITING|BL101|Sanjay|1000.0
    //   BILL|BL101|Sanjay|1000.0|50.0|1050.0|Paid
    static void loadData() {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            return; // first run: nothing to load
        }

        int highestNumber = 100; // so the next ID is at least BL101

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                String[] parts = line.split("\\|");
                try {
                    if (parts[0].equals("WAITING") && parts.length == 4) {
                        Customer c = new Customer(parts[1], parts[2], Double.parseDouble(parts[3]));
                        queue.enqueue(c);
                        highestNumber = Math.max(highestNumber, idNumber(parts[1]));
                    } else if (parts[0].equals("BILL") && parts.length == 7) {
                        Bill b = new Bill(parts[1], parts[2], Double.parseDouble(parts[3]),
                                Double.parseDouble(parts[4]), Double.parseDouble(parts[5]), parts[6]);
                        addBillToArray(b);
                        highestNumber = Math.max(highestNumber, idNumber(parts[1]));
                    }
                } catch (NumberFormatException e) {
                    // skip a damaged line and continue
                }
            }
        } catch (IOException e) {
            System.out.println("Could not read " + FILE_NAME + ": " + e.getMessage());
        }

        nextBillNumber = highestNumber + 1; // keeps Bill IDs correct after restart

        if (queue.size > 0 || billCount > 0) {
            System.out.println("Loaded " + queue.size + " waiting customer(s) and "
                    + billCount + " bill(s) from " + FILE_NAME + ".");
        }
    }

    // Turns "BL105" into the number 105 (returns 0 if the ID is not valid).
    static int idNumber(String billId) {
        try {
            return Integer.parseInt(billId.substring(2));
        } catch (Exception e) {
            return 0;
        }
    }

    static void saveData() {
        bubbleSortBillsByAmount(); // sort processed bills before saving

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            // 1) waiting customers (front to rear, so FIFO order is kept)
            Node current = queue.front;
            while (current != null) {
                Customer c = current.customer;
                writer.write("WAITING|" + c.billId + "|" + c.customerName + "|" + c.amount);
                writer.newLine();
                current = current.next;
            }
            // 2) processed bills
            for (int i = 0; i < billCount; i++) {
                Bill b = billRecords[i];
                writer.write("BILL|" + b.billId + "|" + b.customerName + "|" + b.amount
                        + "|" + b.gst + "|" + b.totalAmount + "|" + b.status);
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Could not save " + FILE_NAME + ": " + e.getMessage());
        }
    }
}