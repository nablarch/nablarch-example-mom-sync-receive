package com.nablarch.example;

import nablarch.test.core.messaging.MessagingRequestTestSupport;
import nablarch.test.junit5.extension.messaging.MessagingRequestTest;
import org.junit.jupiter.api.Test;

/**
 * {@link ProjectInsertMessageAction}のテストクラス。
 *
 */
@MessagingRequestTest
class ProjectInsertMessageRequestTest {
    MessagingRequestTestSupport support;

    /** 正常終了のテストケース。 */
    @Test
    void testNormalEnd() {
        support.execute(support.testName.getMethodName());
    }

    /** 異常終了のテストケース。 */
    @Test
    void testValidationError() {
        support.execute(support.testName.getMethodName());
    }
}
