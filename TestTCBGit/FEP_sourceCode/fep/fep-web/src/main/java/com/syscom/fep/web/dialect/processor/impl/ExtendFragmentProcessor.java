package com.syscom.fep.web.dialect.processor.impl;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.engine.AttributeName;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.IOpenElementTag;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.templatemode.TemplateMode;

import com.syscom.fep.web.entity.PageView;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.entity.User;
import com.syscom.fep.web.util.WebUtil;

import nz.net.ultraq.thymeleaf.layoutdialect.fragments.FragmentProcessor;

public class ExtendFragmentProcessor extends FragmentProcessor {
    private static final String ATTR_VALUE_PANEL_CONDITION = "panel-condition";
    private static final String ATTR_NAME_CLASS = "class";
    private static final String CLASS_VALUE_PANEL_HIDDEN = "panel-hidden";

    public ExtendFragmentProcessor(TemplateMode templateMode, String dialectPrefix) {
        super(templateMode, dialectPrefix);
    }

    @Override
    protected void doProcess(ITemplateContext context, IProcessableElementTag tag, AttributeName attributeName, String attributeValue, IElementTagStructureHandler structureHandler) {
        super.doProcess(context, tag, attributeName, attributeValue, structureHandler);
        @SuppressWarnings("unchecked")
        Map<String, List<Object>> map = (Map<String, List<Object>>) context.getVariable("LayoutDialect::FragmentCollection");
        if (MapUtils.isNotEmpty(map)) {
            List<Object> list = map.get(attributeValue);
            if (CollectionUtils.isNotEmpty(list)) {
                if (ATTR_VALUE_PANEL_CONDITION.equals(attributeValue)) {
                    User user = WebUtil.getUser();
                    if (user == null) {
                        return;
                    }
                    IModel mode = (IModel) list.get(0);
                    if (mode.size() > 0) {
                        IOpenElementTag elementTag = (IOpenElementTag) mode.get(0);
                        String templateName = elementTag.getTemplateName();
                        Router router = Router.fromView(templateName);
                        if (router == null) return; // 理論上這裡不會出現null的情況, 除非是沒有增加到Router中
                        PageView pageView = user.addPageView(router);
                        pageView.setCollapseButtonShownForConditionPanel(true);
                        if (StringUtils.contains(elementTag.getAttributeValue(ATTR_NAME_CLASS), CLASS_VALUE_PANEL_HIDDEN)) {
                            pageView.setCollapseButtonShownForConditionPanel(false);
                        }
                    }
                }
            } else {
                structureHandler.removeElement();
            }
        }
    }
}
