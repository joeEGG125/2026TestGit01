package com.syscom.fep.jms;

import com.syscom.fep.frmcommon.jms.JmsDefinition;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.function.Function;

/**
 * 所有的Queue名稱定義在這個類中
 *
 * @author Richard
 */
public class JmsQueueNames {
    /**
     * TestQueue
     */
    @Lazy
    private JmsDefinition test;
    /**
     * DeadQueue
     */
    @Lazy
    private JmsDefinition dead;
    /**
     * dead-fisc
     */
    @Lazy
    private JmsDefinition deadFisc;
    /**
     * 用於Batch
     */
    @Lazy
    private JmsDefinition batch;
    /**
     * 用於ATMGateway
     */
    @Lazy
    private JmsDefinition atmMon;
    /**
     * 用於EMS Service接收EMS log
     */
    @Lazy
    private JmsDefinition ems;
    /**
     * PYBATCH
     */
    @Lazy
    private JmsDefinition pyBatch;
    /**
     * PYBATCHAck
     */
    @Lazy
    private JmsDefinition pyBatchAck;
    /**
     * Nb
     */
    @Lazy
    private JmsDefinition nb;
    /**
     * NbQ
     */
    @Lazy
    private JmsDefinition nbQAck;

    /**
     * NbP
     */
    @Lazy
    private JmsDefinition nbPAck;
    /**
     * mb
     */
    @Lazy
    private JmsDefinition mb;
    /**
     * mb
     */
    @Lazy
    private JmsDefinition mbAck;
    /**
     * ivr
     */
    @Lazy
    private JmsDefinition vo;
    /**
     * ivr
     */
    @Lazy
    private JmsDefinition voAck;
    /**
     * eatm
     */
    @Lazy
    private JmsDefinition eatm;
    /**
     * eatm
     */
    @Lazy
    private JmsDefinition eatmAck;
    /**
     * hce
     */
    @Lazy
    private JmsDefinition hce;
    /**
     * hce
     */
    @Lazy
    private JmsDefinition hceAck;
    /**
     * cbspend
     */
    @Lazy
    private JmsDefinition cbspend;
    @Lazy
    private JmsDefinition mft;
    @Lazy
    private JmsDefinition mftAck;
    @Lazy
    private JmsDefinition twmp;
    @Lazy
    private JmsDefinition twmpAck;
    @Lazy
    private JmsDefinition fisc;
    @Lazy
    private JmsDefinition fiscAlternative;
    @Lazy
    private JmsDefinition mch;
    @Lazy
    private JmsDefinition mchAck;
    @Lazy
    private JmsDefinition eip;
    @Lazy
    private JmsDefinition eipAck;
    @Lazy
    private JmsDefinition eoi;
    @Lazy
    private JmsDefinition eoiAck;
    @Lazy
    private JmsDefinition vip;
    @Lazy
    private JmsDefinition vipAck;
    @Lazy
    private JmsDefinition nonvip;
    @Lazy
    private JmsDefinition nonvipAck;
    @Lazy
    private JmsDefinition sso;
    @Lazy
    private JmsDefinition ssoAck;
    @Lazy
    private JmsDefinition onl;
    @Lazy
    private JmsDefinition onlAck;
    @Lazy
    private JmsDefinition fido;
    @Lazy
    private JmsDefinition fidoAck;
    @Lazy
    private JmsDefinition digital;
    @Lazy
    private JmsDefinition digitalAck;
    @Lazy
    private JmsDefinition bizloan;
    @Lazy
    JmsDefinition bizloanAck;

    public JmsDefinition getSso() {
        return sso;
    }

    public void setSso(JmsDefinition sso) {
        this.sso = sso;
    }

    public JmsDefinition getSsoAck() {
        return ssoAck;
    }

    public void setSsoAck(JmsDefinition ssoAck) {
        this.ssoAck = ssoAck;
    }

    public JmsDefinition getOnl() {
        return onl;
    }

    public void setOnl(JmsDefinition onl) {
        this.onl = onl;
    }

    public JmsDefinition getOnlAck() {
        return onlAck;
    }

    public void setOnlAck(JmsDefinition onlAck) {
        this.onlAck = onlAck;
    }

    public JmsDefinition getFido() {
        return fido;
    }

    public void setFido(JmsDefinition fido) {
        this.fido = fido;
    }

    public JmsDefinition getFidoAck() {
        return fidoAck;
    }

    public void setFidoAck(JmsDefinition fidoAck) {
        this.fidoAck = fidoAck;
    }

    public JmsDefinition getDigital() {
        return digital;
    }

    public void setDigital(JmsDefinition digital) {
        this.digital = digital;
    }

    public JmsDefinition getDigitalAck() {
        return digitalAck;
    }

    public void setDigitalAck(JmsDefinition digitalAck) {
        this.digitalAck = digitalAck;
    }

    public JmsDefinition getBizloan() {
        return bizloan;
    }

    public void setBizloan(JmsDefinition bizloan) {
        this.bizloan = bizloan;
    }

    public JmsDefinition getBizloanAck() {
        return bizloanAck;
    }

    public void setBizloanAck(JmsDefinition bizloanAck) {
        this.bizloanAck = bizloanAck;
    }

    public JmsDefinition getVip() {
        return vip;
    }

    public void setVip(JmsDefinition vip) {
        this.vip = vip;
    }

    public JmsDefinition getVipAck() {
        return vipAck;
    }

    public void setVipAck(JmsDefinition vipAck) {
        this.vipAck = vipAck;
    }

    public JmsDefinition getNonvip() {
        return nonvip;
    }

    public void setNonvip(JmsDefinition nonvip) {
        this.nonvip = nonvip;
    }

    public JmsDefinition getNonvipAck() {
        return nonvipAck;
    }

    public void setNonvipAck(JmsDefinition nonvipAck) {
        this.nonvipAck = nonvipAck;
    }

    public JmsDefinition getEip() {
        return eip;
    }

    public void setEip(JmsDefinition eip) {
        this.eip = eip;
    }

    public JmsDefinition getEipAck() {
        return eipAck;
    }

    public void setEipAck(JmsDefinition eipAck) {
        this.eipAck = eipAck;
    }

    public JmsDefinition getEoi() {
        return eoi;
    }

    public void setEoi(JmsDefinition eoi) {
        this.eoi = eoi;
    }

    public JmsDefinition getEoiAck() {
        return eoiAck;
    }

    public void setEoiAck(JmsDefinition eoiAck) {
        this.eoiAck = eoiAck;
    }

    public JmsDefinition getMchAck() {
        return mchAck;
    }

    public void setMchAck(JmsDefinition mchAck) {
        this.mchAck = mchAck;
    }

    public JmsDefinition getMch() {
        return mch;
    }

    public void setMch(JmsDefinition mch) {
        this.mch = mch;
    }

    public JmsDefinition getTest() {
        return test;
    }

    public void setTest(JmsDefinition test) {
        this.test = test;
    }

    public JmsDefinition getDead() {
        return dead;
    }

    public void setDead(JmsDefinition dead) {
        this.dead = dead;
    }

    public JmsDefinition getDeadFisc() {
        return deadFisc;
    }

    public void setDeadFisc(JmsDefinition deadFisc) {
        this.deadFisc = deadFisc;
    }

    public JmsDefinition getBatch() {
        return batch;
    }

    public void setBatch(JmsDefinition batch) {
        this.batch = batch;
    }

    public JmsDefinition getAtmMon() {
        return atmMon;
    }

    public void setAtmMon(JmsDefinition atmMon) {
        this.atmMon = atmMon;
    }

    public JmsDefinition getEms() {
        return ems;
    }

    public void setEms(JmsDefinition ems) {
        this.ems = ems;
    }

    public JmsDefinition getPyBatch() {
        return pyBatch;
    }

    public void setPyBatch(JmsDefinition pyBatch) {
        this.pyBatch = pyBatch;
    }

    public JmsDefinition getPyBatchAck() {
        return pyBatchAck;
    }

    public void setPyBatchAck(JmsDefinition pyBatchAck) {
        this.pyBatchAck = pyBatchAck;
    }

    public JmsDefinition getNb() {
        return nb;
    }

    public void setNb(JmsDefinition nb) {
        this.nb = nb;
    }

    public JmsDefinition getNbQAck() {
        return nbQAck;
    }

    public void setNbQAck(JmsDefinition nbQAck) {
        this.nbQAck = nbQAck;
    }

    public JmsDefinition getNbPAck() {
        return nbPAck;
    }

    public void setNbPAck(JmsDefinition nbPAck) {
        this.nbPAck = nbPAck;
    }

    public JmsDefinition getMb() {
        return mb;
    }

    public void setMb(JmsDefinition mb) {
        this.mb = mb;
    }

    public JmsDefinition getMbAck() {
        return mbAck;
    }

    public void setMbAck(JmsDefinition mbAck) {
        this.mbAck = mbAck;
    }

    public JmsDefinition getVo() {
        return vo;
    }

    public void setVo(JmsDefinition vo) {
        this.vo = vo;
    }

    public JmsDefinition getVoAck() {
        return voAck;
    }

    public void setVoAck(JmsDefinition voAck) {
        this.voAck = voAck;
    }

    public JmsDefinition getEatm() {
        return eatm;
    }

    public void setEatm(JmsDefinition eatm) {
        this.eatm = eatm;
    }

    public JmsDefinition getEatmAck() {
        return eatmAck;
    }

    public void setEatmAck(JmsDefinition eatmAck) {
        this.eatmAck = eatmAck;
    }

    public JmsDefinition getHce() {
        return hce;
    }

    public void setHce(JmsDefinition hce) {
        this.hce = hce;
    }

    public JmsDefinition getHceAck() {
        return hceAck;
    }

    public void setHceAck(JmsDefinition hceAck) {
        this.hceAck = hceAck;
    }

    public JmsDefinition getCbspend() {
        return cbspend;
    }

    public void setCbspend(JmsDefinition cbspend) {
        this.cbspend = cbspend;
    }

    public JmsDefinition getMft() {
        return mft;
    }

    public void setMft(JmsDefinition mft) {
        this.mft = mft;
    }

    public JmsDefinition getMftAck() {
        return mftAck;
    }

    public void setMftAck(JmsDefinition mftAck) {
        this.mftAck = mftAck;
    }

    public JmsDefinition getTwmp() {
        return twmp;
    }

    public void setTwmp(JmsDefinition twmp) {
        this.twmp = twmp;
    }

    public JmsDefinition getTwmpAck() {
        return twmpAck;
    }

    public void setTwmpAck(JmsDefinition twmpAck) {
        this.twmpAck = twmpAck;
    }

    public JmsDefinition getFisc() {
        return fisc;
    }

    public void setFisc(JmsDefinition fisc) {
        this.fisc = fisc;
    }

    public JmsDefinition getFiscAlternative() {
        return fiscAlternative;
    }

    public void setFiscAlternative(JmsDefinition fiscAlternative) {
        this.fiscAlternative = fiscAlternative;
    }

    public void toString(StringBuilder sb, String configurationPropertiesPrefix) {
        Field[] fields = this.getClass().getDeclaredFields();
        if (ArrayUtils.isNotEmpty(fields)) {
            int repeat = 2;
            for (Field field : fields) {
                ReflectionUtils.makeAccessible(field);
                JmsDefinition jmsDefinition = (JmsDefinition) ReflectionUtils.getField(field, this);
                if (jmsDefinition != null) {
                    jmsDefinition.toString(sb, StringUtils.join(configurationPropertiesPrefix, ".", field.getName()));
                }
            }
        }
    }

    public static JmsDefinition getDefinition(JmsQueueNames queueNames, Function<JmsQueueNames, JmsDefinition> function) {
        if (queueNames != null && function != null) {
            return function.apply(queueNames);
        }
        return null;
    }

    public static String getQueueName(JmsQueueNames queueNames, Function<JmsQueueNames, JmsDefinition> function) {
        JmsDefinition definition = getDefinition(queueNames, function);
        return definition == null ? null : definition.getDestination();
    }
}
