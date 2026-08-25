package com.syscom.fep.mybatis.his.generator;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.IntrospectedTable.TargetRuntime;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.plugins.EqualsHashCodePlugin;

import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import static org.mybatis.generator.internal.util.JavaBeansUtil.getGetterMethodName;
import static org.mybatis.generator.internal.util.StringUtility.isTrue;

public class ChangeHashCodeModelPlugin extends EqualsHashCodePlugin {
    private boolean useEqualsHashCodeFromRoot;

    @Override
    public void setProperties(Properties properties) {
        super.setProperties(properties);
        useEqualsHashCodeFromRoot = isTrue(properties.getProperty("useEqualsHashCodeFromRoot")); //$NON-NLS-1$
    }

    protected void generateHashCode(TopLevelClass topLevelClass,
                                    List<IntrospectedColumn> introspectedColumns,
                                    IntrospectedTable introspectedTable) {
        Method method = new Method("hashCode"); //$NON-NLS-1$
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(FullyQualifiedJavaType.getIntInstance());
        method.addAnnotation("@Override"); //$NON-NLS-1$

        if (introspectedTable.getTargetRuntime() == TargetRuntime.MYBATIS3_DSQL) {
            context.getCommentGenerator().addGeneralMethodAnnotation(method, introspectedTable,
                    topLevelClass.getImportedTypes());
        } else {
            context.getCommentGenerator().addGeneralMethodComment(method,
                    introspectedTable);
        }

        final int prime = 31; //$NON-NLS-1$
        method.addBodyLine("int result = 1;"); //$NON-NLS-1$

        if (useEqualsHashCodeFromRoot && topLevelClass.getSuperClass().isPresent()) {
            method.addBodyLine("result = " + prime + " * result + super.hashCode();"); //$NON-NLS-1$
        }

        StringBuilder sb = new StringBuilder();
        boolean hasTemp = false;
        Iterator<IntrospectedColumn> iter = introspectedColumns.iterator();
        while (iter.hasNext()) {
            IntrospectedColumn introspectedColumn = iter.next();

            FullyQualifiedJavaType fqjt = introspectedColumn
                    .getFullyQualifiedJavaType();

            String getterMethod = getGetterMethodName(
                    introspectedColumn.getJavaProperty(), fqjt);

            sb.setLength(0);
            if (fqjt.isPrimitive()) {
                if ("boolean".equals(fqjt.getFullyQualifiedName())) { //$NON-NLS-1$
                    sb.append("result = " + prime + " * result + ("); //$NON-NLS-1$
                    sb.append(getterMethod);
                    sb.append("() ? 1231 : 1237);"); //$NON-NLS-1$
                    method.addBodyLine(sb.toString());
                } else if ("byte".equals(fqjt.getFullyQualifiedName())) { //$NON-NLS-1$
                    sb.append("result = " + prime + " * result + "); //$NON-NLS-1$
                    sb.append(getterMethod);
                    sb.append("();"); //$NON-NLS-1$
                    method.addBodyLine(sb.toString());
                } else if ("char".equals(fqjt.getFullyQualifiedName())) { //$NON-NLS-1$
                    sb.append("result = " + prime + " * result + "); //$NON-NLS-1$
                    sb.append(getterMethod);
                    sb.append("();"); //$NON-NLS-1$
                    method.addBodyLine(sb.toString());
                } else if ("double".equals(fqjt.getFullyQualifiedName())) { //$NON-NLS-1$
                    if (!hasTemp) {
                        method.addBodyLine("long temp;"); //$NON-NLS-1$
                        hasTemp = true;
                    }
                    sb.append("temp = Double.doubleToLongBits("); //$NON-NLS-1$
                    sb.append(getterMethod);
                    sb.append("());"); //$NON-NLS-1$
                    method.addBodyLine(sb.toString());
                    method
                            .addBodyLine("result = " + prime + " * result + (int) (temp ^ (temp >>> 32));"); //$NON-NLS-1$
                } else if ("float".equals(fqjt.getFullyQualifiedName())) { //$NON-NLS-1$
                    sb
                            .append("result = " + prime + " * result + Float.floatToIntBits("); //$NON-NLS-1$
                    sb.append(getterMethod);
                    sb.append("());"); //$NON-NLS-1$
                    method.addBodyLine(sb.toString());
                } else if ("int".equals(fqjt.getFullyQualifiedName())) { //$NON-NLS-1$
                    sb.append("result = " + prime + " * result + "); //$NON-NLS-1$
                    sb.append(getterMethod);
                    sb.append("();"); //$NON-NLS-1$
                    method.addBodyLine(sb.toString());
                } else if ("long".equals(fqjt.getFullyQualifiedName())) { //$NON-NLS-1$
                    sb.append("result = " + prime + " * result + (int) ("); //$NON-NLS-1$
                    sb.append(getterMethod);
                    sb.append("() ^ ("); //$NON-NLS-1$
                    sb.append(getterMethod);
                    sb.append("() >>> 32));"); //$NON-NLS-1$
                    method.addBodyLine(sb.toString());
                } else if ("short".equals(fqjt.getFullyQualifiedName())) { //$NON-NLS-1$
                    sb.append("result = " + prime + " * result + "); //$NON-NLS-1$
                    sb.append(getterMethod);
                    sb.append("();"); //$NON-NLS-1$
                    method.addBodyLine(sb.toString());
                }
            } else if (fqjt.isArray()) {
                // Arrays is already imported by the generateEquals method, we don't need
                // to do it again
                sb.append("result = " + prime + " * result + (Arrays.hashCode("); //$NON-NLS-1$
                sb.append(getterMethod);
                sb.append("()));"); //$NON-NLS-1$
                method.addBodyLine(sb.toString());
            } else {
                sb.append("result = " + prime + " * result + (("); //$NON-NLS-1$
                sb.append(getterMethod);
                sb.append("() == null) ? 0 : "); //$NON-NLS-1$
                sb.append(getterMethod);
                sb.append("().hashCode());"); //$NON-NLS-1$
                method.addBodyLine(sb.toString());
            }
        }

        method.addBodyLine("return result;"); //$NON-NLS-1$

        topLevelClass.addMethod(method);
    }
}
