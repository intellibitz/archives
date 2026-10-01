package com.androidrocks.bex.server

import javax.jdo.JDOHelper
import javax.jdo.PersistenceManagerFactory

class PMF private constructor() {
    companion object {
        private val pmfInstance: PersistenceManagerFactory =
            JDOHelper.getPersistenceManagerFactory("transactions-optional")

        @JvmStatic
        fun get(): PersistenceManagerFactory {
            return pmfInstance
        }
    }
}
