S# Customer Billing System (PS-49)

A console-based billing counter application written in **Java**. Customers are added to a waiting queue, bills are processed in First-In-First-Out (FIFO) order with 5% GST, and all records are saved to a text file.

| | |
|---|---|
| **Student** | Anjali Chouhan |
| **ERP ID** | RU-26-02332 |
| **Course** | Data Structure and Algorithms (DSA) – RU-200-01-00029 |
| **Programme** | B.Tech CSE with AI & ML in association with Google |
| **Guide** | Mr. Surya Mani Verma, Technical Trainer |
| **University** | School of Computer Science and Engineering, Rungta International Skills University, Bhilai, Chhattisgarh |
| **Session** | 2026-27 |

## Features

The program shows a menu with exactly five options:

1. **Add Customer to Queue** – enters the customer name and amount. The amount must be greater than 0. A unique Bill ID (BL101, BL102, ...) is generated.
2. **Process Next Bill** – serves the customer at the front of the queue, adds 5% GST and stores the bill.
3. **View Waiting Queue** – shows all waiting customers in FIFO order.
4. **Search Bill by ID** – finds a processed bill using its Bill ID.
5. **Exit** – saves all data to `bills.txt` and closes the program.

## DSA Concepts Used

| Concept | Where it is used |
|---|---|
| Queue (custom Linked List) | Waiting customers, FIFO order (`CustomerQueue`, `Node`) |
| Array | Stores processed bill records |
| Linear Search (manual) | Search bill by ID |
| Bubble Sort (manual) | Sorts processed bill records by total amount before saving |
| Methods | Each menu operation is a separate method |
| File Handling | Loads and saves records using `bills.txt` |

No built-in sorting or searching methods are used.

## Project Structure

```
CustomerBillingSystem.java   -> complete source code (single file)
README.md                    -> this file
bills.txt                    -> created automatically when the program exits
```

Classes inside the file: `Customer`, `Node`, `CustomerQueue`, `Bill`, `CustomerBillingSystem`.

## How to Run

**Requirement:** JDK 21 (or any recent JDK) installed. Check with `java -version`.

1. Download or clone this repository and open the folder in a terminal (or VS Code).
2. Compile:
   ```
   javac CustomerBillingSystem.java
   ```
3. Run:
   ```
   java CustomerBillingSystem
   ```

## Sample Run

```
===== CUSTOMER BILLING SYSTEM =====
1. Add Customer to Queue
2. Process Next Bill
3. View Waiting Queue
4. Search Bill by ID
5. Exit
Enter your choice: 1
Enter customer name: Sanjay
Enter amount: 1000
Customer added. ID: BL101 | Sanjay | Amount: 1000 | Position: 1

Enter your choice: 2
Bill generated. BL101 | Sanjay | Amount: 1000 | GST 5%: 50 | Total: 1050
Queue is now empty.

Enter your choice: 4
Enter Bill ID: BL101
Found: BL101 | Sanjay | Total: 1050 | Status: Paid
```

Invalid amount:

```
Enter amount: -200
Invalid amount. Amount must be greater than 0.
```

## Data File Format (`bills.txt`)

One record per line, fields separated by `|`:

```
WAITING|BL102|Rekha|500.0
BILL|BL101|Sanjay|1000.0|50.0|1050.0|Paid
```

- `WAITING` lines are customers still in the queue.
- `BILL` lines are processed bills.
- On the next start the file is loaded, and new Bill IDs continue after the highest ID already used.

## Test Cases (PS-49)

| # | Scenario | Result |
|---|---|---|
| 1 | Add customer Sanjay, 1000 | Customer added. ID: BL101, Position: 1 |
| 2 | Process the only bill in the queue | Bill generated, GST 50, Total 1050, queue empty |
| 3 | Add Rekha with amount -200 | Invalid amount. Amount must be greater than 0. |
| 4 | Add Rekha 500, process next bill, search BL101 | BL102 added, BL101 billed, BL101 found (Total 1050, Paid) |
