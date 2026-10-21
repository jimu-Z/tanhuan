package com.ruoyi.talk.service;

import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.test.util.ReflectionTestUtils;
import com.ruoyi.talk.service.impl.*;

final class StudentImportTestSupport {
    static PlatformTransactionManager transactionManager(TalkStudentServiceImpl service) {
        PlatformTransactionManager transactions=mock(PlatformTransactionManager.class);
        when(transactions.getTransaction(any())).thenAnswer(call->new SimpleTransactionStatus());
        ReflectionTestUtils.setField(service,"transactionManager",transactions);
        return transactions;
    }
    static void unrelatedRelations(TalkStudentServiceImpl service) {
        transactionManager(service);
        ReflectionTestUtils.setField(service,"importRelations",mock(StudentImportRelationsService.class));
    }
}
