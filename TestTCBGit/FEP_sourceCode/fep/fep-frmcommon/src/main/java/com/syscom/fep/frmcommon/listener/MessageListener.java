package com.syscom.fep.frmcommon.listener;

import java.io.Serializable;
import java.util.EventListener;
import java.util.List;

public interface MessageListener<M extends Serializable> extends EventListener {

	default public void onMessage(M message) {}

	default public void onMessage(M[] messages) {}

	default public void onMessage(List<M> messageList) {}

}
