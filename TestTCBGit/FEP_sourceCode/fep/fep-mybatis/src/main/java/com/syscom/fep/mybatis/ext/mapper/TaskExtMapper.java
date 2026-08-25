package com.syscom.fep.mybatis.ext.mapper;

import com.syscom.fep.mybatis.mapper.TaskMapper;
import com.syscom.fep.mybatis.model.Task;
import org.apache.ibatis.annotations.Param;

import jakarta.annotation.Resource;
import java.util.List;

@Resource
public interface TaskExtMapper extends TaskMapper {
    /**
     * zk 2022-01-14
     * */
    List<Task> queryTaskAll();


    /**
     * xy add
     */
    List<Task> getTaskByName(@Param("taskName") String taskName, @Param("direction") String direction);

    Integer updateTASKByNAME(Task task);

    Task selectTaskforName(Task task);

    /**
     * LeYun add
     */
    List<Task> getUCDTaskByName(@Param("taskName") String taskName);

}