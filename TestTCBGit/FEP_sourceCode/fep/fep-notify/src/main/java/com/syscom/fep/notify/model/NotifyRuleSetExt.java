package com.syscom.fep.notify.model;

import com.syscom.fep.mybatis.model.Notifyruleset;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Setter
@Getter
public class NotifyRuleSetExt  implements Serializable, Cloneable {
    private static final long serialVersionUID = -7420049018526259961L;
    private Long ruleSetId;
    private String ruleName;
    private String ruleExpression;
    private String customerComponent;
    private Integer updateUserId;
    private Date updateTime;

    private String expression;

    public NotifyRuleSetExt(Notifyruleset notifyruleset) {
        this.ruleSetId = notifyruleset.getRulesetId();
        this.ruleName =notifyruleset.getRuleName();
        this.ruleExpression = notifyruleset.getRuleExpression();
        this.customerComponent = notifyruleset.getCustomerComponent();
        this.updateUserId = notifyruleset.getUpdateUserId();
        this.updateTime = notifyruleset.getUpdateTime();
    }

    @Override
    public NotifyRuleSetExt clone() {
        NotifyRuleSetExt object = null;
        try {
            object = (NotifyRuleSetExt) super.clone();
            return object;
        } catch (CloneNotSupportedException e) {
            // e.printStackTrace();
            com.syscom.fep.common.log.LogHelperFactory.getTraceLogger().error(e, "clone failed, ", e.getMessage());
        }
        return object;
    }
}
