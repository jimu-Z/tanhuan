package com.ruoyi.system.service;

import com.ruoyi.common.core.domain.entity.SysDictType;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DictUtils;
import com.ruoyi.system.mapper.SysDictDataMapper;
import com.ruoyi.system.mapper.SysDictTypeMapper;
import com.ruoyi.system.service.impl.SysDictTypeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class DictTypeBatchDeletionTest {
    private SysDictTypeServiceImpl service;
    private SysDictTypeMapper types;
    private SysDictDataMapper data;

    @BeforeEach
    void setup() {
        service = new SysDictTypeServiceImpl();
        types = mock(SysDictTypeMapper.class);
        data = mock(SysDictDataMapper.class);
        ReflectionTestUtils.setField(service, "dictTypeMapper", types);
        ReflectionTestUtils.setField(service, "dictDataMapper", data);
        when(types.selectDictTypeById(1L)).thenReturn(type(1L, "qa_empty"));
        when(types.selectDictTypeById(2L)).thenReturn(type(2L, "qa_protected"));
    }

    @Test
    void protectedLastTypeRejectsBeforeAnyDeletionOrCacheEviction() {
        when(data.countDictDataByType("qa_protected")).thenReturn(1);
        try (MockedStatic<DictUtils> cache = mockStatic(DictUtils.class)) {
            assertThatThrownBy(() -> service.deleteDictTypeByIds(new Long[]{1L, 2L}))
                .isInstanceOf(ServiceException.class).hasMessageContaining("qa_protected");
            verify(types, never()).deleteDictTypeById(anyLong());
            cache.verifyNoInteractions();
        }
    }

    @Test
    void protectedFirstTypeAlsoKeepsTheEntireBatch() {
        when(data.countDictDataByType("qa_protected")).thenReturn(1);
        try (MockedStatic<DictUtils> cache = mockStatic(DictUtils.class)) {
            assertThatThrownBy(() -> service.deleteDictTypeByIds(new Long[]{2L, 1L}))
                .isInstanceOf(ServiceException.class);
            verify(types, never()).deleteDictTypeById(anyLong());
            cache.verifyNoInteractions();
        }
    }

    @Test
    void emptyTypesAreAllValidatedBeforeTheFirstDeletion() {
        try (MockedStatic<DictUtils> cache = mockStatic(DictUtils.class)) {
            service.deleteDictTypeByIds(new Long[]{1L, 2L});
            InOrder order = inOrder(types, data);
            order.verify(types).selectDictTypeById(1L);
            order.verify(data).countDictDataByType("qa_empty");
            order.verify(types).selectDictTypeById(2L);
            order.verify(data).countDictDataByType("qa_protected");
            order.verify(types).deleteDictTypeById(1L);
            order.verify(types).deleteDictTypeById(2L);
            cache.verify(() -> DictUtils.removeDictCache("qa_empty"));
            cache.verify(() -> DictUtils.removeDictCache("qa_protected"));
        }
    }

    private SysDictType type(Long id, String key) {
        SysDictType type = new SysDictType();
        type.setDictId(id);
        type.setDictType(key);
        type.setDictName(key);
        return type;
    }
}