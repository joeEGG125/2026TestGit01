package com.syscom.fep.mybatis.deslog.generator;

import com.syscom.fep.mybatis.deslog.vo.MybatisConstant;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.OutputUtilities;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.plugins.EqualsHashCodePlugin;

import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import static org.mybatis.generator.internal.util.JavaBeansUtil.getGetterMethodName;
import static org.mybatis.generator.internal.util.StringUtility.isTrue;

public class ToStringEqualsHashCodeModelPlugin extends EqualsHashCodePlugin {

    private boolean useEqualsHashCodeFromRoot;
    private boolean useToStringFromRoot;

    @Override
    public void setProperties(Properties properties) {
        super.setProperties(properties);
        useEqualsHashCodeFromRoot = isTrue(properties.getProperty("useEqualsHashCodeFromRoot")); //$NON-NLS-1$
        useToStringFromRoot = isTrue(properties.getProperty("useToStringFromRoot")); //$NON-NLS-1$
    }

    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        this.generateTableNameSuffixField(topLevelClass, introspectedTable);
        super.modelBaseRecordClassGenerated(topLevelClass, introspectedTable);
        this.generateToString(topLevelClass, introspectedTable);
        return true;
    }

    @Override
    public boolean modelPrimaryKeyClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        this.generateTableNameSuffixField(topLevelClass, introspectedTable);
        super.modelPrimaryKeyClassGenerated(topLevelClass, introspectedTable);
        this.generateToString(topLevelClass, introspectedTable);
        return true;
    }

    @Override
    public boolean modelRecordWithBLOBsClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        this.generateTableNameSuffixField(topLevelClass, introspectedTable);
        super.modelRecordWithBLOBsClassGenerated(topLevelClass, introspectedTable);
        this.generateToString(topLevelClass, introspectedTable);
        return true;
    }

    @Override
    protected void generateEquals(TopLevelClass topLevelClass,
                                  List<IntrospectedColumn> introspectedColumns,
                                  IntrospectedTable introspectedTable) {
        Method method = new Method("equals"); //$NON-NLS-1$
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(FullyQualifiedJavaType
                .getBooleanPrimitiveInstance());
        method.addParameter(new Parameter(FullyQualifiedJavaType
                .getObjectInstance(), "that")); //$NON-NLS-1$
        method.addAnnotation("@Override"); //$NON-NLS-1$

        if (introspectedTable.getTargetRuntime() == IntrospectedTable.TargetRuntime.MYBATIS3_DSQL) {
            context.getCommentGenerator().addGeneralMethodAnnotation(method, introspectedTable,
                    topLevelClass.getImportedTypes());
        } else {
            context.getCommentGenerator().addGeneralMethodComment(method,
                    introspectedTable);
        }

        method.addBodyLine("if (this == that) {"); //$NON-NLS-1$
        method.addBodyLine("return true;"); //$NON-NLS-1$
        method.addBodyLine("}"); //$NON-NLS-1$

        method.addBodyLine("if (that == null) {"); //$NON-NLS-1$
        method.addBodyLine("return false;"); //$NON-NLS-1$
        method.addBodyLine("}"); //$NON-NLS-1$

        method.addBodyLine("if (getClass() != that.getClass()) {"); //$NON-NLS-1$
        method.addBodyLine("return false;"); //$NON-NLS-1$
        method.addBodyLine("}"); //$NON-NLS-1$

        StringBuilder sb = new StringBuilder();
        sb.append(topLevelClass.getType().getShortName());
        sb.append(" other = ("); //$NON-NLS-1$
        sb.append(topLevelClass.getType().getShortName());
        sb.append(") that;"); //$NON-NLS-1$
        method.addBodyLine(sb.toString());

        if (useEqualsHashCodeFromRoot && topLevelClass.getSuperClass().isPresent()) {
            method.addBodyLine("if (!super.equals(other)) {"); //$NON-NLS-1$
            method.addBodyLine("return false;"); //$NON-NLS-1$
            method.addBodyLine("}"); //$NON-NLS-1$
        }

        boolean first = true;
        Iterator<IntrospectedColumn> iter = introspectedColumns.iterator();
        while (iter.hasNext()) {
            IntrospectedColumn introspectedColumn = iter.next();

            sb.setLength(0);

            if (first) {
                // Only for DESLOG of tableNameSuffix
                if (MybatisConstant.TABLE_NAME_DESLOG1.equals(introspectedTable.getFullyQualifiedTable().getIntrospectedTableName())) {
                    sb.append("return "); //$NON-NLS-1$
                    sb.append("(this.getTableNameSuffix() == null ? other.getTableNameSuffix() == null : this.getTableNameSuffix().equals(other.getTableNameSuffix()))\r\n");
                    OutputUtilities.javaIndent(sb, 3);
                    sb.append("&& ("); //$NON-NLS-1$
                } else {
                    sb.append("return ("); //$NON-NLS-1$
                }
                first = false;
            } else {
                OutputUtilities.javaIndent(sb, 1);
                sb.append("&& ("); //$NON-NLS-1$
            }

            String getterMethod = getGetterMethodName(
                    introspectedColumn.getJavaProperty(), introspectedColumn
                            .getFullyQualifiedJavaType());

            if (introspectedColumn.getFullyQualifiedJavaType().isPrimitive()) {
                sb.append("this."); //$NON-NLS-1$
                sb.append(getterMethod);
                sb.append("() == "); //$NON-NLS-1$
                sb.append("other."); //$NON-NLS-1$
                sb.append(getterMethod);
                sb.append("())"); //$NON-NLS-1$
            } else if (introspectedColumn.getFullyQualifiedJavaType().isArray()) {
                topLevelClass.addImportedType("java.util.Arrays"); //$NON-NLS-1$
                sb.append("Arrays.equals(this."); //$NON-NLS-1$
                sb.append(getterMethod);
                sb.append("(), "); //$NON-NLS-1$
                sb.append("other."); //$NON-NLS-1$
                sb.append(getterMethod);
                sb.append("()))"); //$NON-NLS-1$
            } else {
                sb.append("this."); //$NON-NLS-1$
                sb.append(getterMethod);
                sb.append("() == null ? other."); //$NON-NLS-1$
                sb.append(getterMethod);
                sb.append("() == null : this."); //$NON-NLS-1$
                sb.append(getterMethod);
                sb.append("().equals(other."); //$NON-NLS-1$
                sb.append(getterMethod);
                sb.append("()))"); //$NON-NLS-1$
            }

            if (!iter.hasNext()) {
                sb.append(';');
            }

            method.addBodyLine(sb.toString());
        }

        topLevelClass.addMethod(method);
    }

    /**
     * Generates a <code>hashCode</code> method that includes all fields.
     *
     * <p>Note that this implementation is based on the eclipse foundation hashCode
     * generator.
     *
     * @param topLevelClass       the class to which the method will be added
     * @param introspectedColumns column definitions of this class and any superclass of this
     *                            class
     * @param introspectedTable   the table corresponding to this class
     */
    @Override
    protected void generateHashCode(TopLevelClass topLevelClass,
                                    List<IntrospectedColumn> introspectedColumns,
                                    IntrospectedTable introspectedTable) {
        Method method = new Method("hashCode"); //$NON-NLS-1$
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(FullyQualifiedJavaType.getIntInstance());
        method.addAnnotation("@Override"); //$NON-NLS-1$

        if (introspectedTable.getTargetRuntime() == IntrospectedTable.TargetRuntime.MYBATIS3_DSQL) {
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

        // Only for DESLOG of tableNameSuffix
        if (MybatisConstant.TABLE_NAME_DESLOG1.equals(introspectedTable.getFullyQualifiedTable().getIntrospectedTableName())) {
            method.addBodyLine("result = " + prime + " * result + ((getTableNameSuffix() == null) ? 0 : getTableNameSuffix().hashCode());");
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

    private void generateTableNameSuffixField(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        if (!MybatisConstant.TABLE_NAME_DESLOG1.equals(introspectedTable.getFullyQualifiedTable().getIntrospectedTableName())) {
            return;
        }
        // field
        Field tableNameSuffixField = new Field("tableNameSuffix", new FullyQualifiedJavaType(String.class.getName()));
        tableNameSuffixField.addJavaDocLine("/**");
        tableNameSuffixField.addJavaDocLine(" *");
        tableNameSuffixField.addJavaDocLine(" * This field was generated by MyBatis Generator.");
        tableNameSuffixField.addJavaDocLine(" *");
        tableNameSuffixField.addJavaDocLine(" * @mbg.generated");
        tableNameSuffixField.addJavaDocLine(" */");
        tableNameSuffixField.setVisibility(JavaVisibility.PRIVATE);
        topLevelClass.getFields().add(0, tableNameSuffixField);
        // getter
        Method tableNameSuffixGetterMethod = new Method("getTableNameSuffix");
        tableNameSuffixGetterMethod.addJavaDocLine("/**");
        tableNameSuffixGetterMethod.addJavaDocLine(" * This method was generated by MyBatis Generator.");
        tableNameSuffixGetterMethod.addJavaDocLine(" * ");
        tableNameSuffixGetterMethod.addJavaDocLine(" * @mbg.generated");
        tableNameSuffixGetterMethod.addJavaDocLine(" */");
        tableNameSuffixGetterMethod.setReturnType(new FullyQualifiedJavaType(String.class.getName()));
        tableNameSuffixGetterMethod.setVisibility(JavaVisibility.PUBLIC);
        tableNameSuffixGetterMethod.addBodyLine("return tableNameSuffix;");
        topLevelClass.getMethods().add(0, tableNameSuffixGetterMethod);
        // setter
        Method tableNameSuffixSetterMethod = new Method("setTableNameSuffix");
        tableNameSuffixSetterMethod.addJavaDocLine("/**");
        tableNameSuffixSetterMethod.addJavaDocLine(" * This method was generated by MyBatis Generator.");
        tableNameSuffixSetterMethod.addJavaDocLine(" * ");
        tableNameSuffixSetterMethod.addJavaDocLine(" * @mbg.generated");
        tableNameSuffixSetterMethod.addJavaDocLine(" */");
        tableNameSuffixSetterMethod.addParameter(new Parameter(new FullyQualifiedJavaType(String.class.getName()), "tableNameSuffix"));
        tableNameSuffixSetterMethod.setVisibility(JavaVisibility.PUBLIC);
        tableNameSuffixSetterMethod.addBodyLine("this.tableNameSuffix = tableNameSuffix;");
        topLevelClass.getMethods().add(1, tableNameSuffixSetterMethod);
    }

    private void generateToString(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        Method method = new Method("toString"); //$NON-NLS-1$
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(FullyQualifiedJavaType.getStringInstance());
        method.addAnnotation("@Override"); //$NON-NLS-1$
        if (introspectedTable.getTargetRuntime() == IntrospectedTable.TargetRuntime.MYBATIS3_DSQL) {
            context.getCommentGenerator().addGeneralMethodAnnotation(method, introspectedTable, topLevelClass.getImportedTypes());
        } else {
            context.getCommentGenerator().addGeneralMethodComment(method, introspectedTable);
        }
        method.addBodyLine("StringBuilder sb = new StringBuilder();"); //$NON-NLS-1$
        method.addBodyLine("sb.append(getClass().getSimpleName());"); //$NON-NLS-1$
        method.addBodyLine("sb.append(\" [\");"); //$NON-NLS-1$
        method.addBodyLine("sb.append(\"Hash = \").append(hashCode());"); //$NON-NLS-1$
        StringBuilder sb = new StringBuilder();
        for (Field field : topLevelClass.getFields()) {
            String property = field.getName();
            sb.setLength(0);
            sb.append("sb.append(\"").append(", ").append(property) //$NON-NLS-1$ //$NON-NLS-2$
                    .append("=\")").append(".append(").append(property) //$NON-NLS-1$ //$NON-NLS-2$
                    .append(");"); //$NON-NLS-1$
            method.addBodyLine(sb.toString());
        }
        method.addBodyLine("sb.append(\"]\");"); //$NON-NLS-1$
        if (useToStringFromRoot && topLevelClass.getSuperClass().isPresent()) {
            method.addBodyLine("sb.append(\", from super class \");"); //$NON-NLS-1$
            method.addBodyLine("sb.append(super.toString());"); //$NON-NLS-1$
        }
        method.addBodyLine("return sb.toString();"); //$NON-NLS-1$
        topLevelClass.addMethod(method);
    }
}
