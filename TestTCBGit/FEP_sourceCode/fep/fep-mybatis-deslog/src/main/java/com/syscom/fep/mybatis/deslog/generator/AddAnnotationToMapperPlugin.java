package com.syscom.fep.mybatis.deslog.generator;

import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.mybatis.deslog.vo.MybatisConstant;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ResourceUtils;

import jakarta.annotation.Resource;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 非繼承類的Mapper增加@Resource注入
 * 
 * @author Richard
 *
 */
public class AddAnnotationToMapperPlugin extends PluginAdapter {
	private static final LogHelper logger = new LogHelper();
	private static final List<String> extModelNameList = new ArrayList<>();

	static {
		try {
			org.springframework.core.io.Resource[] resources =
					new PathMatchingResourcePatternResolver().getResources(StringUtils.join(
							ResourceUtils.CLASSPATH_URL_PREFIX, MybatisConstant.PATH_EXT_MAPPER));
			for (org.springframework.core.io.Resource resource : resources) {
				String fileName = resource.getFilename();
				fileName = fileName.substring(0, fileName.length() - MybatisConstant.FILE_NAME_SUFFIX_EXT_MAPPER.length());
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
	public boolean clientGenerated(Interface interfaze, IntrospectedTable introspectedTable) {
		String modelName = introspectedTable.getFullyQualifiedTable().getDomainObjectName();
		modelName = modelName.equals(MybatisConstant.ENTITY_NAME_DESLOG1) ? MybatisConstant.REPLACE_ENTITY_NAME_FOR_DESLOG1 : modelName;
		if (!CollectionUtils.contains(extModelNameList.iterator(), modelName)) {
			interfaze.getAnnotations().add(StringUtils.join("@", Resource.class.getSimpleName()));
			interfaze.getImportedTypes().add(new FullyQualifiedJavaType(Resource.class.getName()));
		}
		return true;
	}

}
