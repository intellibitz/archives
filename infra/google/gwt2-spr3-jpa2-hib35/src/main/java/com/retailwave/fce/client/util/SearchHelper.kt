package com.retailwave.fce.client.util

import com.retailwave.fce.shared.dto.UserDTO

/**
 * $Id: SearchHelper.java 5 2010-06-03 11:07:35Z muthu $
 * $HeadURL: svn://10.10.200.111:3691/Finance/tags/framework-snapshot1/fce/src/main/java/com/retailwave/fce/client/util/SearchHelper.java $
 */

class SearchHelper private constructor() {
    companion object {
        @JvmStatic
        fun enableLike(criteria: UserDTO) {
            criteria.name = enableLike(criteria.name)
            criteria.fullName = enableLike(criteria.fullName)
            criteria.emailAddress = enableLike(criteria.emailAddress)
        }

        @JvmStatic
        fun enableLike(value: String?): String? {
            if (null == value) {
                return null
            }
            return "%$value%"
        }
    }
}
