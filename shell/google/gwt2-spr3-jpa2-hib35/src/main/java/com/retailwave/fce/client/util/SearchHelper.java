package com.retailwave.fce.client.util;

import com.retailwave.fce.shared.dto.UserDTO;

/**
 * $Id: SearchHelper.java 5 2010-06-03 11:07:35Z muthu $
 * $HeadURL: svn://10.10.200.111:3691/Finance/tags/framework-snapshot1/fce/src/main/java/com/retailwave/fce/client/util/SearchHelper.java $
 */

public class SearchHelper {

    private SearchHelper() {
    }

    public static void enableLike(UserDTO criteria) {
        criteria.setName(enableLike(criteria.getName()));
        criteria.setFullName(enableLike(criteria.getFullName()));
        criteria.setEmailAddress(enableLike(criteria.getEmailAddress()));
    }

    public static String enableLike(String val) {
        if (null == val) {
            return null;
        }
        return "%" + val + "%";
    }
}