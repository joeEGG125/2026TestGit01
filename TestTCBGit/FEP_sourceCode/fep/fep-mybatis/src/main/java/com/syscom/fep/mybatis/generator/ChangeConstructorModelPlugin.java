package com.syscom.fep.mybatis.generator;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ResourceUtils;

import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.mybatis.vo.MybatisConstant;

/**
 * 將繼承Model類中的構建函數改為protected
 * 
 * @author Richard
 *
 */
public class ChangeConstructorModelPlugin extends PluginAdapter {
	private static final LogHelper logger = new LogHelper();
	private static final List<String> extModelNameList = new ArrayList<>();
	private static final List<String> excludeModelName = Arrays.asList("Fwdtxn", "Rmout");

	static {
		try {
			org.springframework.core.io.Resource[] resources =
					new PathMatchingResourcePatternResolver().getResources(StringUtils.join(
							ResourceUtils.CLASSPATH_URL_PREFIX, MybatisConstant.PATH_EXT_MODEL));
			for (org.springframework.core.io.Resource resource : resources) {
				String fileName = resource.getFilename();
				fileName = fileName.substring(0, fileName.length() - MybatisConstant.FILE_NAME_SUFFIX_EXT_MODEL.length());
				extModelNameList.add(fileName);
			}
		} catch (FileNotFoundException e) {
			logger.warn(e.getMessage());
		} catch (IOException e) {
			logger.exceptionMsg(e, e.getMessage());
		}
	}

	@Override
	public boolean validate(List<String> warnings) {
		return true;
	}

	@Override
	public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		String modelName = introspectedTable.getFullyQualifiedTable().getDomainObjectName();
		modelName = modelName.equals(MybatisConstant.ENTITY_NAME_FEPTXN01) ? MybatisConstant.REPLACE_ENTITY_NAME_FOR_FEPTXN01 : modelName;
		// 不需要修改的直接返回true
		if(CollectionUtils.contains(excludeModelName.iterator(), modelName)) {
			return true;
		}
		// 取出構建函數, 改成protected
		else if (CollectionUtils.contains(extModelNameList.iterator(), modelName)) {
			List<Method> constructorMethodList = topLevelClass.getMethods().stream().filter(x -> x.isConstructor()).collect(Collectors.toList());
			for (Method constructorMethod : constructorMethodList) {
				constructorMethod.setVisibility(JavaVisibility.PROTECTED);
			}
		}
		return true;
	}
}
