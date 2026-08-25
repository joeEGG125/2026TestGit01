package com.syscom.fep.frmcommon.parse;

public interface Parser<Out, In> {

    public Out readIn(In in) throws Exception;

    public In writeOut(Out out) throws Exception;

}
