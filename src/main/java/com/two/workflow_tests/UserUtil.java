package com.two.workflow_tests;

import net.bytebuddy.utility.RandomString;

class UserUtil {

    static String uniqueEmail() {
        return ("workflowTest-" + RandomString.make(10) + "@two.com");
    }

}
