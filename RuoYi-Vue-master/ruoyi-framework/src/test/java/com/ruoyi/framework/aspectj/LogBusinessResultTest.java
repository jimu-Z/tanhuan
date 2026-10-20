package com.ruoyi.framework.aspectj;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessStatus;
import com.ruoyi.system.domain.SysOperLog;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class LogBusinessResultTest {
    private final LogAspect aspect = new LogAspect();

    @Test void rejectedBusinessOperationIsFailureWithReason() {
        SysOperLog log = result(null, AjaxResult.error("角色名称已存在"));
        assertThat(log.getStatus()).isEqualTo(BusinessStatus.FAIL.ordinal());
        assertThat(log.getErrorMsg()).isEqualTo("角色名称已存在");
    }

    @Test void validationWarningAndForbiddenResultAreFailures() {
        assertThat(result(null, AjaxResult.warn("已分配不能删除")).getStatus()).isEqualTo(1);
        assertThat(result(null, AjaxResult.error(403, "无权操作")).getStatus()).isEqualTo(1);
    }

    @Test void successfulResultKeepsSuccessEvenWithErrorWordsInData() {
        SysOperLog log = result(null, AjaxResult.success(Map.of("code", 500, "msg", "历史失败记录")));
        assertThat(log.getStatus()).isZero();
        assertThat(log.getErrorMsg()).isNull();
    }

    @Test void untypedPayloadAndNoResponseDoNotBecomeFailures() {
        assertThat(result(null, null).getStatus()).isZero();
        assertThat(result(null, Map.of("code", 500)).getStatus()).isZero();
        assertThat(result(null, new AjaxResult()).getStatus()).isZero();
    }

    @Test void thrownExceptionTakesPrecedenceOverReturnedResult() {
        SysOperLog log = result(new IllegalStateException("真实异常"), AjaxResult.error("返回提示"));
        assertThat(log.getStatus()).isEqualTo(1);
        assertThat(log.getErrorMsg()).isEqualTo("真实异常");
    }

    @Test void failureMessageRemainsBoundedForDatabaseColumn() {
        assertThat(result(null, AjaxResult.error("测".repeat(2500))).getErrorMsg()).hasSize(2000);
    }

    private SysOperLog result(Exception error, Object response) {
        SysOperLog log = new SysOperLog();
        aspect.setOperationResult(log, error, response);
        return log;
    }
}
