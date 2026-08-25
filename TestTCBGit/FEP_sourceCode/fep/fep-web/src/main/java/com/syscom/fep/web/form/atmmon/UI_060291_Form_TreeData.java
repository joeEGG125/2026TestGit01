package com.syscom.fep.web.form.atmmon;

import com.syscom.fep.mybatis.model.Msgctl;
import com.syscom.fep.mybatis.model.Txtype;

import java.util.List;

public class UI_060291_Form_TreeData  {

    private static final long serialVersionUID = 1L;

    private String id;
    private Short txType1;
    private String txType1Name;
    private Short txType2;
    private String txType2Name;
    private Txtype txtype;
    private List<Msgctl> msgctl;
    private int treeLevel;
    private boolean checked;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Short getTxType1() {
        return txType1;
    }

    public void setTxType1(Short txType1) {
        this.txType1 = txType1;
    }

    public String getTxType1Name() {
        return txType1Name;
    }

    public void setTxType1Name(String txType1Name) {
        this.txType1Name = txType1Name;
    }

    public Short getTxType2() {
        return txType2;
    }

    public void setTxType2(Short txType2) {
        this.txType2 = txType2;
    }

    public String getTxType2Name() {
        return txType2Name;
    }

    public void setTxType2Name(String txType2Name) {
        this.txType2Name = txType2Name;
    }

    public Txtype getTxtype() {
        return txtype;
    }

    public void setTxtype(Txtype txtype) {
        this.txtype = txtype;
    }

    public List<Msgctl> getMsgctl() {
        return msgctl;
    }

    public void setMsgctl(List<Msgctl> msgctl) {
        this.msgctl = msgctl;
    }

    public int getTreeLevel() {
        return treeLevel;
    }

    public void setTreeLevel(int treeLevel) {
        this.treeLevel = treeLevel;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    @Override
    public String toString() {
        return "UI_060291_Form_TreeData{" +
                "id='" + id + '\'' +
                ", txType1=" + txType1 +
                ", txType1Name='" + txType1Name + '\'' +
                ", txType2=" + txType2 +
                ", txType2Name='" + txType2Name + '\'' +
                ", txtype=" + txtype +
                ", msgctl=" + msgctl +
                ", treeLevel=" + treeLevel +
                ", checked=" + checked +
                '}';
    }
}
