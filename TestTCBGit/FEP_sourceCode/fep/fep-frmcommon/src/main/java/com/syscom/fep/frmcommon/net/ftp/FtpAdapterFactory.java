package com.syscom.fep.frmcommon.net.ftp;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * FTP工廠類, FTP處理的實例使用此類來取
 * 
 * @author Richard
 *
 */
@Component
@Lazy
public class FtpAdapterFactory implements BeanFactoryAware {
	protected BeanFactory beanFactory;

	public FtpAdapter createFtpAdapter(FtpProperties ftpProperties) {
		switch (ftpProperties.getProtocol()) {
			case FTP:
				return new FtpClient(ftpProperties, beanFactory);
			case SFTP:
				return new SftpClient(ftpProperties, beanFactory);
			default:
				return new FtpClient(ftpProperties, beanFactory);
		}
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		if (this.beanFactory == null) {
			this.beanFactory = beanFactory;
		}
	}
}
