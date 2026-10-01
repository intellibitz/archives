package com.androidrocks.bex.server.manager

import com.androidrocks.bex.server.persistent.TradeBook
import com.androidrocks.bex.server.persistent.WishBook
import javax.jdo.PersistenceManager
import javax.jdo.Transaction
import java.util.List
import java.util.logging.Logger

class BookManager private constructor() {
    companion object {
        private val log: Logger = Logger.getLogger(BookManager::class.java
            .getName())
        @JvmStatic
        fun saveWishBookWithCustomKey(book: WishBook?) {

                        val pm = PMF.get().persistenceManager
                        pm.setDetachAllOnCommit(true)
                        val tx = pm.currentTransaction()
                        try {
                            tx.begin()
                            book.setKey(TypeFactory.createWishBookKeyWithPrefix(book.getId()))
                            pm.makePersistent(book)
                            log.info("#saveBookWithCustomKey: " + book)
                            tx.commit()
                        } finally {
                            if (tx.isActive) {
                                tx.rollback()
                            }
                            pm.close()
                        }
        }

        @JvmStatic
        fun saveTradeBookWithCustomKey(book: TradeBook?) {

                        val pm = PMF.get().persistenceManager
                        val tx = pm.currentTransaction()
                        try {
                            tx.begin()
                            book.setKey(TypeFactory.createTradeBookKeyWithPrefix(book.getId()))
                            pm.makePersistent(book)
                            log.info("#saveBookWithCustomKey: " + book)
                            tx.commit()
                        } finally {
                            if (tx.isActive) {
                                tx.rollback()
                            }
                            pm.close()
                        }
        }

        @JvmStatic
        fun saveWishList(books: List<WishBook>?) {

                    for (book in books) {
                        saveWishBookWithCustomKey(book)
                    }
        }

        @JvmStatic
        fun saveTradeList(books: List<TradeBook>?) {

                    for (book in books) {
                        saveTradeBookWithCustomKey(book)
                    }
        }

    }
}
