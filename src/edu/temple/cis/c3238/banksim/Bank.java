package edu.temple.cis.c3238.banksim;

import java.util.concurrent.Semaphore;

/**
 * @author Cay Horstmann
 * @author Modified by Paul Wolfgang
 */
public class Bank {

    public static final int NTEST = 10;
    private final Account[] accounts;
    private long ntransacts = 0;
    private final int initialBalance;
    private final int numAccounts;
    private boolean open;
    private boolean isTesting;
    private int isTransfering;
    private int totalTests = 0;
    
    public Bank(int numAccounts, int initialBalance) {
        open = true;
        this.initialBalance = initialBalance;
        this.numAccounts = numAccounts;
        accounts = new Account[numAccounts];
        for (int i = 0; i < accounts.length; i++) {
            accounts[i] = new Account(this, i, initialBalance);
        }
        ntransacts = 0;
        
        isTransfering = 0;
        isTesting = false;
    }

    public void transfer(int from, int to, int amount) throws InterruptedException{
        accounts[from].waitForAvailableFunds(amount);
        waitForTesting();
        if (!open) return;
        if (accounts[from].withdraw(amount)) {
            accounts[to].deposit(amount);
        }
        finishedTransfering();
        if (shouldTest()){
            isTesting = true;
            new TestThread(this).start();
        }
    }
    
    public synchronized void finishedTransfering(){
        isTransfering--;
        notifyAll();
    }
    public synchronized void waitForTesting() throws InterruptedException{
        while(isTesting){
            wait();
        }
        isTransfering++;
    }

    public synchronized void test() throws InterruptedException {
        while(isTransfering>0){
            wait();
        }
        int sum = 0;
        for (Account account : accounts) {
            System.out.printf("%s %s%n", 
                    Thread.currentThread().toString(), account.toString());
            sum += account.getBalance();
        }
        System.out.println(Thread.currentThread().toString() + 
                " Sum: " + sum);
        if (sum != numAccounts * initialBalance) {
            System.out.println(Thread.currentThread().toString() + 
                    " Money was gained or lost");
            System.exit(1);
        } else {
            System.out.println(Thread.currentThread().toString() + 
                    " The bank is in balance");
        }
        totalTests++;
        isTesting = false;
        System.out.println("Total Tests: "+totalTests);
        notifyAll();
    }

    public int size() {
        return accounts.length;
    }
    
    public synchronized boolean isOpen() {return open;}
    
    public void closeBank() {
        synchronized (this) {
            open = false;
        }
        for (Account account : accounts) {
            synchronized(account) {
                account.notifyAll();
            }
        }
    }
    
    public synchronized boolean shouldTest() {
        return ++ntransacts % NTEST == 0;
    }
}
