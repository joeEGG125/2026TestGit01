package com.syscom.fep.web.dialect;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.thymeleaf.processor.IProcessor;
import org.thymeleaf.templatemode.TemplateMode;

import com.syscom.fep.web.dialect.processor.impl.ExtendFragmentProcessor;

import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
import nz.net.ultraq.thymeleaf.layoutdialect.fragments.FragmentProcessor;

@Component
public class ExtendLayoutDialect extends LayoutDialect {

	@Override
	public Set<IProcessor> getProcessors(String dialectPrefix) {
		Set<IProcessor> set = super.getProcessors(dialectPrefix);
		set = set.stream().filter(t -> !(t instanceof FragmentProcessor)).collect(Collectors.toSet());
		set.add(new ExtendFragmentProcessor(TemplateMode.HTML, dialectPrefix));
		set.add(new ExtendFragmentProcessor(TemplateMode.XML, dialectPrefix));
		return set;
	}
}
