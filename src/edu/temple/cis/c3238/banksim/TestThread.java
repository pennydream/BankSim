package edu.temple.cis.c3238.banksim;

import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Brendan Garrett
 */
class TestThread extends Thread {

    private final Bank bank;

    public TestThread(Bank b) {
        bank = b;
    }

    @Override
    public void run() {
        try {
            bank.test();
        } catch (InterruptedException ex) {        }
    }
}