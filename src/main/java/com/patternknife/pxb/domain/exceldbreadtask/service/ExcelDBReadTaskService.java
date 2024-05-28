package com.patternknife.pxb.domain.exceldbreadtask.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.patternknife.pxb.config.response.error.exception.data.ResourceNotFoundException;

import com.patternknife.pxb.domain.exceldbcommontask.dto.ExcelDBCommonTaskResDTO;
import com.patternknife.pxb.domain.exceldbreadtask.dao.ExcelDBReadTaskRepositorySupport;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;


@RequiredArgsConstructor
@Service
public class ExcelDBReadTaskService {


    private final ExcelDBReadTaskRepositorySupport excelDBReadTaskRepositorySupport;

    public Page<ExcelDBCommonTaskResDTO.OneRes> findExcelDBReadTasks(Boolean skipPagination,
                                                                     Integer pageNum,
                                                                     Integer pageSize,
                                                                     String excelDBReadTaskSearchFilter,
                                                                     String sorterValueFilter,
                                                                     String dateRangeFilter, Long groupId) throws JsonProcessingException, ResourceNotFoundException {

        return excelDBReadTaskRepositorySupport.findExcelDBReadTasks(skipPagination, pageNum, pageSize, excelDBReadTaskSearchFilter, sorterValueFilter, dateRangeFilter, groupId);

    }

    public ExcelDBCommonTaskResDTO.StatusRes findExcelDBReadTaskCountsByStatus(Long groupId){
        return excelDBReadTaskRepositorySupport.findExcelDBReadTaskCountsByStatus(groupId);
    }

    public ExcelDBCommonTaskResDTO.StartEndTimestampRes findExcelDBReadTaskStartEndTimestamps(Long groupId){
        return excelDBReadTaskRepositorySupport.findStartEndTimestampsByGroupId(groupId);
    }
}