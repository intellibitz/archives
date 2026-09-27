package com.androidrocks.bex.server.manager;

import com.androidrocks.bex.server.persistent.TradeBook;
import com.androidrocks.bex.server.persistent.WishBook;

import javax.jdo.PersistenceManager;
import javax.jdo.Transaction;
import java.util.List;
import java.util.logging.Logger;

public final class BookManager {

    private static final Logger log = Logger.getLogger(BookManager.class
            .getName());

    private BookManager() {
    }

    public static void saveWishBookWithCustomKey(WishBook book) {
            PersistenceManager pm = PMF.get().getPersistenceManager();
            pm.setDetachAllOnCommit(true);
            Transaction tx = pm.currentTransaction();
            try {
                tx.begin();
                book.setKey(TypeFactory.createWishBookKeyWithPrefix(book.getId()));
                pm.makePersistent(book);
                log.info("#saveBookWithCustomKey: " + book);
                tx.commit();
            } finally {
                if (tx.isActive()) {
                    tx.rollback();
                }
                pm.close();
            }
    }

    public static void saveTradeBookWithCustomKey(TradeBook book) {
            PersistenceManager pm = PMF.get().getPersistenceManager();
            Transaction tx = pm.currentTransaction();
            try {
                tx.begin();
                book.setKey(TypeFactory.createTradeBookKeyWithPrefix(book.getId()));
                pm.makePersistent(book);
                log.info("#saveBookWithCustomKey: " + book);
                tx.commit();
            } finally {
                if (tx.isActive()) {
                    tx.rollback();
                }
                pm.close();
            }
    }

    public static void saveWishList(List<WishBook> books) {
        for (WishBook book : books) {
            saveWishBookWithCustomKey(book);
        }
    }

    public static void saveTradeList(List<TradeBook> books) {
        for (TradeBook book : books) {
            saveTradeBookWithCustomKey(book);
        }
    }
}