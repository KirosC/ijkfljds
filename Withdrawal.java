import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.Timer;

public class Withdrawal extends Transaction {

  private int amount;
  private String inputValue;
  private CashDispenser cashDispenser;
  private WithdrawalHandler withdrawalHandler = new WithdrawalHandler();
  JButton[] lbtns = {createButton("              "), createButton("              "),
      createButton("              "), createButton("              ")};
  JButton[] rbtns = {createButton("              "), createButton("              "),
      createButton("              "), createButton("              ")};

  public Withdrawal(int userAccountNumber, BankDatabase atmBankDatabase,
      CashDispenser atmCashDispenser,
      JFrame theMainMenu, InputOperations a) {
    super(userAccountNumber, atmBankDatabase, theMainMenu, a);
    cashDispenser = atmCashDispenser;
  }

  protected JButton createButton(String buttonText) {
    JButton btn = new JButton(buttonText);
    btn.setFocusable(false);
    btn.addActionListener(withdrawalHandler);
    return btn;
  }

  public void execute() {
    inputOp.stepCounter = 21;
    inputOp.clearInputCache();
    inputValue = null;
    inputOp.sideButton(lbtns, true);
    inputOp.sideButton(rbtns, false);
    inputOp.setENThandler(withdrawalHandler);
    dispenseAmount();
  }

  void dispenseAmount() {
    inputOp.stepCounter = 22;
    String[] reqOnAmt = {"Amount to withdraw", "", "", "Leave  "};
    inputOp.sideButton(lbtns, true);
    inputOp.sideButton(rbtns, false);
    inputOp.displayScreen(reqOnAmt, true, false, true);
  }

  void confirmMessage() {
    inputOp.stepCounter = 23;
    try {
      amount = Integer.parseInt(inputValue);
      // Compare the withdrawal amount with balance
      if (amount > super.getBankDatabase().getAvailableBalance(getAccountNumber())) {
        String[] transactionCnl = {"", "", "Insufficient Balance.", "Transaction Cancelled."};
        toMainMenu(transactionCnl);
      } else if (!cashDispenser.isSufficientCashAvailable(amount) && amount > 0) {
        String[] transactionCnl = {"", "Insufficient Bill To Dispense.",
            "Please try a lower amount or use another ATM.", "", "Transaction Cancelled."};
        toMainMenu(transactionCnl);
        // Invalid amount check
      } else if (amount % 100 == 0 && amount > 0) {
        String[] details = {" ", " ", "Withdraw $ " + amount, "Sure ?"};
        inputOp.displayOptionScreen(details, "No", "Yes");
      } else {
        String[] transactionCnl = {"", "", "Indispensable amount.", "Transaction Cancelled."};
        toMainMenu(transactionCnl);
      }
    } catch (Exception e) {
      String[] transactionCnl = {"", "", "Invalid Amount Input.", "Transaction Cancelled."};
      toMainMenu(transactionCnl);
    }
  }

  void dispense() {
    // Reset step counter
    inputOp.stepCounter = -1;
    // Set delay for dispensing cash
    Timer timer = new Timer(2000, new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        cashDispenser.dispenseCash(amount);
        String[] transactionCnl = {"", "Withdraw $ " + amount, "Please take your money.",
            "Exiting ..."};
        terminate(transactionCnl);
      }
    });

    // Debit the account balance and eject the card
    getBankDatabase().debit(getAccountNumber(), amount);
    String[] cardReminder = {"", "Transaction Success.", "Please take your card first.", ""};
    theATMFrame.repaint();
    inputOp.displayScreen(cardReminder, false, false, false);
    timer.setRepeats(false);
    timer.start();
  }

  void toMainMenu(String[] msg) {
    Timer t = new Timer(2000, new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        toMainMenu();
      }
    });
    theATMFrame.repaint();
    inputOp.displayScreen(msg, false, false, false);

    t.setRepeats(false);
    t.start();
  }

  void toMainMenu() {
    inputOp.removeENThandler(withdrawalHandler);
    theATMFrame.repaint();
    inputOp.mainMenu(getAccountNumber());
  }

  void terminate(String[] msg) {
    Timer t = new Timer(2000, new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        terminate();
      }
    });

    theATMFrame.repaint();
    inputOp.displayScreen(msg, false, false, false);

    t.setRepeats(false);
    t.start();
  }

  void terminate() {
    inputOp.removeENThandler(withdrawalHandler);
    theATMFrame.repaint();
    ATMCaseStudy.main(null);
  }

  private void withdrawCash() {
    inputValue = inputOp.rText;
    confirmMessage();
  }

  private void withdrawCash(String amount) {
    inputValue = amount;
    confirmMessage();
  }

  public class WithdrawalHandler implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
      Object source = e.getSource();
      if (source instanceof JButton) {
        JButton btn = (JButton) source;
        try {
          switch (btn.getText()) {
            case "ENT":
              if (inputOp.stepCounter == 22) {
                withdrawCash();
              }
            case "              ":
              if (inputOp.stepCounter == 5) { // Confirm EXIT
                if (e.getSource() == lbtns[3]) {
                  Timer timer = new Timer(2000, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                      theATMFrame.dispose();
                      ATMCaseStudy.main(null);
                      theATMFrame.toFront();
                    }
                  });
                  String[] cardReminder = {"", "", "Please take your card.", ""};
                  inputOp.displayScreen(cardReminder, false, false, false);
                  theATMFrame.repaint();
                  timer.setRepeats(false);
                  timer.start();
                } else if (e.getSource() == rbtns[3]) {
                  theATMFrame.repaint();
                  inputOp.mainMenu(getAccountNumber());
                }
              }
              if (inputOp.stepCounter == 22) {
                if (e.getSource() == rbtns[2]) { // Return main menu from Withdrawal
                  String[] transactionCnl = {"", "", "Transaction Cancelled.", ""};
                  toMainMenu(transactionCnl);
                } else if (e.getSource()
                    == lbtns[0]) { // Withdraw provided amount of cash on screen
                  withdrawCash("100");
                } else if (e.getSource() == lbtns[1]) {
                  withdrawCash("500");
                } else if (e.getSource() == rbtns[0]) {
                  withdrawCash("300");
                } else if (e.getSource() == rbtns[1]) {
                  withdrawCash("1000");
                }
              }
              if (inputOp.stepCounter == 23) {
                if (e.getSource() == lbtns[3]) {
                  String[] transactionCnl = {"", "", "Transaction Cancelled.", ""};
                  toMainMenu(transactionCnl);
                } else if (e.getSource() == rbtns[3]) {
                  dispense();
                }
              }
              break;
          }
        } catch (Exception exp) {
          exp.printStackTrace();
        }
      }
    }
  }
}
